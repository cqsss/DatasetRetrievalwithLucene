package com.datasetretrievalwithlucene.demo.Bean;

public class QueryData {
    private String query;
    private int dataset_id;
    private String topic_source;
    private int TFIDF_rank;
    private int BM25_rank;
    private int FSDM_rank;

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public int getDataset_id() {
        return dataset_id;
    }

    public void setDataset_id(int dataset_id) {
        this.dataset_id = dataset_id;
    }

    public String getTopic_source() {
        return topic_source;
    }

    public void setTopic_source(String topic_source) {
        this.topic_source = topic_source;
    }

    public int getTFIDF_rank() {
        return TFIDF_rank;
    }

    public void setTFIDF_rank(int TFIDF_rank) {
        this.TFIDF_rank = TFIDF_rank;
    }

    public int getBM25_rank() {
        return BM25_rank;
    }

    public void setBM25_rank(int BM25_rank) {
        this.BM25_rank = BM25_rank;
    }

    public int getFSDM_rank() {
        return FSDM_rank;
    }

    public void setFSDM_rank(int FSDM_rank) {
        this.FSDM_rank = FSDM_rank;
    }
}
