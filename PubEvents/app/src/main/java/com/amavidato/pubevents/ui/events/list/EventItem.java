package com.amavidato.pubevents.ui.events.list;

import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.google.gson.Gson;

public class EventItem extends MyItem {

    public EventItem(String id, Event event) {
        super(id,event);
    }

    public EventItem decodeStrToObj(String obj){
        return new Gson().fromJson(obj, EventItem.class);
    }

}
