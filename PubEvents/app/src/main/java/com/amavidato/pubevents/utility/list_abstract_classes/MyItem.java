package com.amavidato.pubevents.utility.list_abstract_classes;

import com.amavidato.pubevents.model.ModelObj;
import com.google.gson.Gson;

public abstract class MyItem {
    public final String id;
    public final ModelObj object;

    public MyItem(String id, ModelObj object) {
        this.id = id;
        this.object = object;
    }

    @Override
    public String toString() {
        return  new Gson().toJson(this);
    }

    public abstract MyItem decodeStrToObj(String obj);
}
