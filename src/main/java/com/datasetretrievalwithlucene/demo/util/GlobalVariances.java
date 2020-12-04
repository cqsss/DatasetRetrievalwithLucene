package com.datasetretrievalwithlucene.demo.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.omg.PortableInterceptor.INACTIVE;

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

    // 分析器
    public static Analyzer globeAnalyzer = new EnglishAnalyzer();

    //SQL常数
    public static Integer maxListNumber = 100000;
}
