package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import com.datasetretrievalwithlucene.demo.util.Statistics;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class regexTest {
    @Test
    public void testRegEx(){
        try {
            String str = "Supporting materials \"STATISTICAL MECHANICS FOR METABOLIC NETWORKS IN STEADY-STATE GROWTH\"";
            System.out.println(str.replaceAll("\\p{P}", " "));
            System.out.println(Statistics.getTokens(str.replaceAll("\\p{P}", " ")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}