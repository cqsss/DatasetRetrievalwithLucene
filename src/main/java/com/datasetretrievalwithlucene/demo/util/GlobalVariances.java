package com.datasetretrievalwithlucene.demo.util;

import com.alibaba.fastjson.JSONObject;
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
    public static Integer HitSize = 500;
    public static Integer FSDMUWindowSize = 8;

    // 分析器
    public static Analyzer globeAnalyzer = new EnglishAnalyzer();

    // SQL常数
    public static Integer maxListNumber = 100000;

    // 字段权重
    public static JSONObject FSDMBoostWeights = null;
    public static JSONObject getFSDMBoostWeights() { if(null == FSDMBoostWeights) FSDMBoostWeights = Statistics.readJson("src/main/resources/json/FSDMBoostWeights.json"); return FSDMBoostWeights; }
    public static double[] BM25BoostWeights = {1.0, 0.8, 0.5, 0.4};
    public static String[] BM25Fields = {"title", "notes", "class", "property"};
    public static double[] TFIDFBoostWeights = {1.0, 0.8, 0.5, 0.4};
    public static String[] TFIDFFields = {"title", "notes", "class", "property"};


    // 实验参数
    public static Integer[] queryPoolSize = {5, 10, 20, 50, 100};
    public static Integer[] metricsK = {5, 10, 15, 20};
    public static String testQueriesPath = "src/main/resources/in/TestQueries.in";
    public static String poolingQueriesPath = "src/main/resources/in/PoolingQueries.in";
    public static String GoogleQueriesPath = "src/main/resources/in/GoogleRelatedQueries.in";
    public static String DevelopmentSetQueriesPath = "src/main/resources/in/DevelopmentSetQueries.in";
    public static String poolSizePath = "src/main/resources/out/poolsize_new_";
    public static String qualityRankingResultPath = "src/main/resources/out/result_";
    public static String[] queryFields = {"title", "notes", "class", "property"};
    public static String[] methodList = {"BM25_T", "BM25_N", "BM25_C", "BM25_P", "BM25_TN", "BM25_CP", "BM25_ALL",
            "TFIDF_T", "TFIDF_N", "TFIDF_C", "TFIDF_P", "TFIDF_TN", "TFIDF_CP", "TFIDF_ALL",
            "FSDM_T", "FSDM_N", "FSDM_C", "FSDM_P", "FSDM_TN", "FSDM_CP", "FSDM_ALL", "DPR"};
    public static int annotatorPerPair = 3;

    // DPR
    public static String model_file = "model_file=D:\\DPR\\dpr\\downloads\\checkpoint\\retriever\\single-adv-hn\\nq\\bert-base-encoder.cp";
    public static String qa_dataset = "qa_dataset=local_datasets_with_triple";
    public static String ctx_datatsets = "ctx_datatsets=[datasets_with_triple]";
    public static String encoded_ctx_files = "encoded_ctx_files=[D:\\DPR\\dpr\\downloads\\data\\local_test\\datasets_with_triple_out_0]";
    public static String out_file = "out_file=D:\\DPR\\dpr\\downloads\\data\\local_test\\datasets_with_triple_result";
    public static String out_file_path = "D:\\DPR\\dpr\\downloads\\data\\local_test\\datasets_with_triple_result";
    public static String python_interpreter = "D:\\anaconda3\\envs\\DPR\\python.exe";
    public static String dense_retriever = "D:\\DPR\\dense_retriever.py";
    public static String query_tsv = "D:\\DPR\\dpr\\downloads\\data\\local_test\\datasets_with_triple_query.tsv";

    // 前端参数
    public static int numOfDatasetsPerPage = 10;
    public static int datasetIDGap = 221261;
    public static String detailPageURL = "http://114.212.83.17:8080/search?keyword=";
}
