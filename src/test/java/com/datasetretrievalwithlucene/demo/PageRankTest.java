package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.util.PageRank;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
public class PageRankTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Test
    public void testPageRank() {
        try {
            PageRank.IterativePageRank("content", jdbcTemplate);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}