package com.truefmartin.querier.accumulator;

import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

/**
 * Both Accumulators(AccumulatorWith/WithoutArray) use a hashmap for the DocID's and a heap based priority queue
 * to keep track of the top results. The difference is in using an intermediary ArrayList.
 * This implementation maps DocID -> Weight in the hashmap. Every time the heap needs to
 * compare two weights, it must address the HashMap. This saves memory, but results in a
 * slower execution. It is similar to AccumulatorHash except that it uses the priority queue
 * the entire time, instead of at the end. The queue also compares using the HashTable instead
 * of by filling the queue with both DocID and Weights.
 */
public class AccumulatorWithoutArray extends Accumulator{

    public AccumulatorWithoutArray(int numPostings, int numDesiredResults, ConcurrentLinkedQueue<int[]> postings, Semaphore semaphore) {
        super(numPostings, numDesiredResults, postings, semaphore);
    }

    @Override
    public CompareType[] call() throws Exception {

        HashMap<Integer, Integer> docMap = new HashMap<>(numPostings * 3);
        PriorityQueue<Integer> queue = new PriorityQueue<>(numDesiredResults, new CustomCompare(docMap));
        semaphore.acquire();

        // Insert all postings into docMap / queue
        main:
        for (int i = 0; i < numPostings; i++) {
            int[] queueResult;
            while((queueResult = postings.poll()) == null){
                Thread.sleep(10);
                /*
                 Should not happen, but prevents infinite waiting when
                 all producers are finished, but we have not looped through
                 expected number of postings.
                */
                if(producersFinished)
                    break main;
            }
            var posting = new CompareType(queueResult);
            int postingWeight = posting.weight;
            // If its in the map already, update the weight in docMap
            int newTotalWeight = docMap.merge(queueResult[0], postingWeight,Integer::sum);
            // If newTotalWeight is different from the posting's weight, this document has been seen before
            if (newTotalWeight != posting.weight) {
                if (queue.size() >= numDesiredResults) {
                    int leastWeight = docMap.get(queue.peek());
                    // This means the doc was already in the queue (or is likely to be)
                    if(leastWeight <= newTotalWeight - postingWeight) {
                        // If item is in list, remove and re-add it in case the position has changed
                        if(queue.remove(posting.docID)) {
                            queue.add(posting.docID);
                            // Item was not in queue before, but was == in weight to least in queue, and now can enter
                        } else if (newTotalWeight > leastWeight){
                            queue.add(posting.docID);
                            queue.poll();
                        }
                    } else { // else this document could not have been in queue before
                        // If the document now meets the requirements for the queue
                        if (newTotalWeight >= leastWeight) {
                            queue.add(posting.docID);
                            queue.poll();
                        }
                    }
                    // Doc is in queue, but queue is not full
                } else {
                    queue.remove(posting.docID);
                    queue.add(posting.docID);
                }
                continue;
            }
            if (queue.size() >= numDesiredResults) {
                if(docMap.get(queue.peek()) > postingWeight){
                    continue;
                }
                queue.add(posting.docID);
                queue.poll();
            } else {
                queue.add(posting.docID);
            }
        }
        CompareType[] qResults = new CompareType[queue.size()];
        int j = queue.size();
        while (!queue.isEmpty()) {
            int docID = queue.poll();
            qResults[--j] = new CompareType(docMap.get(docID), docID);
        }
        return qResults;
    }
}
