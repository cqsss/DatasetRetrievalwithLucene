package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.Query;
import com.datasetretrievalwithlucene.demo.Bean.QueryData;
import org.apache.ibatis.annotations.Insert;
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

    @Select("select count(*) from query")
    int getQueryCount();

    @Insert("insert into query(query_text) values(#{query_text})")
    void insertQueryData(Query query);
}
