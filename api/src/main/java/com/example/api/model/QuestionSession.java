package com.example.api.model;

import java.util.ArrayList;

public class QuestionSession {
    String creatorType;
    ArrayList<Long> creatorQns;
    String otherType;
    ArrayList<Long> otherQns;

    public QuestionSession() {
    }

    public QuestionSession(String creatorType, ArrayList<Long> creatorQns, String otherType, ArrayList<Long> otherQns) {
        this.creatorType = creatorType;
        this.creatorQns = creatorQns;
        this.otherType = otherType;
        this.otherQns = otherQns;
    }

    public String getCreatorType() {
        return creatorType;
    }

    public void setCreatorType(String creatorType) {
        this.creatorType = creatorType;
    }

    public ArrayList<Long> getCreatorQns() {
        return creatorQns;
    }

    public void setCreatorQns(ArrayList<Long> creatorQns) {
        this.creatorQns = creatorQns;
    }

    public String getOtherType() {
        return otherType;
    }

    public void setOtherType(String otherType) {
        this.otherType = otherType;
    }

    public ArrayList<Long> getOtherQns() {
        return otherQns;
    }

    public void setOtherQns(ArrayList<Long> otherQns) {
        this.otherQns = otherQns;
    }

    @Override
    public String toString() {
        return "QuestionSession{" +
                "creatorType='" + creatorType + '\'' +
                ", creatorQns=" + creatorQns +
                ", otherType='" + otherType + '\'' +
                ", otherQns=" + otherQns +
                '}';
    }
}
