package com.truefmartin.inverter.structs;

public class PostData extends Writeable{
    public String docId;
    public String weight;

    public PostData() {
        this("","");
    }
    public PostData(String docId, String weight) {
        super(docId, weight);
        this.docId = docId;
        this.weight = weight;
    }

    @Override
    protected void reassignFields() {
        this.docId = this.columns[0];
        this.weight = this.columns[1];
    }

    @Override
    public void print(){
        System.out.println("DocID: " + this.docId + ", Weight: " + this.weight);
    }

    @Override
    public boolean isEmpty() {
        return docId.isEmpty() || this.docId.equals("-1");
    }


}
