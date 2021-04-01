package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.QueryData;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryDataMapper {
    @Select("select * from query_data")
    List<QueryData> getAll();

    @Select("select * from query_data where query_data_id=#{query_data_id}")
    QueryData getQueryDataById(@Param("query_data_id") int query_data_id);

    @Select("select max(query_data_id) from query_data")
    int getMaxId();

    @Select("select query_data_id from query_data where query_id=#{query_id} and dataset_id=#{dataset_id}")
    int getQueryDataIdByQueryIdAndDatasetId(@Param("query_id") int query_id, @Param("dataset_id") int dataset_id);
}
