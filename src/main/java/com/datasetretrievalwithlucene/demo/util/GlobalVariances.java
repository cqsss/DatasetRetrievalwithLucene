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
    public static String store_Dir = "D:/db_index/";
    public static String index_Dir = "D:/db_index/";
    public static Integer commit_limit = 10;
    public static Integer HitSize = 500;
    public static Integer FSDMUWindowSize = 8;

    // 分析器
    public static Analyzer globeAnalyzer = new EnglishAnalyzer();

    //SQL常数
    public static Integer maxListNumber = 100000;

    public static JSONObject boostWeights = null;
    public static JSONObject getBoostWeights() { if(null == boostWeights) boostWeights = Statistics.readJson("src/main/resources/json/ParseMatch_boost.json"); return boostWeights; }

    public static Integer[] queryPoolSize = {5, 10, 20, 50, 100};
    public static String queriesPath = "src/main/resources/in/queries.in";
    public static String GoogleQueriesPath = "src/main/resources/in/title_notesGoogleQueries 2021.3.25.in";
    public static String[] queryFields = {"title_notes", "class_property"};

    //DPR
    public static String model_file = "model_file=F:\\DPR\\dpr\\downloads\\checkpoint\\retriever\\single-adv-hn\\nq\\bert-base-encoder.cp";
    public static String qa_dataset = "qa_dataset=local_metadata";
    public static String ctx_datatsets = "ctx_datatsets=[metadata0,metadata1,metadata2,metadata3,metadata4,metadata5]";
    public static String encoded_ctx_files = "encoded_ctx_files=[F:\\DPR\\dpr\\downloads\\data\\local_test\\metadata_0_out_0,F:\\DPR\\dpr\\downloads\\data\\local_test\\metadata_1_out_0,F:\\DPR\\dpr\\downloads\\data\\local_test\\metadata_2_out_0,F:\\DPR\\dpr\\downloads\\data\\local_test\\metadata_3_out_0,F:\\DPR\\dpr\\downloads\\data\\local_test\\metadata_4_out_0,F:\\DPR\\dpr\\downloads\\data\\local_test\\metadata_5_out_0]";
    public static String out_file = "out_file=F:\\DPR\\dpr\\downloads\\data\\local_test\\metadata_result";
    public static String out_file_path = "F:\\DPR\\dpr\\downloads\\data\\local_test\\metadata_result";
    public static String python_interpreter = "E:\\Anaconda\\envs\\DPR\\python.exe";
    public static String dense_retriever = "F:\\DPR\\dense_retriever.py";
    public static String query_tsv = "F:\\DPR\\dpr\\downloads\\data\\local_test\\metadata_query.tsv";
}
