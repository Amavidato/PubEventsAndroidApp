package com.amavidato.pubevents.ui.findpub.list;

import com.amavidato.pubevents.model.Pub;
import com.google.gson.Gson;

public class PubItem {
    public final String id;
    public final Pub pub;

    /*public PubItem(Pub pub) {
        //this.id = id;
        this.pub = pub;
    }*/
    public PubItem(String id,Pub pub) {
        this.id = id;
        this.pub = pub;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public static PubItem decodeStrToObj(String obj){
        return new Gson().fromJson(obj, PubItem.class);
    }

}
