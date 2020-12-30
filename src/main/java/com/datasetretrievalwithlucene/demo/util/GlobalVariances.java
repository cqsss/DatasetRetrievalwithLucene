package com.datasetretrievalwithlucene.demo.util;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPObject;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

/**
 * 存放全局变量
 */
public class GlobalVariances {
    // Index常数
    public static Integer maxEntityNumber = 500;
    public static Integer maxRelationNumber = 1000;

    // 数据存放常数
    public static String store_Dir = "D:/db_index_new/";
    public static String index_Dir = "D:/db_index_new/";
    public static Integer commit_limit = 10;

    // 分析器
    public static Analyzer globeAnalyzer = new EnglishAnalyzer();

    //SQL常数
    public static Integer maxListNumber = 100000;

    public static JSONObject boostWeights = null;
    public static JSONObject getBoostWeights() { if(null == boostWeights) boostWeights = Statistics.readJson("src/main/resources/json/ParseMatch_boost.json"); return boostWeights; }

    public static Integer FSDMUWindowSize = 8;
    public static Integer[] queryPoolSize = {5, 10, 20, 50, 100};
    public static String queriesPath = "src/main/resources/in/queries.in";
    public static String GoogleQueriesPath = "src/main/resources/in/GoogleRelatedQueries.in";
    public static String[] queryFields = {"content", "title", "notes"};
}
