package com.truefmartin.querier;

import com.truefmartin.querier.accumulator.CompareType;

import java.util.Arrays;
import java.util.concurrent.ExecutionException;

public class Query {
    public static boolean DEBUG = false;
    private final String resultCLArg = "-result-size=";
    private final String speedCLArg = "-speed=";
    private final int resultSize;
    private final AccumulatorType accumulatorType;

    String[] queryArgs;
    public enum AccumulatorType{
        WITHOUT_ARRAY, WITH_ARRAY, HASH;
    }

    public Query(String[] args) {
        boolean debug = false;
        int speed = 2;
        int size = 10;
        // Parse optional arguments
        int numOptional = 0;
        for (String arg : args) {
            if (arg.equals("-debug")) {
                debug = true;
                numOptional++;
            } else if (arg.startsWith(resultCLArg)) {
                size = Integer.parseInt(arg.split("=")[1]);
                numOptional++;
            } else if (arg.startsWith(speedCLArg)) {
                speed = Integer.parseInt(arg.split("=")[1]);
                numOptional++;
            }
        }
        switch (speed) {
            case 1:
                accumulatorType = AccumulatorType.WITHOUT_ARRAY; break;
            case 3:
                accumulatorType = AccumulatorType.HASH; break;
            default:
                accumulatorType = AccumulatorType.WITH_ARRAY; break;
        }
        resultSize = size;
        queryArgs = Arrays.copyOfRange(args, numOptional, args.length);
    }

    public boolean isValidArgs() {
        return queryArgs != null && queryArgs.length != 0;
    }

    public CompareType[] query() {
        if(queryArgs == null || queryArgs.length == 0) {
            return new CompareType[0];
        }
        var invertedFileQuery = new InvertedFileQuerier(queryArgs, resultSize);
        var tokens = invertedFileQuery.parseTokens();
        var dictEntries = invertedFileQuery.queryDict(tokens);
        if(dictEntries == null) {
            return new CompareType[0];
        }
        try {
            return invertedFileQuery.queryPost(dictEntries, accumulatorType);

        } catch (ExecutionException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}

