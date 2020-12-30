package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import javafx.util.Pair;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SpringBootTest
public class QueryProcessTest {
    private List<Pair<String, String>> queryList = new ArrayList<>();
    public void readQueries(String fileName) {
        try {
            String str;
            String[] tmps;
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((str = in.readLine()) != null) {
                tmps = str.split("\t");
                String tmp = tmps[1].replaceAll("\\p{P}"," ");
                List<String> tmpList = Statistics.getTokens(tmp);
                if (tmpList.size() > 8) continue;
                if (!tmp.matches("^[A-Za-z0-9 ]+$")) continue;
                queryList.add(new Pair<>(tmps[0], tmps[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testQueryProcess(){
        readQueries(GlobalVariances.GoogleQueriesPath);
        for (Pair<String, String> i : queryList) {
            System.out.println(i.getKey() + ";" + i.getValue());
        }
    }
}
