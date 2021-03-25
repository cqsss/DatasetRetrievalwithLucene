package com.datasetretrievalwithlucene.demo.Bean;

public class Query {
    int query_id;
    String query;
    String topic_source;

    public int getQuery_id() {
        return query_id;
    }

    public void setQuery_id(int query_id) {
        this.query_id = query_id;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getTopic_source() {
        return topic_source;
    }

    public void setTopic_source(String topic_source) {
        this.topic_source = topic_source;
    }
}
