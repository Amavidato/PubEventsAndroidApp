package com.amavidato.pubevents.utility.suggestions;

import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.ui.events.list.EventItem;
import com.amavidato.pubevents.ui.pubs.list.PubItem;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class SuggestionsManager {
    private static final String TAG = SuggestionsManager.class.getSimpleName();

    public enum TypeOfSuggestion{EVENTS, PUBS};

    List<GeneralCandidateSelector> selectorsEvents;
    List<GeneralCandidateSelector> selectorsPubs;
    List<Candidate> candidateEvents;
    List<Candidate> candidatePubs;

    public SuggestionsManager(){
        selectorsEvents = new ArrayList<>();
        selectorsEvents.add(new DistanceCandidateSelector());
        selectorsEvents.add(new TypeCandidateSelection());

        selectorsPubs = new ArrayList<>();
        selectorsPubs.add(new DistanceCandidateSelector());

    }

    public void getSuggestions(OnSuggestionsCompleteListerer listener, TypeOfSuggestion tos){
        createCandidates(listener,tos);
    }

    private void createCandidates(final OnSuggestionsCompleteListerer listener, final TypeOfSuggestion tos){
        Log.d(TAG,"CreateCandidates..."+tos );
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null) {
            String path = tos == TypeOfSuggestion.EVENTS ? DBManager.CollectionsPaths.EVENTS : DBManager.CollectionsPaths.PUBS;
            FirebaseFirestore.getInstance().collection(path).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if(tos == TypeOfSuggestion.EVENTS){
                                    candidateEvents = new ArrayList<>();
                                }else{
                                    candidatePubs = new ArrayList<>();
                                }
                                final int numItems = task.getResult().size();
                                final int[] itemsDone = {0};
                                for (final DocumentSnapshot document : task.getResult()) {
                                    final String itemID = document.getId();
                                    Map<String, Object> data = document.getData();
                                    Log.d(TAG, document.getId() + " => " + data);

                                    if(tos == TypeOfSuggestion.EVENTS){
                                        getEvent(listener, tos, data,document,itemsDone,numItems);
                                    }else{
                                        getPub(listener, tos, data,document,itemsDone,numItems);
                                    }

                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void getPub(final OnSuggestionsCompleteListerer listener, final TypeOfSuggestion tos, Map<String, Object> data, final DocumentSnapshot document, final int[] itemsDone, final int numItems) {
        final Pub pub = new Pub();
        pub.setName((String) data.get(DBManager.CollectionsPaths.PubFields.NAME));
        pub.setGeoLocation((GeoPoint) data.get(DBManager.CollectionsPaths.PubFields.GEOLOCATION));
        pub.setAverageAge(((Long) data.get(DBManager.CollectionsPaths.PubFields.AVG_AGE)).intValue());
        pub.setPrice(Pub.Price.valueOf(((String) data.get(DBManager.CollectionsPaths.PubFields.PRICE)).toUpperCase()));
        Object ratingTemp = data.get(DBManager.CollectionsPaths.PubFields.OVERALL_RATING);
        if (ratingTemp instanceof Double) {
            pub.setOverallRating((Double) ratingTemp);
        } else if (ratingTemp instanceof Long) {
            pub.setOverallRating(((Long) ratingTemp).doubleValue());
        }
        final String cityID = (String) data.get(DBManager.CollectionsPaths.PubFields.CITY);
        //Nested query to retrieve city's information
        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.CITY)
                .document(cityID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> data = task.getResult().getData();
                            Log.d(TAG + "--CITy", task.getResult().getId() + " => " + data);
                            pub.setCity((String) data.get(DBManager.CollectionsPaths.CityFields.NAME));
                            candidatePubs.add(new Candidate(new PubItem(document.getId(),pub),0.0));
                            itemsDone[0]++;
                            if (itemsDone[0] == numItems) {
                                getLastNPubsForUser(listener,tos);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            pub.setCity("ERROR");
                        }
                    }
                });

    }

    private void getEvent(final OnSuggestionsCompleteListerer listener, final TypeOfSuggestion tos, Map<String, Object> data, final DocumentSnapshot document, final int [] itemsDone, final int numItems) {
        final Event event = new Event();
        String stmp = (String) data.get(DBManager.CollectionsPaths.EventFields.NAME);
        event.setName(stmp != null ? stmp : "NULL");

        Timestamp ttmp = (Timestamp) data.get(DBManager.CollectionsPaths.EventFields.DATE);
        event.setDate(ttmp != null ? ttmp.toDate() : null);

        Object ptmp = data.get(DBManager.CollectionsPaths.EventFields.PRICE);
        Double price = null;
        if (ptmp != null) {
            if (ptmp instanceof Double) {
                price = (Double) ptmp;
            } else {//if (eTmp instanceof Long){
                price = ((Long) ptmp).doubleValue();
            }
        }
        event.setPrice(price);

        stmp = (String) data.get(DBManager.CollectionsPaths.EventFields.TYPE);
        stmp = stmp != null ? stmp : "NULL";
        event.setType(Event.EventType.valueOf(stmp.toUpperCase()));

        Long ltmp = (Long) data.get(DBManager.CollectionsPaths.EventFields.RESERVED_SEATS);
        ltmp = ltmp != null ? ltmp : new Long(0);
        event.setReserved_seats(ltmp.intValue());

        ltmp = (Long) data.get(DBManager.CollectionsPaths.EventFields.MAX_CAPACITY);
        event.setMax_capacity(ltmp != null ? ltmp.intValue() : null);

        final String pubID = (String) data.get(DBManager.CollectionsPaths.EventFields.PUB);
        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                .document(pubID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    Map<String, Object> data = task.getResult().getData();
                    Log.d(TAG, task.getResult().getId() + " => " + data);

                    final Pub pub = new Pub();
                    pub.setName((String) data.get(DBManager.CollectionsPaths.PubFields.NAME));
                    pub.setGeoLocation((GeoPoint) data.get(DBManager.CollectionsPaths.PubFields.GEOLOCATION));
                    pub.setAverageAge(((Long) data.get(DBManager.CollectionsPaths.PubFields.AVG_AGE)).intValue());
                    event.setPub(pub);
                    candidateEvents.add(new Candidate(new EventItem(document.getId(), event), 0.0));
                    itemsDone[0]++;
                    if (itemsDone[0] == numItems) {
                        getLastNEventsForUser(listener,tos);
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    private void getLastNPubsForUser(final OnSuggestionsCompleteListerer listener, final TypeOfSuggestion tos){
        Log.d(TAG,"getLastNPubsForUser...");

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String uid = user.getEmail();

            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS).document(uid).collection(DBManager.CollectionsPaths.UserFields.LAST_PUBS).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final List <MyItem> base = new ArrayList<>();
                                final int numItems = task.getResult().size();
                                final int[] itemsDone = {0};
                                for (final DocumentSnapshot document : task.getResult()) {
                                    final String itemID = document.getId();

                                    FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                                            .document(itemID)
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            Map<String, Object> data = task.getResult().getData();
                                            Log.d(TAG, task.getResult().getId() + " => " + data);

                                            final Pub pub = new Pub();
                                            pub.setName((String) data.get(DBManager.CollectionsPaths.PubFields.NAME));
                                            pub.setGeoLocation((GeoPoint) data.get(DBManager.CollectionsPaths.PubFields.GEOLOCATION));
                                            pub.setAverageAge(((Long) data.get(DBManager.CollectionsPaths.PubFields.AVG_AGE)).intValue());
                                            pub.setPrice(Pub.Price.valueOf(((String) data.get(DBManager.CollectionsPaths.PubFields.PRICE)).toUpperCase()));
                                            Object ratingTemp = data.get(DBManager.CollectionsPaths.PubFields.OVERALL_RATING);
                                            if (ratingTemp instanceof Double) {
                                                pub.setOverallRating((Double) ratingTemp);
                                            } else if (ratingTemp instanceof Long) {
                                                pub.setOverallRating(((Long) ratingTemp).doubleValue());
                                            }
                                            final String cityID = (String) data.get(DBManager.CollectionsPaths.PubFields.CITY);
                                            //Nested query to retrieve city's information
                                            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.CITY)
                                                    .document(cityID)
                                                    .get()
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                        @RequiresApi(api = Build.VERSION_CODES.N)
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                            if (task.isSuccessful()) {
                                                                Map<String, Object> data = task.getResult().getData();
                                                                Log.d(TAG + "--CITy", task.getResult().getId() + " => " + data);
                                                                pub.setCity((String) data.get(DBManager.CollectionsPaths.CityFields.NAME));
                                                                base.add(new PubItem(itemID,pub));
                                                                itemsDone[0]++;
                                                                if (itemsDone[0] == numItems) {
                                                                    computeSuggestions(base,listener,tos);
                                                                }
                                                            } else {
                                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                                                pub.setCity("ERROR");
                                                            }
                                                        }
                                                    });
                                        }
                                    });

                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    private void getLastNEventsForUser(final OnSuggestionsCompleteListerer listener, final TypeOfSuggestion tos) {
        Log.d(TAG,"getLastNEventsForUser...");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            String uid = user.getEmail();

            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS).document(uid).collection(DBManager.CollectionsPaths.UserFields.LAST_EVENTS).get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final List <MyItem> base = new ArrayList<>();
                                final int numItems = task.getResult().size();
                                final int[] itemsDone = {0};
                                for (final DocumentSnapshot document : task.getResult()) {
                                    final String itemID = document.getId();

                                    FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.EVENTS)
                                            .document(itemID)
                                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            Map<String, Object> data = task.getResult().getData();
                                            Log.d(TAG, task.getResult().getId() + " => " + data);

                                            final Event event = new Event();
                                            String stmp = (String)data.get(DBManager.CollectionsPaths.EventFields.NAME);
                                            event.setName(stmp != null ? stmp : "NULL");

                                            Timestamp ttmp = (Timestamp) data.get(DBManager.CollectionsPaths.EventFields.DATE);
                                            event.setDate(ttmp != null ? ttmp.toDate() : null);

                                            Object ptmp = data.get(DBManager.CollectionsPaths.EventFields.PRICE);
                                            Double price = null;
                                            if(ptmp != null){
                                                if(ptmp instanceof Double){
                                                    price = (Double)ptmp;
                                                }else {//if (eTmp instanceof Long){
                                                    price = ((Long)ptmp).doubleValue();
                                                }
                                            }
                                            event.setPrice(price);

                                            stmp = (String) data.get(DBManager.CollectionsPaths.EventFields.TYPE);
                                            stmp = stmp != null ? stmp : "NULL";
                                            event.setType(Event.EventType.valueOf(stmp.toUpperCase()));

                                            Long ltmp = (Long)data.get(DBManager.CollectionsPaths.EventFields.RESERVED_SEATS);
                                            ltmp = ltmp != null ? ltmp : new Long(0);
                                            event.setReserved_seats(ltmp.intValue());

                                            ltmp = (Long)data.get(DBManager.CollectionsPaths.EventFields.MAX_CAPACITY);
                                            event.setMax_capacity(ltmp != null ? ltmp.intValue() : null);

                                            final String pubID = (String)data.get(DBManager.CollectionsPaths.EventFields.PUB);
                                            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                                                    .document(pubID)
                                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @RequiresApi(api = Build.VERSION_CODES.N)
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if(task.isSuccessful()){
                                                        Map<String, Object> data = task.getResult().getData();
                                                        Log.d(TAG, task.getResult().getId() + " => " + data);

                                                        final Pub pub = new Pub();
                                                        pub.setName((String) data.get(DBManager.CollectionsPaths.PubFields.NAME));
                                                        pub.setGeoLocation((GeoPoint) data.get(DBManager.CollectionsPaths.PubFields.GEOLOCATION));
                                                        pub.setAverageAge(((Long) data.get(DBManager.CollectionsPaths.PubFields.AVG_AGE)).intValue());
                                                        event.setPub(pub);
                                                        base.add(new EventItem(itemID,event));
                                                        itemsDone[0]++;
                                                        if (itemsDone[0] == numItems) {
                                                            computeSuggestions(base,listener,tos);
                                                        }
                                                    }else {
                                                        Log.d(TAG, "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                        }
                                    });

                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                            }
                        }
                    });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void computeSuggestions(List<MyItem> base, OnSuggestionsCompleteListerer listener, TypeOfSuggestion tos) {
        Log.d(TAG,"ComputeSuggestions...");
        if(tos == TypeOfSuggestion.EVENTS){
            for (GeneralCandidateSelector selector :
                    selectorsEvents) {
                selector.computeParametersForSelection(base);
                selector.updateCandidatesScore(candidateEvents);
            }
            candidateEvents.sort(new Comparator<Candidate>() {
                @Override
                public int compare(Candidate candidate, Candidate t1) {
                    Double score1 = candidate.getScore();
                    Double score2 = t1.getScore();

                    return score1 == score2 ? 0 : score1 > score2 ? -1 : 1;
                }
            });
            listener.onComplete(candidateEvents,tos);
            Log.d(TAG,"Suggestions EVENTS Computed. RESULTS:"+ candidateEvents.toString());
        }else{
            for (GeneralCandidateSelector selector :
                    selectorsPubs) {
                selector.computeParametersForSelection(base);
                selector.updateCandidatesScore(candidatePubs);
            }
            candidatePubs.sort(new Comparator<Candidate>() {
                @Override
                public int compare(Candidate candidate, Candidate t1) {
                    Double score1 = candidate.getScore();
                    Double score2 = t1.getScore();

                    return score1 == score2 ? 0 : score1 > score2 ? -1 : 1;
                }
            });
            listener.onComplete(candidatePubs,tos);
            Log.d(TAG,"Suggestions PUBS Computed. RESULTS:"+ candidatePubs.toString());
        }
    }
}
