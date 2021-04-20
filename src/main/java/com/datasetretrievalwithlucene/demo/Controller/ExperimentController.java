package com.datasetretrievalwithlucene.demo.Controller;

import com.datasetretrievalwithlucene.demo.Bean.Query;
import com.datasetretrievalwithlucene.demo.Bean.QueryData;
import com.datasetretrievalwithlucene.demo.Service.QueryDataService;
import com.datasetretrievalwithlucene.demo.Service.QueryService;
import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.RelevanceRanking;
import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

public class ExperimentController {
    @Autowired
    private QueryService queryService;
    @Autowired
    private QueryDataService queryDataService;

    private IndexReader indexReader;
    private IndexSearcher indexSearcher;
    private List<String> queryList;
    private List<String> poolingQueryList;
    private Directory directory;
    public void init() {
        try {
            directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void readQueries(String fileName) {
        try {
            String str;
            queryList = new ArrayList<>();
            BufferedReader bufferedReader = new BufferedReader(new FileReader(fileName));
            while ((str = bufferedReader.readLine()) != null) {
                queryList.add(str);
            }
            bufferedReader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void getPoolingQueries(double k) {
        try {
            poolingQueryList = new ArrayList<>();
            String[] fields = GlobalVariances.queryFields;
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            for (String qi : queryList) {
                String tmp =qi.replaceAll("\\p{P}"," ");
                org.apache.lucene.search.Query query = queryParser.parse(tmp);
                int queryLength = query.toString().split(" ").length / fields.length;
                TopDocs docsSearch = indexSearcher.search(query, 500);
                ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
                int cnt = 0;
                for (ScoreDoc si : scoreDocs) {
                    double averageScore = si.score / (double) queryLength / (double) fields.length;
                    if (averageScore >= k) cnt++;
                    //System.out.println(e);
                }
                if(cnt>20)
                    poolingQueryList.add(qi);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequestMapping("/queryfilter")
    public void queryFilter() {
        double k = 0.4;
        try {
            getPoolingQueries(k);
            for (String q : poolingQueryList) {
                Query query = new Query();
                query.setQuery_text(q);
                queryService.insertQuery(query);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @RequestMapping("/pooling")
    public void pooling() {
        try {
            List<Query> queryList = queryService.getAll();
            int poolingK = GlobalVariances.queryPoolSize[1];
            for (Query q : queryList) {
                List<Pair<Integer, Double>> TFIDFScoreList = RelevanceRanking.TFIDFRankingList(q.getQuery_text());
                List<Pair<Integer, Double>> BM25ScoreList = RelevanceRanking.BM25RankingList(q.getQuery_text());
                List<Pair<Integer, Double>> FSDMScoreList = RelevanceRanking.FSDMRankingList(q.getQuery_text());
                List<Pair<Integer, Double>> DPRScoreList = RelevanceRanking.DPRRankingList(q.getQuery_text());
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
