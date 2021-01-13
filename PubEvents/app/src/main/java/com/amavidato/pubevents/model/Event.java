package com.amavidato.pubevents.model;

import java.util.Date;

public class Event {
    private String name;
    private Date date;
    private String pub;
    private double price;
    private int max_capacity;
    private int reserved_seats;
    public enum EventType { NONE, QUIZ, MOVIE_NIGHT, LIVE_MUSIC, KARAOKE, SPEED_DATING}
    private EventType type;

    public Event() {
        this.name = this.pub = "";
        this.price = max_capacity = reserved_seats = 0;
        this.type = EventType.NONE;
    }

    public Event(String name, Date date, String pub, float price, int max_capacity, int reserved_seats, EventType type) {
        this.name = name;
        this.date = date;
        this.pub = pub;
        this.price = price;
        this.max_capacity = max_capacity;
        this.reserved_seats = reserved_seats;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPub() {
        return pub;
    }

    public void setPub(String pub) {
        this.pub = pub;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if(price>=0){
            this.price = price;
        }
    }

    public int getMax_capacity() {
        return max_capacity;
    }

    public void setMax_capacity(int max_capacity) {
        if(max_capacity>=0){
            this.max_capacity = max_capacity;
        }
    }

    public int getReserved_seats() {
        return reserved_seats;
    }

    public void setReserved_seats(int reserved_seats) {
        if(reserved_seats<=max_capacity){
            this.reserved_seats = reserved_seats;
        }
    }

    public int getAvailableSeats(){
        return max_capacity - reserved_seats;
    }

    public EventType getType() {
        return type;
    }

    public void setType(EventType type) {
        this.type = type;
    }

}
