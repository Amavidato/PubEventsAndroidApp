package com.amavidato.pubevents.ui.pubs.list;

import com.amavidato.pubevents.utility.list_abstract_classes.FilterOptions;

public class FilterOptionsPub implements FilterOptions {

    public static final String NAME = "NAME";
    public static final String CITY = "CITY";

    @Override
    public String valueOf(String value) {
        switch (value){
            case NAME:
                return NAME;
            case CITY:
                return CITY;
            default:
                return null;
        }
    }
}
