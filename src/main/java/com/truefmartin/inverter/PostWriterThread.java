package com.truefmartin.inverter;

import java.util.AbstractMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PostWriterThread implements Runnable{
    private final ConcurrentLinkedQueue<AbstractMap.SimpleEntry<Integer, Integer>> queue;

    private boolean stop = false;

    public PostWriterThread(ConcurrentLinkedQueue<AbstractMap.SimpleEntry<Integer, Integer>> queue) {
        this.queue = queue;
    }

    @Override
    public void run() {
        try (var postFileWriter = new InvertedFileWriter(InvertedFileWriter.FileType.POST)) {
            AbstractMap.SimpleEntry<Integer, Integer> entry;
            while ((entry = queue.poll()) != null || !stop) {
                if (entry != null)
                    postFileWriter.writePostRecord(entry.getKey(), entry.getValue());
            }
        } catch (Exception e) {
            System.out.println("ERROR in wring POST");
            throw new RuntimeException(e);
        }
    }

    public void setStop(boolean stop) {
        this.stop = stop;
    }
}
