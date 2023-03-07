package com.example.api.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.gson.Gson;

@IgnoreExtraProperties
public class User {
    public String name = "";
    public String gender = "";
    public String ownlevel = "";
    public int level = 1;
    public String id = "";
    public String avatar = "";

    public User() {
    }

    public User(String name, String gender, String ownlevel, String id, String avatar) {
        this.name = name;
        this.gender = gender;
        this.ownlevel = ownlevel;
        this.id = id;
        this.avatar = avatar;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getOwnlevel() {
        return ownlevel;
    }

    public void setOwnlevel(String ownlevel) {
        this.ownlevel = ownlevel;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", gender='" + gender + '\'' +
                ", ownlevel='" + ownlevel + '\'' +
                ", level=" + level +
                ", id='" + id + '\'' +
                ", avatar='" + avatar + '\'' +
                '}';
    }

    public String toJsonString(User user){
        Gson gson = new Gson();
        String json = gson.toJson(user);
        return json;
    }
}
