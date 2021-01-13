package com.amavidato.pubevents.utility.db;

public class DBManager {
    private static final String TAG = DBManager.class.getSimpleName();

    public static class CollectionsPaths {
        public static final String USERS = "users";
        public static class UserFields {
            public static final String USERNAME = "username";
            public static final String EMAIL = "email";
            public static final String FOLLOWED_PUBS = "followed_pubs";

            public static class FollowedPubsFields {
            }

            public static final String ACQUIRED_EVENTS = "acquired_events";


            public static class AcquiredEventsFields {

                public static final String TICKETS_BOUGHT = "tickets_bought";
            }
        }
        public static final String EVENTS = "events";
        public static class EventFields{
            public static final String NAME = "name";
            public static final String PUB = "pub";
            public static final String DATE = "date";
            public static final String MAX_CAPACITY = "max_capacity";
            public static final String PRICE = "price";
            public static final String RESERVED_SEATS = "reserved_seats";
            public static final String TYPE = "type";
        }

        public static final String PUBS = "pubs";
            public static class PubFields{
                public static final String NAME = "name";
                public static final String GEOLOCATION = "geolocation";
                public static final String CITY = "city";
            public static final String AVG_AGE = "avg_age";
            public static final String PRICE = "price";
            public static final String OVERALL_RATING = "rating";
            public static final String RATINGS = "ratings";
            public static class Ratings{
                public static final String RATING = "rating";
                public static final String USER = "user";
                public static final String COMMENT = "comment";
                public static final String EMAIL = "email";
            }
        }
        public static final String CITY = "cities";
        public static class CityFields{
            public static final String NAME = "name";
            public static final String province = "province";
        }
        public static final String PROVINCE = "provinces";
        public static class ProvinceFields{
            public static final String NAME = "name";
            public static final String SHORT_NAME = "short_name";
            public static final String REGION = "region";
        }
        public static final String REGIONS = "regions";
        public static class RegionFields{
            public static final String NAME = "name";
        }
    }
}
