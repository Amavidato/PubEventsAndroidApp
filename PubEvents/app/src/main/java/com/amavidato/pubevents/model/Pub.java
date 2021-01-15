package com.amavidato.pubevents.model;

import com.google.firebase.firestore.GeoPoint;
import com.google.gson.Gson;

public class Pub implements ModelObj{
    private String name;
    private GeoPoint geoLocation;
    private String city;
    private int averageAge;
    private double overallRating;



    public enum Price {HIGH,MEDIUM,LOW};
    private Price price;

    public Pub(){
        name = "EMPTY";
        geoLocation = null;
        city = "EMPTY";
    }

    public Pub(String name, String city){
        this.name = name;
        this.city = city;
        this.geoLocation = null;
    }

    public Pub(String name, String city, GeoPoint geoLocation){
        this.name = name;
        this.city = city;
        this.geoLocation = geoLocation;
    }

    public String getName() {
        return name;
    }

    public GeoPoint getGeoLocation() {
        return geoLocation;
    }

    public String getCity() {
        return city;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setGeoLocation(GeoPoint geoLocation) {
        this.geoLocation = geoLocation;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public int getAverageAge() {
        return averageAge;
    }

    public void setAverageAge(int averageAge) {
        this.averageAge = averageAge;
    }

    public Price getPrice() {
        return price;
    }

    public void setPrice(Price price) {
        this.price = price;
    }

    public double getOverallRating() {return overallRating;}

    public void setOverallRating(double overallRating){this.overallRating = overallRating;}

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static Pub decodeStrToObj(String obj){
        return new Gson().fromJson(obj, Pub.class);
    }
}
