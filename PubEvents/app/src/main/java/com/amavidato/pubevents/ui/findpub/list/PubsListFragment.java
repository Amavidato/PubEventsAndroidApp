package com.amavidato.pubevents.ui.findpub.list;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.general_list_fragment.GeneralListFragment;
import com.amavidato.pubevents.utility.general_list_fragment.MyItem;
import com.amavidato.pubevents.utility.general_list_fragment.SortOptions;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class PubsListFragment extends GeneralListFragment {

    private static final String TAG = PubsListFragment.class.getSimpleName();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PubsListFragment() {
    }

    @Override
    protected void initializeFilterAndSortOptions() {
        filterOptions = new FilterOptionsPub();
        sortOptions = new SortOptionsPub();

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.Filter_Options_pubs, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this.getContext(), R.array.Sort_Options_pubs, android.R.layout.simple_spinner_item);
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
        // Set the adapter
        if (specificRecyclerView instanceof RecyclerView) {
            final Context context = specificRecyclerView.getContext();
            final RecyclerView recyclerView = (RecyclerView) specificRecyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(divider);
            final List<MyItem> pubs = new ArrayList<>();
            //Query to retrieve pubs information
            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        final int numPubs = task.getResult().size();
                        final int[] pubsDone = {0};
                        for (final QueryDocumentSnapshot document : task.getResult()) {
                            final Pub pub = new Pub();
                            final String pubID = document.getId();
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
                            FirebaseFirestore.getInstance().document(DBManager.CollectionsPaths.CITY + "/" + cityID)
                                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
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
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(context, "Error getting pub list from database.\n Please check your connection and try again.", Toast.LENGTH_LONG).show();
                    }
                    progressBar.setVisibility(View.INVISIBLE);
                }
            });
        }
    }
}
