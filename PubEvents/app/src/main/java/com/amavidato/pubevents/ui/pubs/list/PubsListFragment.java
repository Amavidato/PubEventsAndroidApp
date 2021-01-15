package com.amavidato.pubevents.ui.pubs.list;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.general_list_fragment.GeneralListFragment;
import com.amavidato.pubevents.utility.general_list_fragment.MyItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PubsListFragment extends GeneralListFragment {

    private static final String TAG = PubsListFragment.class.getSimpleName();
    private boolean openFollowedPubsList;
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PubsListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        openFollowedPubsList = PubsListFragmentArgs.fromBundle(getArguments()).getStringOpenFollowedPubsList();
        View view = super.onCreateView(inflater,container,savedInstanceState);
        Log.d(TAG, "OPEN FPL: "+openFollowedPubsList);
        return view;
    }
    @Override
    protected void initializeFilterAndSortOptions() {
        filterOptions = new FilterOptionsPub();
        sortOptions = new SortOptionsPub();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.filter_options_pubs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.sort_options_pubs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSort.setAdapter(adapter);
    }

    @Override
    protected View createSpecificListLayout() {
        return (LinearLayout) getLayoutInflater().inflate(R.layout.fragment_publist, null);
    }

    @Override
    protected View createSpecificRecyclerView() {
        return specificListLayout.findViewById(R.id.frag_recycler_view_publist);
    }

    @Override
    protected void popolateSpecificRecyclerView() {
        String path = "";
        if (specificRecyclerView instanceof RecyclerView) {
            progressBar.setVisibility(View.VISIBLE);
            final Context context = specificRecyclerView.getContext();
            final RecyclerView recyclerView = (RecyclerView) specificRecyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(divider);

            if(openFollowedPubsList){
                //Query to retrieve pubs information
                String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if(uid!=null) {
                    path = DBManager.CollectionsPaths.USERS + "/" + uid + "/" + DBManager.CollectionsPaths.UserFields.FOLLOWED_PUBS;
                }else {
                    return;
                }
            } else{
                path = DBManager.CollectionsPaths.PUBS;
            }
            FirebaseFirestore.getInstance().collection(path)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final List<MyItem> pubs = new ArrayList<>();
                                final int numPubs = task.getResult().size();
                                final int[] pubsDone = {0};
                                for (final DocumentSnapshot document : task.getResult()) {
                                    final String pubID = document.getId();
                                    final Pub pub = new Pub();
                                    if(openFollowedPubsList) {
                                        fillWithFollowedPubs(pubID, pub, document, pubs, pubsDone,numPubs,recyclerView);
                                    }else {
                                        fillPubValues(pubID, pub, document, pubs, pubsDone,numPubs,recyclerView);
                                    }
                                }
                            } else {
                                Log.d(TAG, "Error getting documents: ", task.getException());
                                Toast.makeText(context, "Error getting pub list from database.\n Please check your connection and try again.", Toast.LENGTH_LONG).show();
                            }
                            progressBar.setVisibility(View.INVISIBLE);
                        }
                    });
            }
    }

    private void fillWithFollowedPubs(final String pubID, final Pub pub, final DocumentSnapshot document, final List<MyItem> pubs, final int[] pubsDone, final int numPubs, final RecyclerView recyclerView) {
        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                .document(pubID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            fillPubValues(pubID, pub, task.getResult(), pubs, pubsDone,numPubs,recyclerView);
                        } else {
                            Log.d(TAG, "ERROR");
                        }
                    }
                });
    }

    private void fillPubValues(final String pubID, final Pub pub, DocumentSnapshot document, final List<MyItem> pubs, final int[] pubsDone, final int numPubs, final RecyclerView recyclerView) {
        Map<String, Object> data = document.getData();
        Log.d(TAG, document.getId() + " => " + data);

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
                            pubs.add(new PubItem(pubID, pub));
                            pubsDone[0]++;
                            if (pubsDone[0] == numPubs) {
                                recyclerAdapter = new PubListRecyclerViewAdapter(pubs, getActivity());
                                recyclerView.setAdapter(recyclerAdapter);
                                initializeRecyclerAdapter(pubs);
                            }
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            pub.setCity("ERROR");
                        }
                    }
                });
    }
}