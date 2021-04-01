package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.Annotation;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnnotationMapper {

    @Select("select * from annotation")
    List<Annotation> getAll();

    @Select("select * from annotation where user_id=#{user_id} and query_id=#{query_id} and dataset_id=#{dataset_id}")
    Annotation getAnnotation(@Param("user_id") int user_id, @Param("query_id") int query_id, @Param("dataset_id") int dataset_id);

    @Insert("insert into annotation(user_id,query_id,dataset_id,rating,annotation_time,reason) values(#{user_id},#{query_id},#{dataset_id},#{rating},#{annotation_time},#{reason})")
    void insertAnnotation(Annotation annotation);

    @Update("update annotation set rating=#{rating}, annotation_time=#{annotation_time} where annotation_id=#{annotation_id}")
    void updateRatingById(@Param("annotation_id") int annotation_id, @Param("rating") int rating, @Param("annotation_time") String annotation_time);

    @Select("select rating from annotation where query_id=#{query_id} and dataset_id=#{dataset_id}")
    List<Integer> getRating(@Param("query_id") int query_id, @Param("dataset_id") int dataset_id);

    @Update("update annotation set reason=#{reason} where annotation_id=#{annotation_id}")
    void updateReasonById(@Param("annotation_id") int annotation_id, @Param("reason") String reason);
}
