package com.amavidato.pubevents.ui.findpub.list;

import com.amavidato.pubevents.model.ModelObj;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.utility.general_list_fragment.MyItem;
import com.google.gson.Gson;

public class PubItem extends MyItem {

    public PubItem(String id,Pub pub) {
        super(id,pub);
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }

    public MyItem decodeStrToObj(String obj){
        return new Gson().fromJson(obj, PubItem.class);
    }

}
