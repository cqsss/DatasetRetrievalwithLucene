package com.datasetretrievalwithlucene.demo.Bean;

public class Annotation {
    private int annotation_id;
    private int user_id;
    private int query_id;
    private int dataset_id;
    private int rating;
    private String annotation_time;

    public int getAnnotation_id() {
        return annotation_id;
    }

    public void setAnnotation_id(int annotation_id) {
        this.annotation_id = annotation_id;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public int getQuery_id() {
        return query_id;
    }

    public void setQuery_id(int query_id) {
        this.query_id = query_id;
    }

    public int getDataset_id() {
        return dataset_id;
    }

    public void setDataset_id(int dataset_id) {
        this.dataset_id = dataset_id;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getAnnotation_time() {
        return annotation_time;
    }

    public void setAnnotation_time(String annotation_time) {
        this.annotation_time = annotation_time;
    }
}
