package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserMapper {

    @Select("select * from user")
    List<User> getAll();

    @Select("select * from user where username=#{username}")
    User getByUsername(@Param("username") String username);

    @Select("select user_id from user where username=#{username}")
    int getIdByUsername(@Param("username") String username);

    @Update("update user set last_annotation_id=#{last_annotation_id} where user_id=#{user_id}")
    void updateLastIdByUserId(@Param("user_id") int user_id, @Param("last_annotation_id") int last_annotation_id);

    @Select("select count(*) from user")
    int getUserCount();
}
