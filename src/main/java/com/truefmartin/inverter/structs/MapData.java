package com.truefmartin.inverter.structs;

public class MapData extends Writeable{
    public String docId;
    public String fileName;

    public MapData() {
        this("", "");
    }
    public MapData(String docId, String fileName) {
        super(docId, fileName);
        this.docId = docId;
        this.fileName = fileName;
    }

    @Override
    protected void reassignFields() {
        this.docId = this.columns[0];
        this.fileName = this.columns[1];
    }

    @Override
    public void print(){
        System.out.println("DocID: " + this.docId + ", FileName: " + this.fileName);
    }

    @Override
    public boolean isEmpty() {
        return this.docId.isEmpty() || this.docId.equals("-1");
    }

}
