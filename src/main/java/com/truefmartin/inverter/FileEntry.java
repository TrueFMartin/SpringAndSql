package com.truefmartin.inverter;

// Holds the contents and information about each entry in the sorted temporary files
public class FileEntry {
    String term;
    double freq;
    int docID;

    public FileEntry() {
        this(-1);
    }
    public FileEntry(int docID) {
        this.docID = docID;
        this.term = "";
        this.freq = -1;
    }
}