package com.github.truefmartin.api;


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

        if (args.length == 0) {
            error = new Error("No query provided");
            return null;
        }
        return null;
    }
}
