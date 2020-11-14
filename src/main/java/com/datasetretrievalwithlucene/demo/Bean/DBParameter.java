package com.datasetretrievalwithlucene.demo.Bean;

import java.util.List;

public class DBParameter {
    public String name;
    public List<Integer> value;

    public DBParameter() {
    }

    public DBParameter(String name, List<Integer> value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Integer> getValue() {
        return value;
    }

    public void setValue(List<Integer> value) {
        this.value = value;
    }
}
