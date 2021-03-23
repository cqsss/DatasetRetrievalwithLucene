package com.datasetretrievalwithlucene.demo.Bean;

public class User {
    private int userid;
    private String username;
    private String password;
    private int last_annotation_id;

    public int getUserid() {
        return userid;
    }

    public void setUserid(int userid) {
        this.userid = userid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getLast_annotation_id() {
        return last_annotation_id;
    }

    public void setLast_annotation_id(int last_annotation_id) {
        this.last_annotation_id = last_annotation_id;
    }
}
