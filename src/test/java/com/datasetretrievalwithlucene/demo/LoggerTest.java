package com.datasetretrievalwithlucene.demo;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoggerTest {
    private static final Logger logger= LoggerFactory.getLogger(LoggerTest.class);
    @Test
    public void testLogger(){
        logger.info("Logger Test");
    }
}
