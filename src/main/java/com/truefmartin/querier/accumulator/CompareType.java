package com.truefmartin.querier.accumulator;

import com.truefmartin.inverter.structs.PostData;

public class CompareType implements Comparable<CompareType> {
    public int weight;
    public int docID;
    CompareType(int weight, int docID) {
        this.weight = weight;
        this.docID = docID;
    }
    CompareType(PostData postData) {
        this.weight = Integer.parseInt(postData.weight);
        this.docID = Integer.parseInt(postData.docId);
    }
    CompareType(int[] data) {
        this.docID = data[0];
        this.weight = data[1];
    }

    @Override
    public int compareTo(CompareType o) {
        return Integer.compare(this.weight, o.weight);
    }

    public int compareToDoc(CompareType o) {
        return Integer.compare(this.docID, o.docID);
    }
    @Override
    public String toString() {
        return "DocID: " + docID + "\tW: " + this.weight + "\n";
    }

}
