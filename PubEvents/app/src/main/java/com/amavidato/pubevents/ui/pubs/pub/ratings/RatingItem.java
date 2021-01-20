package com.amavidato.pubevents.ui.pubs.pub.ratings;

import com.amavidato.pubevents.model.Rating;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.google.gson.Gson;

public class RatingItem extends MyItem {

    public RatingItem(String id, Rating rating) {
        super(id,rating);
    }

    public RatingItem decodeStrToObj(String obj){
        return new Gson().fromJson(obj, RatingItem.class);
    }

}
