package com.datasetretrievalwithlucene.demo.Bean;

public class TripleID {
    public Integer subject;
    public Integer predicate;
    public Integer object;

    public TripleID() {
    }

    public TripleID(Integer subject, Integer predicate, Integer object) {
        this.subject = subject;
        this.predicate = predicate;
        this.object = object;
    }

    public Integer getSubject() {
        return subject;
    }

    public void setSubject(Integer subject) {
        this.subject = subject;
    }

    public Integer getPredicate() {
        return predicate;
    }

    public void setPredicate(Integer predicate) {
        this.predicate = predicate;
    }

    public Integer getObject() {
        return object;
    }

    public void setObject(Integer object) {
        this.object = object;
    }
}
