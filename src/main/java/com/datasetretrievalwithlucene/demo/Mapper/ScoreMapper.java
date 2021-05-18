package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.Score;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author Lenovo
 * @date 2021/5/17
 */
@Repository
public interface ScoreMapper {
    @Select("select * from score")
    List<Score> getAll();

    @Select("select * from score where user_id=#{user_id} and dataset_id=#{dataset_id}")
    Score getScore(@Param("user_id") int user_id, @Param("dataset_id") int dataset_id);

    @Insert("insert into score(user_id,dataset_id,score_num) values(#{user_id},#{dataset_id},#{score_num})")
    void insertScore(Score score);

    @Update("update score set score_num=#{score_num} where score_id=#{score_id}")
    void updateScoreById(@Param("score_id") int score_id, @Param("score_num") int score_num);

    @Select("select score_num from score where dataset_id=#{dataset_id}")
    List<Integer> getScoreListByDatasetId(@Param("dataset_id") int dataset_id);
}
