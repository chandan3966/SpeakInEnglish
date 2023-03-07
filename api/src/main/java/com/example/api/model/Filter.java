package com.example.api.model;

import com.google.firebase.firestore.IgnoreExtraProperties;
import com.google.gson.Gson;

@IgnoreExtraProperties
public class Filter {
    public String gender = "";
    public String level = "";
    public String selfGender = "";
    public String selfLevel = "";
    public String id = "";

    public Filter() {
    }

    public Filter(String gender, String level, String selfGender, String selfLevel, String id) {
        this.gender = gender;
        this.level = level;
        this.selfGender = selfGender;
        this.selfLevel = selfLevel;
        this.id = id;
    }

    public String getSelfGender() {
        return selfGender;
    }

    public void setSelfGender(String selfGender) {
        this.selfGender = selfGender;
    }

    public String getSelfLevel() {
        return selfLevel;
    }

    public void setSelfLevel(String selfLevel) {
        this.selfLevel = selfLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String   toString() {
        return "Filter{" +
                "gender='" + gender + '\'' +
                ", level='" + level + '\'' +
                ", selfGender='" + selfGender + '\'' +
                ", selfLevel='" + selfLevel + '\'' +
                '}';
    }

    public String toJsonString(User user){
        Gson gson = new Gson();
        String json = gson.toJson(user);
        return json;
    }
}
