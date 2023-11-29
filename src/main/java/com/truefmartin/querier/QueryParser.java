package com.truefmartin.querier;

import com.truefmartin.parser.IRParserEvaluator;

import java.io.FileNotFoundException;
import java.util.HashSet;
import java.util.Set;

public class QueryParser extends IRParserEvaluator {
    Set<String> results;

    public QueryParser(int rawTermCount) {
        results = new HashSet<>(rawTermCount);
    }
    @Override
    protected void toHashTable(String s) {
        if(s.length() > 1) {
            results.add(s.toLowerCase());
        }
    }
    @Override
    protected void finish() throws FileNotFoundException {
        // Do nothing when called
    }
    public Set<String> collectResults() {
        return results;
    }
}
