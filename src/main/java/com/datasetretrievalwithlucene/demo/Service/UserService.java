package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.User;
import com.datasetretrievalwithlucene.demo.Mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }
    public List<User> getAll() {return userMapper.getAll();}
    public User getByUsername(String username) {
        return userMapper.getByUsername(username);
    }
    public boolean searchUser(String username){
        if(userMapper.getByUsername(username) != null) {
            return  true;
        } else {
            return  false;
        }
    }

}
