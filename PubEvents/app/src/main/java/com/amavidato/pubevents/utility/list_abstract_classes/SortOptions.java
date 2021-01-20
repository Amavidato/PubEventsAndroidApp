package com.amavidato.pubevents.utility.list_abstract_classes;

public interface SortOptions {
    public static final String PROXIMITY_NEAREST = "PROXIMITY_NEAREST";
    public static final String PROXIMITY_FURTHEST = "PROXIMITY_FURTHEST";

    public abstract String valueOf(String value);

}
