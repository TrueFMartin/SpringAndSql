package com.truefmartin.inverter;

import java.nio.file.Path;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Phaser;

public class Inverter {
    private final int NUM_TERMS;
//------These are here in case I want to pass them in later iterations
    private final int TERM_SIZE;
    private final int NUM_DOC_SIZE;
    private final int START_SIZE;
    private final int DOC_ID_SIZE;
    private final int FILE_NAME_SIZE;
//---------------------------------------------------
    // Default buffer size:
    private int BUFFERED_READ_SIZE = 400;
    private final int CORPUS_SIZE;
    private int numThreads;

    ArrayList<BufferThread> bufferThreads;
    ConcurrentLinkedQueue<AbstractMap.SimpleEntry<Integer, Integer>> queue;
    PostWriterThread postWriterRunner;
    Phaser barrierLock;


    public Inverter(int numTerms, final List<String> fileNames, int bufferSize) {
        this.NUM_TERMS = numTerms;
        CORPUS_SIZE = fileNames.size();
        BUFFERED_READ_SIZE = bufferSize;
        // ----Not used yet ----
        this.DOC_ID_SIZE = 0;
        this.NUM_DOC_SIZE = 0;
        this.START_SIZE = 0;
        this.FILE_NAME_SIZE = 0;
        this.TERM_SIZE = 0;
        // ---------------------
        // Write map file, can be in separate thread, no shared, non-final resources
        new Thread(() -> {
            int index = 0;
            try(var mapFileWriter = new InvertedFileWriter(InvertedFileWriter.FileType.MAP)) {
                // Write map file
                for (String fileName : fileNames) {
                    mapFileWriter.writeMapRecord(index++, Path.of(fileName).getFileName().toString());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }).start();
        queue = new ConcurrentLinkedQueue<>();
        barrierLock = new Phaser();

        this.numThreads = Runtime.getRuntime().availableProcessors();
        // Initialize the buffers
        bufferThreads = new ArrayList<>();
        int index = 0;
        int numBuffers = CORPUS_SIZE / numThreads;
        int remainder = 0;
        for (int i = 1; i <= numThreads; i++) {
            // If in last group of sorted buffers, add the remainder to the total of buffers.
            if(i == numThreads)
                remainder = CORPUS_SIZE % numThreads;
            var tempSortedBuffers = new LinkedList<SortedBuffer>();
            // Create a linked list of all buffers using this portion files (numBuffers * i)
            while( index < numBuffers * i + remainder) {
                tempSortedBuffers.addLast(new SortedBuffer(fileNames.get(index), index++, BUFFERED_READ_SIZE));
            }
            bufferThreads.add(new BufferThread(tempSortedBuffers, queue, barrierLock));
        }
    }

    public void fillGlobalHash(List<AbstractMap.SimpleEntry<String, Integer>> uniquesSorted, int ghtSize) {
        // Open the buffered readers in each buffer
        GlobalHashTable globalHashTable;
        if (ghtSize == -1) {
            globalHashTable = new GlobalHashTable(NUM_TERMS);
        } else {
            globalHashTable = new GlobalHashTable(ghtSize);
        }

        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        bufferThreads.forEach(BufferThread::openBuffer);
        // Get post ready to write
        postWriterRunner = new PostWriterThread(queue);

        FileEntry entry = new FileEntry();
        int start = 0;
        // Register the main thread with the phaser barrier lock
        barrierLock.register();
        try {
            Thread postWriterThread = new Thread(postWriterRunner);
            postWriterThread.start();
            for(BufferThread bufferThread: bufferThreads)
                executor.submit(bufferThread);
            // For each unique term (use this instead of searching array to reduce time complexity at cost of memory)
            for(AbstractMap.SimpleEntry<String, Integer> termToNumDoc: uniquesSorted) {
                String term = termToNumDoc.getKey();
                int numDocs = termToNumDoc.getValue();
                double idf = Math.log(CORPUS_SIZE*1.0/numDocs) + 1;
                // Wait until all threads are ready for the next term
                barrierLock.arriveAndAwaitAdvance();
                bufferThreads.forEach(x -> x.prepareToken(term, idf, numDocs));
                // Signal to bufferThreads they can begin on the next term.
                barrierLock.arriveAndAwaitAdvance();

                if (numDocs == 1)
                    continue;
                globalHashTable.insert(term, numDocs, start);
                start += numDocs;
            }
        } finally {
            executor.shutdown();
            barrierLock.arriveAndDeregister();
            postWriterRunner.setStop(true);
            // Prepare to write dict file
        }
        // Use AutoCloseable of InvFileWriter to ensure close in case of failure
        try(var dictWriter = new InvertedFileWriter(InvertedFileWriter.FileType.DICT)) {
            // Write contents of hash file to dict file
            globalHashTable.printToAny(dictWriter::writeDictRecord);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }

}





