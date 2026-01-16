package com.muradelmanoglu;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private String fullName;
    private String nickname;
    private String password;
    private String role;

    public User(String fullName, String nickname, String password, String role) {
        this.fullName = fullName;
        this.nickname = nickname.toLowerCase().trim();
        this.password = password;
        this.role = role;
    }

    public String getFullName() { return fullName; }
    public String getNickname() { return nickname; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public void setPassword(String password) { this.password = password; }
}