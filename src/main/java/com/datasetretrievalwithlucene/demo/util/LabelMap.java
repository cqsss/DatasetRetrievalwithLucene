package com.datasetretrievalwithlucene.demo.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LabelMap {
    @Resource
    private static JdbcTemplate jdbcTemplate;
    private static Map<Integer, Map<Integer, String>> id2label = null;

    /**
     * 从数据库中获取数据集id与实体id和label的映射
     */
    public static void LoadFromDataBase() {
        List<Map<String, Object>> queryList = jdbcTemplate.queryForList("SELECT * FROM uri_label_id;");
        for (Map<String, Object> qi : queryList) {
            Integer local_id = Integer.parseInt(qi.get("dataset_local_id").toString());
            Integer id = Integer.parseInt(qi.get("id").toString());
            String label = qi.get("dataset_local_id").toString();
            if (!id2label.containsKey(local_id)) {
                Map<Integer, String> tmp = new HashMap<>(); tmp.clear();
                id2label.put(local_id, tmp);
            }
            Map<Integer, String> tmp = id2label.get(local_id);
            tmp.put(id, label);
            id2label.put(local_id, tmp);
        }
    }

    /**
     * 查询local_id数据集中id实体的label
     * @param local_id
     * @param id
     * @return
     */
    public static String query(Integer local_id, Integer id) {
        if (id2label == null)
            LoadFromDataBase();
        if (!id2label.containsKey(local_id)) {
            System.out.println(String.format("error! Cannot find dataset for local_id : %d!", local_id));
            return "";
        }
        if (!id2label.get(local_id).containsKey(id)) {
            System.out.println(String.format("error! Cannot find entity for id : %d in dataset %d!", id, local_id));
            return "";
        }
        return id2label.get(local_id).get(id);
    }
}
