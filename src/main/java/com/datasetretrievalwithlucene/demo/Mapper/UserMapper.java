package com.datasetretrievalwithlucene.demo.Mapper;

import com.datasetretrievalwithlucene.demo.Bean.User;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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
}
