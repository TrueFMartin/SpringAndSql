package com.truefmartin.querier;

import com.truefmartin.IRLexer;
import com.truefmartin.IRParser;
import com.truefmartin.inverter.structs.DictData;
import com.truefmartin.querier.accumulator.*;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import java.util.*;
import java.util.concurrent.*;

public class InvertedFileQuerier {
    private final String[] rawQueryTerms;
    private List<DictData> dictResults;
    private int numDesiredResult;
    public InvertedFileQuerier(String[] rawQueryTerms, int numDesiredResult) {
        this.rawQueryTerms = rawQueryTerms;
        this.numDesiredResult = numDesiredResult;
    }

    public Set<String> parseTokens() {
        StringBuilder sb = new StringBuilder();
        Arrays.stream(rawQueryTerms).forEach(s -> sb.append(s).append(' '));
        IRLexer lexer;
        lexer = new IRLexer(CharStreams.fromString(sb.toString()));
        lexer.removeErrorListeners();
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        IRParser parser = new IRParser(tokens);
        parser.removeErrorListeners();
        // Create an instance of listener that handles exiting of rules
        QueryParser customListener = new QueryParser(rawQueryTerms.length);
        parser.addParseListener(customListener);
        // Begin parsing
        parser.document();
        return customListener.collectResults();
    }

    public List<DictData> queryDict(Set<String> query) {
        int numThreads = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(Math.min(numThreads, query.size()));

        // Create a list of Callable tasks for checking the Dict file in parallel
        List<ReadDictThread> tasks = new LinkedList<>();
        List<Future<DictData>> results = null;
        for (String term : query)
            tasks.add(new ReadDictThread(term));
        try {
            results = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();
        }

        if (results == null)
            return null;
        List<DictData> dictEntries = new ArrayList<>();
        for(var result: results) {
            try {
                DictData dictEntry;
                if ((dictEntry = result.get()) != null)
                    dictEntries.add(dictEntry);
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return dictEntries.isEmpty()? null: dictEntries;
    }

    public CompareType[] queryPost(List<DictData> dictEntries, Query.AccumulatorType accumulatorType) throws ExecutionException, InterruptedException {
        // Get number of processors minus one, so we have a processor for the accumulator
        int numProcessors = Runtime.getRuntime().availableProcessors() - 1;
        // if fewer dict entries than processors, split up dict entries into smaller tasks
        while(dictEntries.size() < numProcessors) {
            dictEntries.sort(Comparator.comparingInt(dictData -> Integer.parseInt(dictData.numDocs)));
            var dictToSplit = dictEntries.get(dictEntries.size()-1);
            int start1 = Integer.parseInt(dictToSplit.start);
            int numDocs = Integer.parseInt(dictToSplit.numDocs);
            int start2 = start1 + numDocs/2;
            // If adding a thread to split the largest dict would not be worth the overhead
            if (start2 - start1 < 15)
                // Do not bother creating an additional thread + dict entry
                break;
            int newNumDocs1 = numDocs/2;
            int newNumDocs2 = numDocs - newNumDocs1;
            // Half the size of the current largest num docs entry
            dictEntries.get(dictEntries.size() -1 ).numDocs = String.valueOf(newNumDocs1);
            // Add a new entry with the other half of the postings
            dictEntries.add(new DictData(dictToSplit.term, String.valueOf(newNumDocs2), String.valueOf(start2)));
        }

        ExecutorService executor = Executors.newFixedThreadPool(Math.min(numProcessors, dictEntries.size()));
        int numPostings = dictEntries.stream().mapToInt(value -> Integer.parseInt(value.numDocs)).reduce(0, Integer::sum);
        ConcurrentLinkedQueue<int[]> queue = new ConcurrentLinkedQueue<>();
        Semaphore semaphore = new Semaphore(0);
        // Create a list of Callable tasks for reading the Post file in parallel
        List<ReadPostThread> tasks = new LinkedList<>();
        for (DictData entry : dictEntries)
            tasks.add(new ReadPostThread(entry, queue, semaphore));
        List<Future<Void>> results = null;
        FutureTask<CompareType[]> accumulatorFuture;
        try {
            results = executor.invokeAll(tasks);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            Accumulator accumulator = null;
            switch(accumulatorType) {
                case HASH:
                    accumulator = new AccumulatorHash(numPostings, numDesiredResult, queue, semaphore);
                    break;
                case WITH_ARRAY:
                    accumulator = new AccumulatorWithArray(numPostings, numDesiredResult, queue, semaphore);
                    break;
                case WITHOUT_ARRAY:
                    accumulator = new AccumulatorWithoutArray(numPostings, numDesiredResult, queue, semaphore);
                    break;
            }
            accumulatorFuture = new FutureTask<>(accumulator);
            new Thread(accumulatorFuture).start();
            executor.shutdown();
            // Wait to make sure all Postings Producers are finished
            if (results != null) {
                for(var result: results) {
                    result.get();
                }
            } else {
                throw new RuntimeException("Results was null");
            }
            accumulator.setProducersFinished();
            // Wait on accumulator
        }
        // Return results from the accumulator
        return accumulatorFuture.get();
    }
}