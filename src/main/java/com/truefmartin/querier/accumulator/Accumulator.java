package com.truefmartin.querier.accumulator;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

public abstract class Accumulator implements Callable<CompareType[]> {
    final int numPostings;
    final int numDesiredResults;
    final ConcurrentLinkedQueue<int[]> postings;
    final Semaphore semaphore;
    boolean producersFinished = false;

    Accumulator(int numPostings, int numDesiredResults, ConcurrentLinkedQueue<int[]> postings, Semaphore semaphore) {
        this.numPostings = numPostings;
        this.numDesiredResults = numDesiredResults;
        this.postings = postings;
        this.semaphore = semaphore;
    }
    public void setProducersFinished() {
        this.producersFinished = true;
    }
    @Override
    public abstract CompareType[] call() throws Exception;
}
