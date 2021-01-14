package com.amavidato.pubevents.model;

import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;

public class Rating implements ModelObj{
    private String user;
    private String email;
    private int value;
    public static final int MIN_VALUE = 1;
    public static final int MAX_VALUE = 5;
    private String comment;

    public Rating(){
        user = "Anonymous";
        email = null;
        value = MIN_VALUE;
        comment = null;
    }

    public Rating(String user, String email, int value){
        this.user = user;
        this.email = email;
        setValue(value);
        this.comment = null;
    }

    public Rating(String user, String email, int value, String comment){
        this.user = user;
        this.email = email;
        setValue(value);
        this.comment = comment;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        if(value < MIN_VALUE){
            this.value = MIN_VALUE;
        }else if (value > MAX_VALUE){
            this.value = MAX_VALUE;
        }else{
            this.value = value;
        }
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static Rating decodeStrToObj(String obj){
        return new Gson().fromJson(obj, Rating.class);
    }
}
