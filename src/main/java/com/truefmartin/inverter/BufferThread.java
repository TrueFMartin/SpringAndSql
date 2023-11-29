package com.truefmartin.inverter;

import java.util.AbstractMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Phaser;

public class BufferThread implements Callable<Void> {
    LinkedList<SortedBuffer> buffers;
    private final ConcurrentLinkedQueue<AbstractMap.SimpleEntry<Integer, Integer>> queue;
    private final Phaser barrier;
    private double idf;
    private String token;
    private int numDocs;



    public BufferThread(LinkedList<SortedBuffer> buffers,
                        ConcurrentLinkedQueue<AbstractMap.SimpleEntry<Integer, Integer>> queue,
                        Phaser barrier) {
        this.buffers = buffers;
        this.queue = queue;
        this.barrier = barrier;
        this.barrier.register();
    }

    public void openBuffer() {
        for(SortedBuffer buffer: buffers) {
            buffer.open();
        }
    }
    public void prepareToken(String token, double idf, int numDocs) {
        this.token = token;
        this.idf = idf;
        this.numDocs = numDocs;
    }

    @Override
    public Void call() throws Exception {
        while (!buffers.isEmpty()) {
            // Iterator for the linked list
            Iterator<SortedBuffer> itr = buffers.iterator();
            // Stop at phase 1 (for main thread to be READY TO set the term to look for)
            barrier.arriveAndAwaitAdvance();
            // Stop at phase 2 for the main thread to finish setting term to look for
            barrier.arriveAndAwaitAdvance();
            while (itr.hasNext()) {
                SortedBuffer buffer = itr.next();
                // If file finished, remove it from linked list so we don't continue to check it
                if (buffer.isClosed) {
                    itr.remove();
                    continue;
                }
                // Get current term held by SortedBuffer class (it has already been read from file)
                var entry = buffer.getEntry();
                if (!token.equals(entry.term)) {
                    continue;
                }
                // Ignore terms only appearing in one document
                if (numDocs == 1) {
                    buffer.next();
                    break;
                }
                int weight = (int) (10E7 * entry.freq * idf);
                // Write a postings entry
                queue.offer(new AbstractMap.SimpleEntry<>(entry.docID, weight));
                // Update the latest term in buffer, increment the buffer
                buffer.next();
            }
        }
        barrier.arriveAndDeregister();
        return null;
    }
}

