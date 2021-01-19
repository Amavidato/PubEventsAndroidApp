package com.amavidato.pubevents.ui.events.list;

import android.util.Log;

import com.amavidato.pubevents.utility.general_list_fragment.FilterOptions;

public class FilterOptionsEvent implements FilterOptions {

    public static final String NAME = "NAME";
    public static final String PUB_NAME = "PUB_NAME";
    public static final String CITY_NAME = "CITY_NAME";

    @Override
    public String valueOf(String value) {
        switch (value){
            case NAME:
                return NAME;
            case PUB_NAME:
                return PUB_NAME;
            case CITY_NAME:
                return CITY_NAME;
            default:
                return null;
        }
    }
}
