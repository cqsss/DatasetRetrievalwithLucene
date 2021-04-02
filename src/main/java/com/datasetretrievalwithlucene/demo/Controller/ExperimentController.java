package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.Query;
import com.datasetretrievalwithlucene.demo.Bean.QueryData;
import com.datasetretrievalwithlucene.demo.Service.QueryDataService;
import com.datasetretrievalwithlucene.demo.Service.QueryService;
import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.RelevanceRanking;
import javafx.util.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;
import java.util.*;

public class ExperimentController {
    @Autowired
    private QueryService queryService;
    @Autowired
    private QueryDataService queryDataService;
    @RequestMapping("/pooling")
    public void pooling() {
        try {
            List<Query> queryList = queryService.getAll();
            int poolingK = GlobalVariances.queryPoolSize[1];
            for (Query q : queryList) {
                List<Pair<Integer, Double>> TFIDFScoreList = RelevanceRanking.TFIDFRankingList(q.getQuery());
                List<Pair<Integer, Double>> BM25ScoreList = RelevanceRanking.BM25RankingList(q.getQuery());
                List<Pair<Integer, Double>> FSDMScoreList = RelevanceRanking.FSDMRankingList(q.getQuery());
                List<Pair<Integer, Double>> DPRScoreList = RelevanceRanking.DPRRankingList(q.getQuery());
                if (TFIDFScoreList.size() < poolingK || BM25ScoreList.size() < poolingK ||FSDMScoreList.size() < poolingK) {
                    continue;
                }
                Set<Integer> scoreSet = new HashSet<>();
                for (int i = 0; i < poolingK; i++) {
                    scoreSet.add(TFIDFScoreList.get(i).getKey());
                    scoreSet.add(BM25ScoreList.get(i).getKey());
                    scoreSet.add(FSDMScoreList.get(i).getKey());
                    scoreSet.add(DPRScoreList.get(i).getKey());
                }
                for (int i : scoreSet) {
                    System.out.println(q+"\t"+i);
                    QueryData queryData = new QueryData();
                    queryData.setQuery_id(q.getQuery_id());
                    queryData.setDataset_id(i);
                    queryDataService.insertQueryData(queryData);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
