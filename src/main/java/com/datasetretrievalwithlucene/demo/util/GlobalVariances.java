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
    public static Integer HitSize = 500;
    public static Integer FSDMUWindowSize = 8;

    // 分析器
    public static Analyzer globeAnalyzer = new EnglishAnalyzer();

    //SQL常数
    public static Integer maxListNumber = 100000;

    public static JSONObject FSDMBoostWeights = null;
    public static JSONObject getFSDMBoostWeights() { if(null == FSDMBoostWeights) FSDMBoostWeights = Statistics.readJson("src/main/resources/json/FSDMBoostWeights.json"); return FSDMBoostWeights; }

    public static double[] BM25BoostWeights = {1.0,1.0,1.0,1.0};
    public static double[] TFIDFBoostWeights = {1.0,1.0,1.0,1.0};


    //实验参数
    public static Integer[] queryPoolSize = {5, 10, 20, 50, 100};
    public static String testQueriesPath = "src/main/resources/in/TestQueries.in";
    public static String poolingQueriesPath = "src/main/resources/in/PoolingQueries.in";
    public static String GoogleQueriesPath = "src/main/resources/in/title_notesGoogleQueries 2021.3.25.in";
    public static String[] queryFields = {"title", "notes", "class", "property"};

    //DPR
    public static String model_file = "model_file=F:\\DPR\\dpr\\downloads\\checkpoint\\retriever\\single-adv-hn\\nq\\bert-base-encoder.cp";
    public static String qa_dataset = "qa_dataset=local_datasets_with_triple";
    public static String ctx_datatsets = "ctx_datatsets=[datasets_with_triple]";
    public static String encoded_ctx_files = "encoded_ctx_files=[F:\\DPR\\dpr\\downloads\\data\\local_test\\datasets_with_triple_out_0]";
    public static String out_file = "out_file=F:\\DPR\\dpr\\downloads\\data\\local_test\\datasets_with_triple_result";
    public static String out_file_path = "F:\\DPR\\dpr\\downloads\\data\\local_test\\datasets_with_triple_result";
    public static String python_interpreter = "E:\\Anaconda\\envs\\DPR\\python.exe";
    public static String dense_retriever = "F:\\DPR\\dense_retriever.py";
    public static String query_tsv = "F:\\DPR\\dpr\\downloads\\data\\local_test\\datasets_with_triple_query.tsv";

    //前端参数
    public static int numOfDatasetsPerPage = 10;
}
