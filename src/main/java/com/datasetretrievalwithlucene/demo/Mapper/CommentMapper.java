package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.Comment;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentMapper {
    @Select("select * from comment")
    List<Comment> getAll();

    @Insert("insert into comment(comment_id,dataset_id,user_id,user_name,text,comment_time) values(#{comment_id},#{dataset_id},#{user_id},#{user_name},#{text},#{comment_time})")
    void insertComment(Comment comment);

    @Select("select * from comment where dataset_id=#{dataset_id}")
    List<Comment> getCommentsByDatasetId(int dataset_id);
}
