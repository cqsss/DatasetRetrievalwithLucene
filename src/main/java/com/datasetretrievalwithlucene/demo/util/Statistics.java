package com.datasetretrievalwithlucene.demo.util;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class Statistics {
    public static List<String> getTokens(String S) throws IOException {
        List<String> res = new ArrayList<>(); res.clear();
        Analyzer analyzer = GlobalVariances.globeAnalyzer;
        //第一个参数fieldName没有实际用处
        TokenStream tokenStream = analyzer.tokenStream("content", new StringReader(S));
        tokenStream.reset();
        CharTermAttribute charTerm = tokenStream.addAttribute(CharTermAttribute.class);
        while (tokenStream.incrementToken()) {
            res.add(charTerm.toString());
        }
        tokenStream.close();
        System.out.println(res);
        return res;
    }
}
