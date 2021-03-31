package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import javafx.util.Pair;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.BytesRef;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

@SpringBootTest
public class JSONTest {
    @Test
    public void testJSON(){
        for (Map.Entry jsonObject : GlobalVariances.getFSDMBoostWeights().entrySet()) {
            System.out.println("key: " + jsonObject.getKey());
            System.out.println("value: " + jsonObject.getValue());
        }
    }
}