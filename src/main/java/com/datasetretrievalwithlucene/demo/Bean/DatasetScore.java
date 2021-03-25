package com.datasetretrievalwithlucene.demo.Bean;

import javafx.util.Pair;

import java.util.List;

public class DatasetScore {
    public Pair<String, Integer> queryDataset;
    public List<Integer> scores;

    public DatasetScore(Pair<String, Integer> queryDataset, List<Integer> scores) {
        this.queryDataset = queryDataset;
        this.scores = scores;
    }

    public Pair<String, Integer> getQueryDataset() {
        return queryDataset;
    }

    public void setQueryDataset(Pair<String, Integer> queryDataset) {
        this.queryDataset = queryDataset;
    }

    public List<Integer> getScores() {
        return scores;
    }

    public void setScores(List<Integer> scores) {
        this.scores = scores;
    }
}
