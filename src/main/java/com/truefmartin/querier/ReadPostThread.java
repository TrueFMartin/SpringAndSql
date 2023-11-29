package com.truefmartin.querier;

import com.truefmartin.inverter.InvertedFileReader;
import com.truefmartin.inverter.InvertedFileWriter;
import com.truefmartin.inverter.structs.DictData;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public class ReadPostThread implements Callable<Void> {
    private final DictData dictData;
    private final ConcurrentLinkedQueue<int[]> queue;
    private final Semaphore semaphore;

    ReadPostThread(DictData dictData, ConcurrentLinkedQueue<int[]> queue, Semaphore semaphore) {
        this.dictData = dictData;
        this.queue = queue;
        this.semaphore = semaphore;
    }

    @Override
    public Void call() throws Exception {
        // Use AutoCloseable to close postReader on finish
        try(var postReader = new InvertedFileReader(InvertedFileWriter.FileType.POST)) {
            postReader.readPostToQueue(Integer.parseInt(dictData.start),
                    Integer.parseInt(dictData.numDocs), queue, semaphore);
        }
        return null;
    }
}
