package com.datasetretrievalwithlucene.demo.util;

import org.apache.log4j.Logger;
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

    private static Logger logger = Logger.getLogger(LabelMap.class);
    private static Map<Integer, String> id2label = null;

    /**
     * 从数据库中获取数据集id与实体id和label的映射
     */
    public static void LoadFromDataBase(JdbcTemplate jdbcTemplate) {
        Integer entityCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM entity;", Integer.class);
        logger.info("total entity number: " + entityCount);
        for (Integer i = 0; i <= entityCount / GlobalVariances.maxListNumber; i++) {
            List<Map<String, Object>> queryList = jdbcTemplate.queryForList(String.format("SELECT global_id,label FROM entity LIMIT %d,%d;", i * GlobalVariances.maxListNumber, GlobalVariances.maxListNumber));
            for (Map<String, Object> qi : queryList) {
                Integer id = Integer.parseInt(qi.get("global_id").toString());
                Object labelObject = qi.get("label");
                String label = "";
                if (labelObject != null)
                    label = labelObject.toString();
                id2label.put(id, label);
            }
            logger.info("entity id: " + (i * GlobalVariances.maxListNumber + GlobalVariances.maxListNumber));
            logger.info("LoadFromDataBase process: " + ((i.doubleValue() * GlobalVariances.maxListNumber.doubleValue() + GlobalVariances.maxListNumber.doubleValue()) / entityCount.doubleValue()));
        }
        logger.info("Completed LoadFromDataBase!");
    }

    /**
     * 查询local_id数据集中id实体的label
     * @param id
     * @param jdbcTemplate
     * @return
     */
    public static String query(Integer id, JdbcTemplate jdbcTemplate) {
        if (id2label == null) {
            id2label = new HashMap<>();
            LoadFromDataBase(jdbcTemplate);
        }
        if (!id2label.containsKey(id)) {
            logger.error(String.format("error! Cannot find entity for id : %d!", id));
            return "";
        }
        return id2label.get(id);
    }
}
