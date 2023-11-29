package com.truefmartin.querier.accumulator;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Both Accumulators(AccumulatorWith/WithoutArray) use a hashmap for the DocID's and a heap based priority queue
 * to keep track of the top results. The difference is in using an intermediary ArrayList.
 * However, this implementation just fills a hash table with all the postings, summing the
 * value if that docID is already present. At the end, it uses a priority queue to get the
 * top results. This is the most readable implementation.
 */
public class AccumulatorHash extends Accumulator{
    public AccumulatorHash(int numPostings, int numDesiredResults, ConcurrentLinkedQueue<int[]> postings, Semaphore semaphore) {
        super(numPostings, numDesiredResults, postings, semaphore);
    }

    @Override
    public CompareType[] call() throws Exception {
        HashMap<Integer, Integer> docMap = new HashMap<>(numPostings*3);

        // Wait for at least one ReadPostThread to start filling ConcurrentQueue
        semaphore.acquire();
        main:
        for (int i = 0; i < numPostings; i++) {
            int[] queueResult;
            // Just in case there is a slight delay in the postings being filled sleep for 10ms
            while ((queueResult = postings.poll()) == null) {
                Thread.sleep(10);
                /*
                 Should not happen, but prevents infinite waiting when
                 all producers are finished, but we have not looped through
                 expected number of postings.
                */
                if (producersFinished)
                    break main;
            }
            docMap.merge(queueResult[0], queueResult[1],Integer::sum);
        }

        PriorityQueue<Map.Entry<Integer, Integer>> queue = new PriorityQueue<>(numDesiredResults, Comparator.comparingInt(Map.Entry::getValue));

        for (Map.Entry<Integer, Integer> entry : docMap.entrySet()) {
            queue.offer(entry);
            if (queue.size() > numDesiredResults) {
                queue.poll();
            }
        }

        CompareType[] qResults = new CompareType[queue.size()];
        int j = queue.size();
        while (!queue.isEmpty()) {
            var entry = queue.poll();
            qResults[--j] = new CompareType(entry.getValue(), entry.getKey());
        }
        return qResults;
    }
}
