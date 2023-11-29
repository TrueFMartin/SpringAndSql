package com.truefmartin.inverter.structs;

public class DictData extends Writeable{
    public String term;
    public String numDocs;
    public String start;

    public DictData(){
        this("","","");
    }
    public DictData(String term, String numDocs, String start) {
        super(term, numDocs, start);
        this.term = term;
        this.numDocs = numDocs;
        this.start = start;
    }

    @Override
    protected void reassignFields() {
        this.term = columns[0];
        this.numDocs = columns[1];
        this.start = columns[2];
    }
    @Override
    public void print(){
        System.out.println("Term: " + this.term + ", NumDocs: " + this.numDocs + ", Start: " + this.start);
    }

    @Override
    public boolean isEmpty() {
        return this.numDocs.isEmpty() || this.numDocs.equals("-1");
    }
}

