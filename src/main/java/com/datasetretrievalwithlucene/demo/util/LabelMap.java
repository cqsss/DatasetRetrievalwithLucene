package com.datasetretrievalwithlucene.demo.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class LabelMap {

    private static final Logger logger = LoggerFactory.getLogger(LabelMap.class);
    private static Map<Integer, String> id2label = null;

    /**
     * 从数据库中获取数据集id与实体id和label的映射
     */
    public static void loadFromDataBase(JdbcTemplate jdbcTemplate) {
        Integer entityCount = jdbcTemplate.queryForObject("SELECT COUNT(1) FROM entity;", Integer.class);
        logger.info("total entity number: " + entityCount);
        for (Integer i = 0; i <= entityCount / GlobalVariances.maxListNumber; i++) {
            List<Map<String, Object>> queryList = jdbcTemplate.queryForList(String.format("SELECT global_id,label,is_literal FROM entity LIMIT %d,%d;", i * GlobalVariances.maxListNumber, GlobalVariances.maxListNumber));
            for (Map<String, Object> qi : queryList) {
                Integer id = Integer.parseInt(qi.get("global_id").toString());
                boolean is_literal = Boolean.parseBoolean(qi.get("is_literal").toString());

                Object labelObject = qi.get("label");
                String label = "";
                if (labelObject != null)
                    label = labelObject.toString();
                if (!is_literal && label.contains("http"))
                    label = label.substring(label.lastIndexOf("/") + 1);
                id2label.put(id, label);
            }
            logger.info("entity id: " + (i * GlobalVariances.maxListNumber + GlobalVariances.maxListNumber));
            //logger.info("LoadFromDataBase process: " + ((i.doubleValue() * GlobalVariances.maxListNumber.doubleValue() + GlobalVariances.maxListNumber.doubleValue()) / entityCount.doubleValue()));
        }
        logger.info("Completed LoadFromDataBase!");
    }

    /**
     * 查询local_id数据集中id实体的label
     *
     * @param id
     * @param jdbcTemplate
     * @return
     */
    public static String query(Integer id, JdbcTemplate jdbcTemplate) {
        if (id2label == null) {
            id2label = new HashMap<>();
            loadFromDataBase(jdbcTemplate);
        }
        if (!id2label.containsKey(id)) {
            logger.error(String.format("error! Cannot find entity for id : %d!", id));
            return "";
        }
        return id2label.get(id);
    }
}
