package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.QualityRanking;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class DRankTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    public void testDRank() {
        try {
            QualityRanking.DRank("content", jdbcTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
