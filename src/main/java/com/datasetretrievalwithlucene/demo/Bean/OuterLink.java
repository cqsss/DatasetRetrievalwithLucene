package com.datasetretrievalwithlucene.demo.Bean;

public class OuterLink {
    private int sub_ds;
    private int predicate;
    private int obj_ds;
    private int count;

    public int getSub_ds() {
        return sub_ds;
    }

    public void setSub_ds(int sub_ds) {
        this.sub_ds = sub_ds;
    }

    public int getPredicate() {
        return predicate;
    }

    public void setPredicate(int predicate) {
        this.predicate = predicate;
    }

    public int getObj_ds() {
        return obj_ds;
    }

    public void setObj_ds(int obj_ds) {
        this.obj_ds = obj_ds;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
