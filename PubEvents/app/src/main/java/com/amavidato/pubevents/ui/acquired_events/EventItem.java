package com.amavidato.pubevents.ui.acquired_events;

import com.amavidato.pubevents.model.Event;
import com.google.gson.Gson;

public class EventItem {
    public final String id;
    public final Event event;

    /*public PubItem(Pub pub) {
        //this.id = id;
        this.pub = pub;
    }*/
    public EventItem(String id, Event event) {
        this.id = id;
        this.event = event;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static EventItem decodeStrToObj(String obj){
        return new Gson().fromJson(obj, EventItem.class);
    }

}
