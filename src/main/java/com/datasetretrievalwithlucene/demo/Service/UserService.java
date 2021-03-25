package com.datasetretrievalwithlucene.demo.Service;

import com.datasetretrievalwithlucene.demo.Bean.User;
import com.datasetretrievalwithlucene.demo.Mapper.UserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    private final UserMapper userMapper;

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public List<User> getAll() {
        return userMapper.getAll();
    }

    public User getByUsername(String username) {
        return userMapper.getByUsername(username);
    }

    public boolean searchUser(String username) {
        if (userMapper.getByUsername(username) != null) {
            return true;
        } else {
            return false;
        }
    }

    public int getIdByUsername(String username) {
        return userMapper.getIdByUsername(username);
    }

    public void updateLastIdByUserId(int user_id, int last_annotation_id) {
        userMapper.updateLastIdByUserId(user_id, last_annotation_id);
    }
}
