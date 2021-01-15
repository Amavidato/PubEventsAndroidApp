package com.amavidato.pubevents.ui.events.list;

public class SortOptionsEvent implements com.amavidato.pubevents.utility.general_list_fragment.SortOptions {

    public static final String NAME_ASC = "NAME_ASC";
    public static final String NAME_DESC = "NAME_DESC";
    public static final String PUB_NAME_ASC = "PUB_NAME_ASC";
    public static final String PUB_NAME_DESC = "PUB_NAME_DESC";
    public static final String CITY_NAME_ASC = "CITY_NAME_ASC";
    public static final String CITY_NAME_DESC = "CITY_NAME_DESC";
    public static final String CLOSEST_TO_FARTHEST = "CLOSEST_TO_FARTHEST";
    public static final String FARTHEST_TO_CLOSEST = "FARTHEST_TO_CLOSEST";
    public static final String PRICE_ASC = "PRICE_ASC";
    public static final String PRICE_DESC = "PRICE_DESC";

    @Override
    public String valueOf(String value) {
        switch (value){
            case NAME_ASC:
                return NAME_ASC;
            case NAME_DESC:
                return NAME_DESC;
            case PUB_NAME_ASC:
                return PUB_NAME_ASC;
            case PUB_NAME_DESC:
                return PUB_NAME_DESC;
            case CITY_NAME_ASC:
                return CITY_NAME_ASC;
            case CITY_NAME_DESC:
                return CITY_NAME_DESC;
            case CLOSEST_TO_FARTHEST:
                return CLOSEST_TO_FARTHEST;
            case FARTHEST_TO_CLOSEST:
                return FARTHEST_TO_CLOSEST;
            case PRICE_ASC:
                return PRICE_ASC;
            case PRICE_DESC:
                return PRICE_DESC;
            default:
                return null;
        }
    }
}
