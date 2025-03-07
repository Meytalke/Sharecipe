package com.example.sharecipe.models;
import java.util.ArrayList;
import java.util.List;

public class User {
    private String userName;
    private String password;
    private String phone;
    public User() {
    }
    public User(String userName, String password, String phone) {
        this.userName = userName;
        this.password = password;
        this.phone = phone;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

}
