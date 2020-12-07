package com.datasetretrievalwithlucene.demo;

import org.apache.log4j.Logger;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class LoggerTest {
    private static Logger logger = Logger.getLogger(LoggerTest.class);
    @Test
    public void testLogger(){
        logger.info("Logger Test");
    }
}
