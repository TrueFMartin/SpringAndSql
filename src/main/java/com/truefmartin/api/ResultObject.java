package com.truefmartin.api;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ResourceLoader;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class ResultObject {

    List<String> results;
    public ResultObject(List<AbstractMap.SimpleEntry<String, Integer>> results) {
        this.results = new ArrayList<>();
        for (var result: results) {
            this.results.add("<a href=\"/" + result.getKey() + "\">" + result.getKey() +", With a weight: " + result.getValue()+ "</a>");
        }
    }

    public List<String> getResults() {
        return results;
    }
}
