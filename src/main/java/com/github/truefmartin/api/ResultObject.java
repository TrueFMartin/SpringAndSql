package com.github.truefmartin.api;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.AbstractMap;

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
        this.text = "/files/" + result.getKey();


//        for (var result: results) {
//            this.results.add("<a href=\"files/" + result.getKey() + "\">" + result.getKey() +", With a weight: " + result.getValue()+ "</a>");
//        }
    }

    public ResultObject(ResultObject copy) {
        this.weight = copy.weight;
        this.file = copy.file;
        this.text = copy.text;
    }

    @Override
    public String toString() {
        return URLEncoder.encode(this.file + "-" + this.weight + "_", StandardCharsets.UTF_8);
    }
}
