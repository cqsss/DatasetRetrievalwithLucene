package com.datasetretrievalwithlucene.demo;

import javafx.util.Pair;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Paths;
import java.util.*;

@SpringBootTest
public class TermFreqTest {

    private static final Logger logger= LoggerFactory.getLogger(LoggerTest.class);

    private Map<String, Integer> termMap = new HashMap<>();
    public void readTerms(String fileName) {
        List<Pair<String, Integer>> arr = new ArrayList<>();
        try {
            String str;
            String[] tmps;
            BufferedReader in = new BufferedReader(new FileReader(fileName));
            while ((str = in.readLine()) != null) {
                tmps = str.split(" ");
                Integer tmp = 0;
                if (termMap.containsKey(tmps[0])) {
                    tmp = termMap.get(tmps[0]);
                }
                termMap.put(tmps[0], tmp + Integer.parseInt(tmps[1]));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Test
    public void testTermFreq() {
        try {
            readTerms("indexProcessor/terms_content.in");
            readTerms("indexProcessor/terms_notes.in");
            readTerms("indexProcessor/terms_title.in");
            List<Map.Entry<String, Integer>> arr = new ArrayList<>(termMap.entrySet());
            Collections.sort(arr, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            Integer cnt = 0;

            for (Map.Entry<String, Integer> i : arr) {
                System.out.println(i.getKey() + "\t" + i.getValue());
                cnt ++;
                if (cnt == 100) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
