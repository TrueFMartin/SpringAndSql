package com.truefmartin.api;

import java.util.AbstractMap;
import java.util.List;

public class ResultObject {
    int weight;

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }
    public String getText() {
        return text;
    }
    String file;
    String text;
    public ResultObject(AbstractMap.SimpleEntry<String, Integer> result) {
        this.weight = result.getValue();
        this.file = result.getKey();
        this.text = "files/" + result.getKey();
//        for (var result: results) {
//            this.results.add("<a href=\"files/" + result.getKey() + "\">" + result.getKey() +", With a weight: " + result.getValue()+ "</a>");
//        }
    }
}
