package com.truefmartin.api;

import com.truefmartin.querier.FileNameRetriever;
import com.truefmartin.querier.Query;
import com.truefmartin.querier.accumulator.CompareType;

import java.util.AbstractMap;
import java.util.List;

public class QueryHandler {
    Error error = null;
    public String[] splitArgs(String s) {
        return s.split(" ");
    }

    public List<AbstractMap.SimpleEntry<String, Integer>> Query(String[] args) {
        if (args.length == 1) {
            args = splitArgs(args[0]);
        }
        Query query = new Query(args);
        if(!query.isValidArgs()) {
            error = new Error("Invalid query. Pass in the optional query arguments followed by the query");
            return null;
        }
        // Get docID and weights of results
        CompareType[] sortedDocs = query.query();
        if(sortedDocs == null || sortedDocs.length == 0) {
            error = new Error("No results found, please try again.");
            return null;
        }
        // Get filename and weights
        List<AbstractMap.SimpleEntry<String, Integer>> fileNames = FileNameRetriever.getFileNames(sortedDocs);
        if (fileNames == null || fileNames.isEmpty()) {
            error = new Error("Error getting file names.");
            return null;
        }
        return fileNames;
//        for (var fileName : fileNames) {
//            System.out.println("Weight: " + fileName.getValue() + ",\t\t" + fileName.getKey());
//        }
    }
}
