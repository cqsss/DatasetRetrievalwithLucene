package com.datasetretrievalwithlucene.demo.util;

import javafx.util.Pair;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;
import org.omg.CORBA.PUBLIC_MEMBER;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

import java.io.*;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QualityRanking {
    private static final Logger logger = LoggerFactory.getLogger(QualityRanking.class);
    /**
     * outLinks.get(i): i指向的点的集合
     * inLinks.get(i): 指向i的点的集合
     * outLinkCount.get(i): i指出的边数
     * inLinkCount.get(i): 指向i的边数
     */
    private static final Map<Integer, Integer> outLinkCount = new HashMap<>();
    private static final Map<Integer, Integer> inLinkCount = new HashMap<>();
    private static final Map<Integer, List<Integer>> outLinks = new HashMap<>();
    private static final Map<Integer, List<Integer>> inLinks = new HashMap<>();
    private static final Map<Pair<Integer, Integer>, Map<Integer, Integer>> edgeSet = new HashMap<>();
    private static final Map<Integer, Integer> predicateCount = new HashMap<>();
    private static final Map<Integer, List<Pair<Integer, Integer>>> outLinksWithCount = new HashMap<>();
    private static final Map<Integer, List<Pair<Integer, Integer>>> inLinksWithCount = new HashMap<>();
    private static int maxID = 0;
    private static int linkCount = 0;
    private static double linkSum = 0.0;

    private static Directory directory;
    private static IndexReader indexReader;
    private static IndexSearcher indexSearcher;

    private static void init() {
        try {
            directory = MMapDirectory.open(Paths.get(GlobalVariances.index_Dir));
            indexReader = DirectoryReader.open(directory);
            indexSearcher = new IndexSearcher(indexReader);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void writeResult (List<Double> scoreList, String method) {
        try {
            File resultFile = new File(GlobalVariances.qualityRankingResultPath + method + ".out");
            if (!resultFile.exists()) {
                resultFile.createNewFile();
            }
            FileWriter fileWriter = new FileWriter(resultFile.getAbsoluteFile());
            BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            for (int i = 0; i < maxID; i++) {
                bufferedWriter.write(String.format("%.20f",scoreList.get(i)));
                bufferedWriter.write("\n");
            }
            bufferedWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addEdgeDING (Integer u, Integer v, Integer c, Integer p) {
        int tmp = 0;
        Pair<Integer, Integer> uv = new Pair<>(u, v);
        if (edgeSet.containsKey(uv)) {
            if (edgeSet.get(uv).containsKey(p)) {
                tmp = edgeSet.get(uv).get(p);
            }
            edgeSet.get(uv).put(p, tmp + c);
        } else {
            Map<Integer, Integer> tmpMap = new HashMap<>();
            tmpMap.put(p, c);
            edgeSet.put(uv, tmpMap);
        }
        if (outLinksWithCount.containsKey(u)) {
            outLinksWithCount.get(u).add(new Pair<>(v, p));
        } else {
            List<Pair<Integer, Integer>> tmpList = new ArrayList<>();
            tmpList.add(new Pair<>(v, p));
            outLinksWithCount.put(u, tmpList);
        }
        if (inLinksWithCount.containsKey(v)) {
            inLinksWithCount.get(v).add(new Pair<>(u, p));
        } else {
            List<Pair<Integer, Integer>> tmpList = new ArrayList<>();
            tmpList.add(new Pair<>(u, p));
            inLinksWithCount.put(v, tmpList);
        }
        tmp = 0;
        if (predicateCount.containsKey(p)) {
            tmp = predicateCount.get(p);
        }
        linkCount ++;
        linkSum += (double) c;
        predicateCount.put(p, tmp + 1);
    }
    private static void readDataBaseDING(JdbcTemplate jdbcTemplate) {
        try {
            List<Map<String, Object>> res;
            //res = jdbcTemplate.queryForList("SELECT sub_ds,obj_ds,predicate,count FROM outerlink3 LIMIT 0,10");
            res = jdbcTemplate.queryForList("SELECT sub_ds,obj_ds,predicate,count FROM outerlink3");
            for (Map<String, Object> ri : res) {
                int dataset1 = Integer.parseInt(ri.get("sub_ds").toString());
                int dataset2 = Integer.parseInt(ri.get("obj_ds").toString());
                maxID = Math.max(maxID, dataset1);
                maxID = Math.max(maxID, dataset2);
                int predicate = Integer.parseInt(ri.get("predicate").toString());
                int count = Integer.parseInt(ri.get("count").toString());
                addEdgeDING(dataset1, dataset2, count, predicate);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private static Double getTF(Integer i, Integer j, Integer p) {
        double res = 0.0;
        for (Pair<Integer, Integer> k : outLinksWithCount.get(i)) {
            res = Math.max(res, edgeSet.get(new Pair<>(i, k.getKey())).get(k.getValue()));
            //res += edgeSet.get(new Pair<>(i, k.getKey())).get(k.getValue());
        }
        res = (double) edgeSet.get(new Pair<>(i, j)).get(p) / res;
        return res;
    }
    private static Double getIDF(Integer p) {
        return Math.log((double) linkCount / (1.0 + predicateCount.get(p)));
        //return Math.log(((double) linkCount - predicateCount.get(p) + 0.5) / (0.5 + predicateCount.get(p)) + 1.0);
    }
    private static Double getBM25(Integer i, Integer j, Integer p) {
        double avgll;
        double L = 0.0;
        double k1 = 1.2;
        double b = 0.75;
        double tf;
        for (Pair<Integer, Integer> k : outLinksWithCount.get(i)) {
            L += (double) edgeSet.get(new Pair<>(i, k.getKey())).get(k.getValue());
        }
        double f = (double) edgeSet.get(new Pair<>(i, j)).get(p);
        avgll = linkSum / (double) linkCount;
        tf = (f * (k1 + 1.0)) / (f + k1 * (1.0 - b + b * L / avgll));
        return tf * getIDF(p);
    }
    private static Double getW(Integer i, Integer j, Integer p) {
        //return  getBM25(i, j, p);
        return getTF(i, j, p) * getIDF(p);
    }
    private static Double getP(Integer i, Integer j, Integer p) {
        double res = 0.0;
        for (Pair<Integer, Integer> k : outLinksWithCount.get(i)) {
            res += getW(i, k.getKey(), k.getValue());
        }
        res = getW(i, j, p) / res;
        return res;
    }

    private static void addEdge(Integer u, Integer v) {
        if (outLinks.containsKey(u)) {
            outLinks.get(u).add(v);
        } else {
            List<Integer> tmp = new ArrayList<>();
            tmp.add(v);
            outLinks.put(u, tmp);
        }
        if (inLinks.containsKey(v)) {
            inLinks.get(v).add(u);
        } else {
            List<Integer> tmp = new ArrayList<>();
            tmp.add(u);
            inLinks.put(v, tmp);
        }
        Integer tmp = 0;
        if (outLinkCount.containsKey(u))
            tmp = outLinkCount.get(u);
        outLinkCount.put(u, tmp + 1);
        tmp = 0;
        if (inLinkCount.containsKey(v))
            tmp = inLinkCount.get(v);
        inLinkCount.put(v, tmp + 1);
    }

    private static void readDataBase(JdbcTemplate jdbcTemplate) {
        try {
            List<Map<String, Object>> res;
            res = jdbcTemplate.queryForList("SELECT sub_ds,obj_ds,count FROM outerlink3");
            for (Map<String, Object> ri : res) {
                Integer dataset1 = Integer.parseInt(ri.get("sub_ds").toString());
                Integer dataset2 = Integer.parseInt(ri.get("obj_ds").toString());
                maxID = Math.max(maxID, dataset1);
                maxID = Math.max(maxID, dataset2);
                addEdge(dataset1, dataset2);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 单向边版本的PageRank
     * @param jdbcTemplate
     */
    public static List<Double> iterativePageRank(JdbcTemplate jdbcTemplate) {
        readDataBase(jdbcTemplate);
        List<Double> pr = new ArrayList<>();
        try {
            //Double N = (double) indexReader.getDocCount(field);
            //maxID = 15;
            double N = (double) maxID;
            double d = 0.85;
            List<Double> tmp = new ArrayList<>();
            for (int i = 0; i < maxID; i++) {
                pr.add(1.0 / N);
                tmp.add(1.0 / N);
            }
            //System.out.println(maxID);
            int cnt = 0;
            int t = 0;
            while (cnt != maxID) {
                cnt = 0;
                t++;
                Double sumPR;
                for (int i = 0; i < maxID; i++) {
                    sumPR = 0.0;
                    if (inLinks.containsKey(i + 1)) {
                        for (int j : inLinks.get(i + 1)) {
                            sumPR += pr.get(j - 1) / outLinkCount.get(j);
                        }
                    }
                    sumPR *= d;
                    sumPR += (1.0 - d) / N;
                    if (sumPR.equals(pr.get(i)))
                        cnt++;
                    tmp.set(i, sumPR);
                }
                pr = tmp;
                //System.out.println(cnt);
            }
            logger.info(pr.toString());
            writeResult(pr, "PageRank");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pr;
    }

    /**
     * 仅考虑度数的DRank
     * @param jdbcTemplate
     */
    public static List<Double> DRank(JdbcTemplate jdbcTemplate) {
        readDataBase(jdbcTemplate);
        List<Double> pr = new ArrayList<>();
        try {
            //Double N = (double) indexReader.getDocCount(field);
            //maxID = 15;
            double tmp;
            for (Integer i = 0; i < maxID; i++) {
                tmp = 0.0;
                if (outLinkCount.containsKey(i))
                    tmp += outLinkCount.get(i);
                if (inLinkCount.containsKey(i))
                    tmp += inLinkCount.get(i);
                pr.add(tmp);
            }
            logger.info(pr.toString());
            writeResult(pr, "DRank");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pr;
    }

    /**
     * DING
     * @param jdbcTemplate
     */
    public static List<Double> DING(JdbcTemplate jdbcTemplate) {
        readDataBaseDING(jdbcTemplate);
        List<Double> pr = new ArrayList<>();
        try {
            //Double N = (double) indexReader.getDocCount(field);
            //maxID = 15;
            double N = (double) maxID;
            double d = 0.85;
            List<Double> tmp = new ArrayList<>();
            for (int i = 0; i < maxID; i++) {
                pr.add(1.0 / N);
                tmp.add(1.0 / N);
            }
            int cnt = 0;
            int t = 0;
            while(cnt != maxID) {
                cnt = 0;
                t ++;
                Double sumPR;
                for (int i = 0; i < maxID; i++) {
                    sumPR = 0.0;
                    if(inLinksWithCount.containsKey(i + 1)) {
                        for (Pair<Integer, Integer> j : inLinksWithCount.get(i + 1)) {
                            sumPR += pr.get(j.getKey() - 1) * getP(j.getKey(), i + 1, j.getValue());
                        }
                    }
                    sumPR *= d;
                    sumPR += (1.0 - d) / N;
                    if(sumPR.equals(pr.get(i)))
                        cnt++;
                    tmp.set(i, sumPR);
                }
                pr = tmp;
                //System.out.println(pr);
            }
            //System.out.println(pr);
            logger.info(pr.toString());
            writeResult(pr, "DING");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pr;
    }

    public static List<Pair<Integer, Double>> DRankRankingList(String query) {
        init();
        query=query.replaceAll("\\p{P}"," ");
        List<Pair<Integer, Double>> DRankScoreList = new ArrayList<>();
        List<Pair<Integer, Double>> res = new ArrayList<>();
        try {
            String str;
            int t = 1;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(GlobalVariances.qualityRankingResultPath + "DRank.out"));
            while ((str = bufferedReader.readLine()) != null) {
                DRankScoreList.add(new Pair<Integer, Double>(t, Double.parseDouble(str)));
            }
            bufferedReader.close();
            String[] fields = GlobalVariances.queryFields;
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            Query parsedQuery = queryParser.parse(query);
            TopDocs docsSearch = indexSearcher.search(parsedQuery, GlobalVariances.HitSize);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            for (ScoreDoc si : scoreDocs) {
                int docID = si.doc;
                Document document = indexReader.document(docID);
                int datasetID = Integer.parseInt(document.get("dataset_id"));
                if (datasetID <= 311 && !res.contains(DRankScoreList.get(datasetID - 1))) {
                    res.add(DRankScoreList.get(datasetID - 1));
                }
            }
            res.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static List<Pair<Integer, Double>> PageRankRankingList(String query) {
        init();
        query=query.replaceAll("\\p{P}"," ");
        List<Pair<Integer, Double>> PageRankScoreList = new ArrayList<>();
        List<Pair<Integer, Double>> res = new ArrayList<>();
        try {
            String str;
            int t = 1;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(GlobalVariances.qualityRankingResultPath + "PageRank.out"));
            while ((str = bufferedReader.readLine()) != null) {
                PageRankScoreList.add(new Pair<Integer, Double>(t, Double.parseDouble(str)));
            }
            bufferedReader.close();
            String[] fields = GlobalVariances.queryFields;
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            Query parsedQuery = queryParser.parse(query);
            TopDocs docsSearch = indexSearcher.search(parsedQuery, GlobalVariances.HitSize);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            for (ScoreDoc si : scoreDocs) {
                int docID = si.doc;
                Document document = indexReader.document(docID);
                int datasetID = Integer.parseInt(document.get("dataset_id"));
                if (datasetID <= 311 && !res.contains(PageRankScoreList.get(datasetID - 1))) {
                    res.add(PageRankScoreList.get(datasetID - 1));
                }
            }
            res.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public static List<Pair<Integer, Double>> DINGRankingList(String query) {
        init();
        query=query.replaceAll("\\p{P}"," ");
        List<Pair<Integer, Double>> DINGScoreList = new ArrayList<>();
        List<Pair<Integer, Double>> res = new ArrayList<>();
        try {
            String str;
            int t = 1;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(GlobalVariances.qualityRankingResultPath + "DING.out"));
            while ((str = bufferedReader.readLine()) != null) {
                DINGScoreList.add(new Pair<Integer, Double>(t, Double.parseDouble(str)));
            }
            bufferedReader.close();
            String[] fields = GlobalVariances.queryFields;
            Analyzer analyzer = new EnglishAnalyzer();
            QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
            Query parsedQuery = queryParser.parse(query);
            TopDocs docsSearch = indexSearcher.search(parsedQuery, GlobalVariances.HitSize);
            ScoreDoc[] scoreDocs = docsSearch.scoreDocs;
            for (ScoreDoc si : scoreDocs) {
                int docID = si.doc;
                Document document = indexReader.document(docID);
                int datasetID = Integer.parseInt(document.get("dataset_id"));
                if (datasetID <= 311 && !res.contains(DINGScoreList.get(datasetID - 1))) {
                    res.add(DINGScoreList.get(datasetID - 1));
                }
            }
            res.sort((o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }
}