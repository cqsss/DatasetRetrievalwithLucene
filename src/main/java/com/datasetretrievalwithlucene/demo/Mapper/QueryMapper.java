package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.Query;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QueryMapper {
    @Select("select * from query")
    List<Query> getAll();

    @Select("select * from query where query_id=#{query_id}")
    Query getQueryById(@Param("query_id") int query_id);

}
