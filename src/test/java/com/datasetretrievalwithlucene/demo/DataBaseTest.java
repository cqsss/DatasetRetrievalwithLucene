package com.datasetretrievalwithlucene.demo;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;

@SpringBootTest
public class DataBaseTest {
    @Autowired
    JdbcTemplate jdbcTemplate;
    @Test
    public void queryDataBaseTest(){
        List<Map<String, Object>> res = jdbcTemplate.queryForList("SELECT * FROM triple ORDER BY dataset_local_id;");
        System.out.println(res);
    }
}
