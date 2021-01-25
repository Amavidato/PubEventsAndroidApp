package com.amavidato.pubevents.ui.home;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.ui.events.list.EventItem;
import com.amavidato.pubevents.ui.home.lists.HomeEventsRecyclerViewAdapter;
import com.amavidato.pubevents.ui.home.lists.HomePubsRecyclerViewAdapter;
import com.amavidato.pubevents.ui.pubs.list.PubItem;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class HomeFragment extends Fragment {
    private static final String TAG = HomeFragment.class.getSimpleName();

    private HomeViewModel homeViewModel;

    private HomeEventsRecyclerViewAdapter eventsAcquiredRecyclerAdapter;
    private HomeEventsRecyclerViewAdapter eventsAllRecyclerAdapter;
    private HomePubsRecyclerViewAdapter pubsFollowedRecyclerAdapter;
    private HomePubsRecyclerViewAdapter pubsAllRecyclerAdapter;

    private ConstraintLayout eventsAcquiredLayoutContainer;
    private ConstraintLayout eventsAllLayoutContainer;
    private ConstraintLayout pubsFollowedLayoutContainer;
    private ConstraintLayout pubsAllLayoutContainer;

    private ConstraintLayout homeContainerEventsAcquired;
    private ConstraintLayout homeContainerEventsAll;
    private ConstraintLayout homeContainerPubsFollowed;
    private ConstraintLayout homeContainerPubsAll;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");
        homeViewModel =
                ViewModelProviders.of(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        final TextView textView = root.findViewById(R.id.text_home);
        homeViewModel.getText().observe(this, new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        
        homeContainerEventsAcquired = root.findViewById(R.id.home_container_events_acquired_date_near_to_far);
        homeContainerEventsAll = root.findViewById(R.id.home_container_events_all_date_near_to_far);
        homeContainerPubsFollowed = root.findViewById(R.id.home_container_pubs_followed_proximity_near_to_far);
        homeContainerPubsAll = root.findViewById(R.id.home_container_pubs_all_proximity_near_to_far);

        FirebaseAuth.getInstance().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser mUser = firebaseAuth.getCurrentUser();//FirebaseAuth.getInstance().getCurrentUser();
                final String[] username = {null};
                if(mUser != null){
                    mUser.getIdToken(true)
                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        String idToken = task.getResult().getToken();
                                        username[0] = mUser.getDisplayName();
                                        Log.d(TAG, "TOKEN EXISTS. Username:"+username[0]);
                                        // Send token to your backend via HTTPS
                                        // ...
                                    } else {
                                        Log.d(TAG, "TOKEN DOESN'T EXISTS");
                                        // Handle error -> task.getException();
                                    }
                                    createLists();
                                }
                            });
                }else{
                    Log.d(TAG, "User null...");
                    //createLists();
                }
            }
        });
        createLists();
        return root;
    }

    private void createLists() {
        Log.d(TAG, "Create Lists");
        Activity activity = getActivity();
        if (isAdded()){ // Check the fragment status
            homeContainerEventsAcquired.removeAllViews();
            homeContainerEventsAll.removeAllViews();
            homeContainerPubsFollowed.removeAllViews();
            homeContainerPubsAll.removeAllViews();
            createEventsAcquiredList();
            createEventsAllList();
            createPubsFollowedList();
            createPubsAllList();
        }
    }

    private void createEventsAcquiredList() {
        Log.d(TAG, "Create Events Acquired Lists");
        eventsAcquiredLayoutContainer = (ConstraintLayout) getLayoutInflater().inflate(R.layout.simple_events_list, null);
        final ProgressBar progressBar = eventsAcquiredLayoutContainer.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        TextView textView = eventsAcquiredLayoutContainer.findViewById(R.id.simple_events_list_title);
        textView.setText("Events Acquired");
        // Set the adapter
        View listView = eventsAcquiredLayoutContainer.findViewById(R.id.pub_events);
        if (listView instanceof RecyclerView) {
            final Context context = listView.getContext();
            final RecyclerView recyclerView = (RecyclerView) listView;

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.HORIZONTAL);
            recyclerView.addItemDecoration(divider);
            final List<MyItem> events = new ArrayList<>();
            //Query to retrieve ratings information
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user != null && user.isEmailVerified()){
                String uid = user.getEmail();
                FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS)
                        .document(uid)
                        .collection(DBManager.CollectionsPaths.UserFields.ACQUIRED_EVENTS)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final int numItems = task.getResult().size();
                            final int[] itemsDone = {0};
                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.EVENTS)
                                        .document(document.getId())
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
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

                                            Long ltmp = (Long)data.get(DBManager.CollectionsPaths.EventFields.MAX_CAPACITY);
                                            event.setMax_capacity(ltmp != null ? ltmp.intValue() : null);

                                            ltmp = (Long)data.get(DBManager.CollectionsPaths.EventFields.RESERVED_SEATS);
                                            //ltmp = ltmp != null ? ltmp : new Long(0);
                                            event.setReserved_seats(ltmp.intValue());


                                            final String pubID = (String)data.get(DBManager.CollectionsPaths.EventFields.PUB);
                                            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                                                    .document(pubID)
                                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                                                        events.add(new EventItem(document.getId() ,event));
                                                        itemsDone[0]++;
                                                        if (itemsDone[0] == numItems) {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            eventsAcquiredRecyclerAdapter = new HomeEventsRecyclerViewAdapter(events,getActivity());
                                                            recyclerView.setAdapter(eventsAcquiredRecyclerAdapter);
                                                            if(eventsAcquiredLayoutContainer.getParent() != null){
                                                                ConstraintLayout l  = ((ConstraintLayout)eventsAcquiredLayoutContainer.getParent());
                                                                Log.d(TAG, "ACUQIRED Constr. Lay.:"+l+"----ID:"+l.getId());
                                                                l.removeView(eventsAcquiredLayoutContainer);
                                                                //l.removeViewInLayout(eventsAllLayoutContainer);
                                                                l  = ((ConstraintLayout)eventsAcquiredLayoutContainer.getParent());
                                                                if(l != null){
                                                                    Log.d(TAG, "ACQUIRED AFTER Constr. Lay.:"+l+"----ID:"+l.getId());
                                                                }
                                                            }
                                                            homeContainerEventsAcquired.addView(eventsAcquiredLayoutContainer);
                                                        }
                                                    }else {
                                                        Log.d(TAG, "Error getting documents: Event Pub:", task.getException());
                                                    }
                                                }
                                            });
                                        }else{
                                            Log.d(TAG, "Error getting documents: All Events:", task.getException());
                                        }
                                    }
                                });
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: Acquired Events:", task.getException());
                            Toast.makeText(context,"Error getting pub list from database.\n Please check your connection and try again.",Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }else {
            }
        }
    }

    private void createEventsAllList() {
        Log.d(TAG, "Create Events All List");

        eventsAllLayoutContainer = (ConstraintLayout) getLayoutInflater().inflate(R.layout.simple_events_list, null);

        final ProgressBar progressBar = eventsAllLayoutContainer.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        TextView textView = eventsAllLayoutContainer.findViewById(R.id.simple_events_list_title);
        textView.setText("All Events");
        // Set the adapter
        View listView = eventsAllLayoutContainer.findViewById(R.id.pub_events);
        if (listView instanceof RecyclerView) {
            final Context context = listView.getContext();
            final RecyclerView recyclerView = (RecyclerView) listView;

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.HORIZONTAL);
            recyclerView.addItemDecoration(divider);
            final List<MyItem> events = new ArrayList<>();
            //Query to retrieve ratings information
            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.EVENTS)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        final int numItems = task.getResult().size();
                        final int[] itemsDone = {0};
                        for (final QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            Log.d(TAG, document.getId() + " => " + data);

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

                            Long ltmp = (Long)data.get(DBManager.CollectionsPaths.EventFields.MAX_CAPACITY);
                            event.setMax_capacity(ltmp != null ? ltmp.intValue() : null);

                            ltmp = (Long)data.get(DBManager.CollectionsPaths.EventFields.RESERVED_SEATS);
                            ltmp = ltmp != null ? ltmp : new Long(0);
                            event.setReserved_seats(ltmp.intValue());


                            final String pubID = (String)data.get(DBManager.CollectionsPaths.EventFields.PUB);
                            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                                    .document(pubID)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                                        events.add(new EventItem(document.getId() ,event));
                                        itemsDone[0]++;
                                        if (itemsDone[0] == numItems) {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            eventsAllRecyclerAdapter = new HomeEventsRecyclerViewAdapter(events,getActivity());
                                            recyclerView.setAdapter(eventsAllRecyclerAdapter);
                                            if(eventsAllLayoutContainer.getParent() != null){
                                                ConstraintLayout l  = ((ConstraintLayout)eventsAllLayoutContainer.getParent());
                                                Log.d(TAG, "Constr. Lay.:"+l+"----ID:"+l.getId());
                                                l.removeView(eventsAllLayoutContainer);
                                                //l.removeViewInLayout(eventsAllLayoutContainer);
                                                l  = ((ConstraintLayout)eventsAllLayoutContainer.getParent());
                                                if(l != null){
                                                    Log.d(TAG, "AFTER Constr. Lay.:"+l+"----ID:"+l.getId());
                                                }
                                            }
                                            homeContainerEventsAll.addView(eventsAllLayoutContainer);
                                        }
                                    }else {
                                        Log.d(TAG, "Error getting documents: Event Pub:", task.getException());
                                    }
                                }
                            });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: Acquired Events:", task.getException());
                        Toast.makeText(context,"Error getting pub list from database.\n Please check your connection and try again.",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

    }

    private void createPubsFollowedList() {
        Log.d(TAG, "Create Pubs FOLLOWED Lists");
        pubsFollowedLayoutContainer = (ConstraintLayout) getLayoutInflater().inflate(R.layout.simple_pubs_list, null);

        final ProgressBar progressBar = pubsFollowedLayoutContainer.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        TextView textView = pubsFollowedLayoutContainer.findViewById(R.id.simple_pubs_list_title);
        textView.setText("Followed Pubs");
        // Set the adapter
        View listView = pubsFollowedLayoutContainer.findViewById(R.id.pubs);
        if (listView instanceof RecyclerView) {
            final Context context = listView.getContext();
            final RecyclerView recyclerView = (RecyclerView) listView;

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.HORIZONTAL);
            recyclerView.addItemDecoration(divider);
            final List<MyItem> pubs = new ArrayList<>();
            //Query to retrieve ratings information
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if(user != null && user.isEmailVerified()) {
                String uid = user.getEmail();
                FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS + "/" + uid + "/" + DBManager.CollectionsPaths.UserFields.FOLLOWED_PUBS)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.N)
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            final int numItems = task.getResult().size();
                            final int[] itemsDone = {0};
                            for (final QueryDocumentSnapshot document : task.getResult()) {
                                Map<String, Object> data = document.getData();
                                Log.d(TAG+"Followed", document.getId() + " => " + data);
                                FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                                        .document(document.getId())
                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        Map<String, Object> data = task.getResult().getData();
                                        Log.d(TAG+"All", task.getResult().getId() + " => " + data);
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
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        if (task.isSuccessful()) {
                                                            Map<String, Object> data = task.getResult().getData();
                                                            Log.d(TAG + "--CITy", task.getResult().getId() + " => " + data);
                                                            pub.setCity((String) data.get(DBManager.CollectionsPaths.CityFields.NAME));
                                                            pubs.add(new PubItem(document.getId(), pub));
                                                            itemsDone[0]++;
                                                            if (itemsDone[0] == numItems) {
                                                                progressBar.setVisibility(View.INVISIBLE);
                                                                pubsFollowedRecyclerAdapter = new HomePubsRecyclerViewAdapter(pubs,getActivity());
                                                                recyclerView.setAdapter(pubsFollowedRecyclerAdapter);
                                                                if(pubsFollowedLayoutContainer.getParent() != null){
                                                                    ConstraintLayout l  = ((ConstraintLayout) pubsFollowedLayoutContainer.getParent());
                                                                    Log.d(TAG, "Constr. Lay.:"+l+"----ID:"+l.getId());
                                                                    l.removeView(pubsFollowedLayoutContainer);
                                                                    //l.removeViewInLayout(eventsAllLayoutContainer);
                                                                    l  = ((ConstraintLayout) pubsFollowedLayoutContainer.getParent());
                                                                    if(l != null){
                                                                        Log.d(TAG, "AFTER Constr. Lay.:"+l+"----ID:"+l.getId());
                                                                    }
                                                                }
                                                                homeContainerPubsFollowed.addView(pubsFollowedLayoutContainer);
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
                            Log.d(TAG, "Error getting documents: Acquired Events:", task.getException());
                            Toast.makeText(context, "Error getting pub list from database.\n Please check your connection and try again.", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        }

    }

    private void createPubsAllList() {
        Log.d(TAG, "Create Pubs ALL Lists");
        pubsAllLayoutContainer = (ConstraintLayout) getLayoutInflater().inflate(R.layout.simple_pubs_list, null);

        final ProgressBar progressBar = pubsAllLayoutContainer.findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);

        TextView textView = pubsAllLayoutContainer.findViewById(R.id.simple_pubs_list_title);
        textView.setText("All Pubs");
        // Set the adapter
        View listView = pubsAllLayoutContainer.findViewById(R.id.pubs);
        if (listView instanceof RecyclerView) {
            final Context context = listView.getContext();
            final RecyclerView recyclerView = (RecyclerView) listView;

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.HORIZONTAL);
            recyclerView.addItemDecoration(divider);
            final List<MyItem> pubs = new ArrayList<>();
            //Query to retrieve ratings information
            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        final int numItems = task.getResult().size();
                        final int[] itemsDone = {0};
                        for (final QueryDocumentSnapshot document : task.getResult()) {
                            Map<String, Object> data = document.getData();
                            Log.d(TAG+"All in All", document.getId() + " => " + data);

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
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                Map<String, Object> data = task.getResult().getData();
                                                Log.d(TAG + "--CITy", task.getResult().getId() + " => " + data);
                                                pub.setCity((String) data.get(DBManager.CollectionsPaths.CityFields.NAME));
                                                pubs.add(new PubItem(document.getId(), pub));
                                                itemsDone[0]++;
                                                if (itemsDone[0] == numItems) {
                                                    progressBar.setVisibility(View.INVISIBLE);
                                                    pubsAllRecyclerAdapter = new HomePubsRecyclerViewAdapter(pubs,getActivity());
                                                    recyclerView.setAdapter(pubsAllRecyclerAdapter);
                                                    if(pubsAllLayoutContainer.getParent() != null){
                                                        ConstraintLayout l  = ((ConstraintLayout) pubsAllLayoutContainer.getParent());
                                                        Log.d(TAG, "Constr. Lay.:"+l+"----ID:"+l.getId());
                                                        l.removeView(pubsAllLayoutContainer);
                                                        //l.removeViewInLayout(eventsAllLayoutContainer);
                                                        l  = ((ConstraintLayout) pubsAllLayoutContainer.getParent());
                                                        if(l != null){
                                                            Log.d(TAG, "AFTER Constr. Lay.:"+l+"----ID:"+l.getId());
                                                        }
                                                    }
                                                    homeContainerPubsAll.addView(pubsAllLayoutContainer);
                                                }
                                            } else {
                                                Log.d(TAG, "Error getting documents: ", task.getException());
                                                pub.setCity("ERROR");
                                            }
                                        }
                                    });
                        }
                    } else {
                        Log.d(TAG, "Error getting documents: Acquired Events:", task.getException());
                        Toast.makeText(context, "Error getting pub list from database.\n Please check your connection and try again.", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        createLists();
        Log.d(TAG, "onResume");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }
}