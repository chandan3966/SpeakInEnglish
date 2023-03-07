package com.example.api.model;

import com.google.firebase.firestore.IgnoreExtraProperties;

import java.util.ArrayList;

@IgnoreExtraProperties
public class Grammar {
    String grammar_key;

    String grammar_question;

    public Grammar() {

    }

    @Override
    public String toString() {
        return "Grammar{" +
                "grammar_key='" + grammar_key + '\'' +
                ", grammar_question='" + grammar_question + '\'' +
                '}';
    }

    public String getGrammar_key() {
        return grammar_key;
    }

    public void setGrammar_key(String grammar_key) {
        this.grammar_key = grammar_key;
    }

    public String getGrammar_question() {
        return grammar_question;
    }

    public void setGrammar_question(String grammar_question) {
        this.grammar_question = grammar_question;
    }

    public Grammar(String grammar_key, String grammar_question) {
        this.grammar_key = grammar_key;
        this.grammar_question = grammar_question;
    }
}
