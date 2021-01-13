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
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.utility.db.DBManager;
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

public class PubsListFragment extends Fragment{

    private static final String TAG = PubsListFragment.class.getSimpleName();

    private ProgressBar progressBar;
    private PubListRecyclerViewAdapter recyclerAdapter;
    private AppCompatSpinner spinnerFilter;

    public enum FilterOptions {NAME, CITY;}

    private AppCompatSpinner spinnerSort;

    public enum SortOptions {
        NAME_ASC, NAME_DESC, CITY_NAME_ASC, CITY_NAME_DESC,
        CLOSEST_TO_FARTHEST, FARTHEST_TO_CLOSEST, OVERALL_RATE_ASC, OVERALL_RATE_DESC,
        PRICE_ASC, PRICE_DESC, AVG_AGE_ASC, AVG_AGE_DESC
    }

    private SearchView searchView;

    private FusedLocationProviderClient fusedLocationClient;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PubsListFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_publist, container, false);
        View listView = view.findViewById(R.id.publist_list);
        progressBar = view.findViewById(R.id.publist_loading_list);
        progressBar.setVisibility(View.VISIBLE);
        spinnerFilter = view.findViewById(R.id.publist_filter_option);
        spinnerSort = view.findViewById(R.id.publist_sort_option);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (view != null) {
                    String selected = ((TextView) view).getText().toString();
                    if (recyclerAdapter != null) {
                        recyclerAdapter.onFilterOptSelected(FilterOptions.valueOf(selected.toUpperCase()), searchView.getQuery().toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (view != null) {
                    String selected = ((TextView) view).getText().toString();
                    if (recyclerAdapter != null) {
                        selected = spinnerSort.getSelectedItem().toString();
                        selected = selected.toUpperCase().replace("(","")
                                .replace(")","").replace(".","").replace(" ", "_");
                        final SortOptions opt = SortOptions.valueOf(selected);
                        if(opt.equals(SortOptions.CLOSEST_TO_FARTHEST) || opt.equals(SortOptions.FARTHEST_TO_CLOSEST)){
                            onSortProximity(opt);
                        }else{
                            recyclerAdapter.onSortOptSelected(opt,null);
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Set the adapter
        if (listView instanceof RecyclerView) {
            final Context context = listView.getContext();
            final RecyclerView recyclerView = (RecyclerView) listView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(), DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(divider);
            final List<PubItem> pubs = new ArrayList<>();
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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(true);

        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //if query is empty, override default onClose by doing nothing
                if (searchView.getQuery().toString().isEmpty()) {
                    return true;
                }
                //else run default onClose (it will delete the query)
                return false;
            }
        });

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setIconified(false);
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                recyclerAdapter.setCurrentFilterString(s);
                recyclerAdapter.getFilter().filter(s);
                return false;
            }
        });

        if (recyclerAdapter != null && recyclerAdapter.getCurrentFilterString() != null && !recyclerAdapter.getCurrentFilterString().isEmpty()) {
            searchView.setQuery(recyclerAdapter.getCurrentFilterString(), false);
            searchItem.expandActionView();
        }
    }

    private void initializeRecyclerAdapter(List<PubItem> pubs) {
        if(recyclerAdapter != null) {
            String selected = spinnerFilter.getSelectedItem().toString();
            recyclerAdapter.onFilterOptSelected(FilterOptions.valueOf(selected.toUpperCase()), searchView.getQuery().toString());

            selected = spinnerSort.getSelectedItem().toString();
            selected = selected.toUpperCase().replace("(","")
                    .replace(")","").replace(".","").replace(" ", "_");
            final SortOptions opt = SortOptions.valueOf(selected);
            if(opt.equals(SortOptions.CLOSEST_TO_FARTHEST) || opt.equals(SortOptions.FARTHEST_TO_CLOSEST)){
                onSortProximity(opt);
            }else{
                recyclerAdapter.onSortOptSelected(opt,null);
                recyclerAdapter.notifyDataSetChanged();
            }
        }
    }

    private void onSortProximity(final SortOptions opt){
        if (ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this.getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // Got last known location. In some rare situations this can be null.
                        if (location != null) {
                            if(!recyclerAdapter.onSortOptSelected(opt,location)){
                                Toast.makeText(getContext(),"Error getting your current location!\nPlease, verify the location permissions, the gps and try again.",Toast.LENGTH_LONG).show();
                            };
                            recyclerAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }

}
