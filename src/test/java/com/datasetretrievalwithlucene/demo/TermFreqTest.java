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
    @Test
    public void testTermFreq() {
        try {
            BufferedReader in = new BufferedReader(new FileReader("indexProcessor/terms_title.in"));
            String str;
            String[] tmp;
            List<Pair<String, Integer>> arr = new ArrayList<>();
            while ((str = in.readLine()) != null) {
                tmp = str.split(" ");
                arr.add(new Pair<>(tmp[0], Integer.parseInt(tmp[1])));
            }
            Collections.sort(arr, new Comparator<Pair<String, Integer>>() {
                @Override
                public int compare(Pair<String, Integer> o1, Pair<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });
            Integer cnt = 0;
            for (Pair<String, Integer> i : arr) {
                System.out.println(i.getKey() + "\t" + i.getValue());
                cnt ++;
                if (cnt == 100) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
