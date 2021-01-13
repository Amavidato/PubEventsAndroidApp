package com.amavidato.pubevents.utility.general_list_fragment;

public interface SortOptions {
    public static final String CLOSEST_TO_FARTHEST = "CLOSEST_TO_FARTHEST";
    public static final String FARTHEST_TO_CLOSEST = "FARTHEST_TO_CLOSEST";

    public abstract String valueOf(String value);

}
