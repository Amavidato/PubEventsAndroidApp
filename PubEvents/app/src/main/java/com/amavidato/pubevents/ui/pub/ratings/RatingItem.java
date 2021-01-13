package com.amavidato.pubevents.ui.pub.ratings;

import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.model.Rating;
import com.amavidato.pubevents.ui.findpub.list.PubItem;
import com.google.gson.Gson;

public class RatingItem {
    public final Rating rating;

    public RatingItem(Rating rating) {
        this.rating = rating;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static PubItem decodeStrToObj(String obj){
        return new Gson().fromJson(obj, PubItem.class);
    }

}
