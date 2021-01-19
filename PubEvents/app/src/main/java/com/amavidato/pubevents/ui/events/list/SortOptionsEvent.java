package com.amavidato.pubevents.ui.events.list;

public class SortOptionsEvent implements com.amavidato.pubevents.utility.general_list_fragment.SortOptions {

    public static final String NAME_ASC = "NAME_ASC";
    public static final String NAME_DESC = "NAME_DESC";
    public static final String PUB_NAME_ASC = "PUB_NAME_ASC";
    public static final String DATE_NEAREST = "DATE_NEAREST";
    public static final String DATE_FURTHEST = "DATE_FURTHEST";
    public static final String PUB_NAME_DESC = "PUB_NAME_DESC";
    public static final String PRICE_ASC = "PRICE_ASC";
    public static final String PRICE_DESC = "PRICE_DESC";

    @Override
    public String valueOf(String value) {
        switch (value){
            case NAME_ASC:
                return NAME_ASC;
            case NAME_DESC:
                return NAME_DESC;
            case DATE_NEAREST:
                return DATE_NEAREST;
            case DATE_FURTHEST:
                return DATE_FURTHEST;
            case PUB_NAME_ASC:
                return PUB_NAME_ASC;
            case PUB_NAME_DESC:
                return PUB_NAME_DESC;
            case PROXIMITY_NEAREST:
                return PROXIMITY_NEAREST;
            case PROXIMITY_FURTHEST:
                return PROXIMITY_FURTHEST;
            case PRICE_ASC:
                return PRICE_ASC;
            case PRICE_DESC:
                return PRICE_DESC;
            default:
                return null;
        }
    }
}
