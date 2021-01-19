package com.amavidato.pubevents.ui.pubs.list;

import com.amavidato.pubevents.utility.general_list_fragment.SortOptions;

public class SortOptionsPub implements SortOptions {

    public static final String NAME_ASC = "NAME_ASC";
    public static final String NAME_DESC = "NAME_DESC";
    public static final String CITY_NAME_ASC = "CITY_NAME_ASC";
    public static final String CITY_NAME_DESC = "CITY_NAME_DESC";
    public static final String OVERALL_RATE_ASC = "OVERALL_RATE_ASC";
    public static final String OVERALL_RATE_DESC = "OVERALL_RATE_DESC";
    public static final String PRICE_ASC = "PRICE_ASC";
    public static final String PRICE_DESC = "PRICE_DESC";
    public static final String AVG_AGE_ASC = "AVG_AGE_ASC";
    public static final String AVG_AGE_DESC = "AVG_AGE_DESC";

    @Override
    public String valueOf(String value) {
        switch (value){
            case NAME_ASC:
                return NAME_ASC;
            case NAME_DESC:
                return NAME_DESC;
            case CITY_NAME_ASC:
                return CITY_NAME_ASC;
            case CITY_NAME_DESC:
                return CITY_NAME_DESC;
            case PROXIMITY_NEAREST:
                return PROXIMITY_NEAREST;
            case PROXIMITY_FURTHEST:
                return PROXIMITY_FURTHEST;
            case OVERALL_RATE_ASC:
                return OVERALL_RATE_ASC;
            case OVERALL_RATE_DESC:
                return OVERALL_RATE_DESC;
            case PRICE_ASC:
                return PRICE_ASC;
            case PRICE_DESC:
                return PRICE_DESC;
            case AVG_AGE_ASC:
                return AVG_AGE_ASC;
            case AVG_AGE_DESC:
                return AVG_AGE_DESC;
            default:
                return null;
        }
    }
}
