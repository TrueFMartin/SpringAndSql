package com.truefmartin.querier.accumulator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
/**
 * Both Accumulators(AccumulatorWith/WithoutArray) use a hashmap for the DocID's and a heap based priority queue
 * to keep track of the top results. The difference is in using an intermediary ArrayList.
 * This implementation maps DocID -> Index in the hashmap, where Index is a position in the
 * ArrayList. The ArrayList holds the DocID and Weight. The priority queue holds an integer
 * which refers to an index in the ArrayList. This way, the queue can directly access the
 * weight by checking the ArrayList instead of having to first check the HashMap for every
 * comparison. It is faster, but has a higher memory usage because of the Array.
 */
public class AccumulatorWithArray extends Accumulator {

    public AccumulatorWithArray(int numPostings, int numDesiredResults, ConcurrentLinkedQueue<int[]> postings, Semaphore semaphore) {
        super(numPostings, numDesiredResults, postings, semaphore);
    }

    @Override
    public CompareType[] call() throws Exception {
        // Holds postings that we have reviewed
        ArrayList<CompareType> list = new ArrayList<>(numPostings);

        PriorityQueue<Integer> queue = new PriorityQueue<>(numDesiredResults, new CustomCompareArray(list));
        // Hopefully there are fewer documents than postings,
        // thus setting size to 2.5 * postings instead of 3 *
        HashMap<Integer, Integer> mapToList = new HashMap<>((int)(numPostings*2.5));

        // Wait for at least one ReadPostThread to start filling ConcurrentQueue
        semaphore.acquire();
        main:
        for (int i = 0; i < numPostings; i++) {
            int[] queueResult;
            // Just in case there is a slight delay in the postings being filled sleep for 10ms
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

            int listLocation = mapToList.getOrDefault(posting.docID, -1);

            int docWeight = posting.weight;
            // If it's in the map already, update the weight in docMap
            if (listLocation != -1) {
                int docsPreviousWeight = list.get(listLocation).weight;
                list.get(listLocation).weight += posting.weight;
                if (queue.size() >= numDesiredResults) {
                    int leastWeight = list.get(queue.peek()).weight;
                    // This means this document was already in the queue
                    if(leastWeight <= docsPreviousWeight) {
                        // If item is in list, remove and re-add it in case the position has changed
                        if(queue.remove(listLocation)) {
                            queue.add(listLocation);
                        // Item was not in queue before, but was == in weight to least in queue, and now can enter
                        } else if (docWeight + docsPreviousWeight > leastWeight){
                            queue.add(listLocation);
                            queue.poll();
                        }
                    } else { // else this document could not have been in queue before
                        // If the document now meets the requirements for the queue
                        if (docWeight + docsPreviousWeight >= leastWeight) {
                            queue.add(listLocation);
                            queue.poll();
                        }
                    }
                // Doc is in queue, but queue is not full
                } else {
                    queue.remove(listLocation);
                    queue.add(listLocation);
                }
                continue;
            }
            //Else, it's a new docID so add it to the hashtable
            listLocation = list.size();
            mapToList.put(posting.docID, listLocation);
            list.add(new CompareType(posting.weight, posting.docID));

            // If queue is full,
            if (queue.size() >= numDesiredResults) {
                // If weight is too low or matched with lowest, skip
                if(list.get(queue.peek()).weight >= posting.weight){
                    continue;
                }

                queue.add(listLocation);
                queue.poll();
            } else { // Else queue is not full, add posting
                queue.add(listLocation);
            }
        }
        CompareType[] qResults = new CompareType[queue.size()];
        int j = queue.size();
        while (!queue.isEmpty()) {
            qResults[--j] = list.get(queue.poll());
        }
        return qResults;
    }
}
