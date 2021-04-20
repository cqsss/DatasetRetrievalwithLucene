package com.datasetretrievalwithlucene.demo;

import com.datasetretrievalwithlucene.demo.Bean.TripleID;
import com.datasetretrievalwithlucene.demo.Service.UserService;
import com.datasetretrievalwithlucene.demo.util.GlobalVariances;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.*;

@SpringBootTest
public class DataBaseTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private UserService userService;
    @Test
    public void testQueryDataBase(){
        List<Map<String, Object>> res = jdbcTemplate.queryForList("SELECT * FROM triple ORDER BY dataset_id;");
        System.out.println(res);
    }
    @Test
    public void testTripleNum() {
        System.out.println("start");
        int tripleCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM triple;", Integer.class);
        int currentID = 1;
        int cnt = 0;
        System.out.println("total: " + tripleCount);
        for (int i = 0; i <= tripleCount / GlobalVariances.maxListNumber; i++) {
            List<Map<String, Object>> res = jdbcTemplate.queryForList(String.format("SELECT dataset_id FROM triple ORDER BY dataset_id LIMIT %d,%d;", i * GlobalVariances.maxListNumber, GlobalVariances.maxListNumber));
            for (Map<String, Object> qi : res) {
                int dataset_id = Integer.parseInt(qi.get("dataset_id").toString());
                if (dataset_id == currentID) {
                    cnt++;
                } else {
                    jdbcTemplate.execute("UPDATE metadata SET num_triples=" + cnt + " WHERE dataset_id="+currentID);
                    System.out.println("Completed calculate dataset " + currentID + "/" + tripleCount);
                    cnt = 0;
                    currentID++;
                }
            }
        }
    }
}
