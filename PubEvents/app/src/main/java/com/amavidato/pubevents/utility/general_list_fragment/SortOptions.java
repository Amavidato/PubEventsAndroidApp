package com.amavidato.pubevents.utility.general_list_fragment;

public interface SortOptions {
    public static final String PROXIMITY_NEAREST = "PROXIMITY_NEAREST";
    public static final String PROXIMITY_FURTHEST = "PROXIMITY_FURTHEST";

    public abstract String valueOf(String value);

}
