package com.datasetretrievalwithlucene.demo.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.MMapDirectory;

import java.io.IOException;
import java.nio.file.Paths;

public class IndexFactory {
    public IndexWriter iwriter = null;
    public Integer commit_cnt = 0;
    public Integer commit_limit = 0;



    /**
     * 初始化IndexWriter
     * @param store_Path
     * @param commit_lim
     * @param analyzer
     */
    public void Init(String store_Path, Integer commit_lim, Analyzer analyzer) {
        try {
            Directory directory = MMapDirectory.open(Paths.get(store_Path));
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            iwriter = new IndexWriter(directory, config);
            commit_limit = commit_lim;
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
