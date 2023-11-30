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
    public int getFileNumber() {
        return fileNumber;
    }

    String file;
    String text;
    int fileNumber;
    public ResultObject(AbstractMap.SimpleEntry<String, Integer> result) {
        this.weight = result.getValue();
        this.file = result.getKey();
        this.text = "/files/" + result.getKey();
        this.fileNumber = Integer.parseInt(this.file.split("\\.")[0]);

//        for (var result: results) {
//            this.results.add("<a href=\"files/" + result.getKey() + "\">" + result.getKey() +", With a weight: " + result.getValue()+ "</a>");
//        }
    }

    public ResultObject(ResultObject copy) {
        this.weight = copy.weight;
        this.file = copy.file;
        this.text = copy.text;
        this.fileNumber = copy.fileNumber;
    }

    @Override
    public String toString() {
        return this.file + "," + this.weight + " ";
    }
}
