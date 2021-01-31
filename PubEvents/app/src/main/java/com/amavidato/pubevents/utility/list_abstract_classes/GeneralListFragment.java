package com.amavidato.pubevents.utility.list_abstract_classes;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
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

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amavidato.pubevents.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

import static com.amavidato.pubevents.ui.findpub.FindPubsFragment.PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION;

public abstract class GeneralListFragment extends Fragment {
    private static final String TAG = GeneralListFragment.class.getSimpleName();
    private boolean userDependentList;

    protected ProgressBar progressBar;
    protected GeneralRecyclerViewAdapter recyclerAdapter;

    protected FilterOptions filterOptions;
    protected SortOptions sortOptions;
    protected AppCompatSpinner spinnerFilter;
    protected AppCompatSpinner spinnerSort;

    protected SearchView searchView;

    protected FusedLocationProviderClient fusedLocationClient;

    protected View specificListLayout;
    protected View specificRecyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG,"INSIDE ON CREATE VIEW GENERAL");
        userDependentList = getUserDependedListFragArgument();
        View root = inflater.inflate(R.layout.general_list_fragment, container, false);

        LinearLayout listViewContainer = root.findViewById(R.id.frag_genlist_recycler_view_container);

        specificListLayout = createSpecificListLayout();
        listViewContainer.addView(specificListLayout);

        progressBar = root.findViewById(R.id.frag_genlist_progress_bar);
        progressBar.setVisibility(View.VISIBLE);
        spinnerFilter = root.findViewById(R.id.frag_genlist_filter_opts);
        spinnerSort = root.findViewById(R.id.frag_genlist_sort_option);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (view != null) {
                    String selected = ((TextView) view).getText().toString();
                    if (recyclerAdapter != null) {
                        recyclerAdapter.onFilterOptSelected(filterOptions.valueOf(selected.toUpperCase()), searchView.getQuery().toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spinnerSort.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                onItemSortSelected(view);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this.getActivity());
        setHasOptionsMenu(true);

        initFilterAndSort();
        specificRecyclerView = createSpecificRecyclerView();
        popolateSpecificRecyclerView();
        return root;
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void initializeRecyclerAdapter(List<MyItem> items) {
        if(recyclerAdapter != null) {
            String selected = spinnerFilter.getSelectedItem().toString();
            recyclerAdapter.onFilterOptSelected(filterOptions.valueOf(selected.toUpperCase()), searchView.getQuery().toString());

            selected = spinnerSort.getSelectedItem().toString();
            selected = selected.toUpperCase().replace("(","")
                    .replace(")","").replace(".","").replace(" ", "_");
            final String opt = sortOptions.valueOf(selected);
            if(opt.equals(SortOptions.PROXIMITY_NEAREST) || opt.equals(SortOptions.PROXIMITY_FURTHEST)){
                onSortProximity(opt);
            }else{
                recyclerAdapter.onSortOptSelected(opt,null);
                recyclerAdapter.notifyDataSetChanged();
            }
        }
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

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem menuItem) {

                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem menuItem) {
                searchView.setQuery("",true);
                recyclerAdapter.setCurrentFilterString("");
                recyclerAdapter.getFilter().filter("");
                return true;
            }
        });

        if (recyclerAdapter != null && recyclerAdapter.getCurrentFilterString() != null && !recyclerAdapter.getCurrentFilterString().isEmpty()) {
            searchView.setQuery(recyclerAdapter.getCurrentFilterString(), false);
            searchItem.expandActionView();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onItemSortSelected(View view){
        if (view != null) {
            if (recyclerAdapter != null) {
                String selected = ((TextView) view).getText().toString();
                selected = selected.toUpperCase().replace("(","")
                        .replace(")","").replace(".","").replace(" ", "_");
                final String opt = sortOptions.valueOf(selected);
                if(opt.equals(SortOptions.PROXIMITY_NEAREST) || opt.equals(SortOptions.PROXIMITY_FURTHEST)){
                    onSortProximity(opt);
                }else{
                    recyclerAdapter.onSortOptSelected(opt,null);
                    recyclerAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onSortProximity(final String opt){
        if (ActivityCompat.checkSelfPermission(this.getContext(),
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this.getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            this.getActivity().requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
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

    protected abstract void initFilterAndSort();

    protected void initSpinners(FilterOptions fo, SortOptions so, int array_fo_id, int array_so_id){
        filterOptions = fo;
        sortOptions = so;
        initSpinnerAdapter(spinnerFilter,array_fo_id);
        initSpinnerAdapter(spinnerSort,array_so_id);
    }

    private void initSpinnerAdapter(AppCompatSpinner spinner, int array_id){
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this.getContext(), array_id, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    protected abstract View createSpecificListLayout();
    protected abstract View createSpecificRecyclerView();

    private void popolateSpecificRecyclerView(){
        String path = "";
        if (specificRecyclerView instanceof RecyclerView) {
            progressBar.setVisibility(View.VISIBLE);
            final Context context = specificRecyclerView.getContext();
            final RecyclerView recyclerView = (RecyclerView) specificRecyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(divider);
            if(userDependentList){
                //Query to retrieve pubs information
                String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if(uid!=null) {
                    path = getUserDependedPathQueryList(uid);
                }else {
                    return;
                }
            } else{
                path = getGeneralPathQueryList();
            }
            FirebaseFirestore.getInstance().collection(path)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @RequiresApi(api = Build.VERSION_CODES.N)
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                final List<MyItem> items = new ArrayList<>();
                                final int numItems = task.getResult().size();
                                final int[] itemsDone = {0};
                                for (final DocumentSnapshot document : task.getResult()) {
                                    final String itemID = document.getId();
                                    if(userDependentList) {
                                        makeUserDependentQuery(document, items, itemsDone,numItems,recyclerView);
                                    }else {
                                        fillModelObjectValues(document, items, itemsDone,numItems,recyclerView);
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

    protected abstract void fillModelObjectValues(final DocumentSnapshot document, final List<MyItem> items, final int[] itemsDone, final int numItems, final RecyclerView recyclerView);

    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void addItemToList(List<MyItem> items, MyItem itemToAdd, int[] itemsDone, int numItems, RecyclerView recyclerView){
        items.add(itemToAdd);
        itemsDone[0]++;
        if (itemsDone[0] == numItems) {
            recyclerAdapter = getConcreteRecyclerViewAdapter(items,getActivity());
            recyclerView.setAdapter(recyclerAdapter);
            initializeRecyclerAdapter(items);
        }
    }

    private void makeUserDependentQuery(final DocumentSnapshot document, final List<MyItem> items, final int[] itemsDone, final int numItems, final RecyclerView recyclerView) {
        final String itemID = document.getId();
        FirebaseFirestore.getInstance().document(getGeneralPathQueryList()+"/"+itemID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            fillModelObjectValues(task.getResult(), items, itemsDone,numItems,recyclerView);
                        } else {
                            Log.d(TAG, "ERROR");
                        }
                    }
                });
    }

    protected abstract boolean getUserDependedListFragArgument();

    protected abstract String getGeneralPathQueryList();
    protected abstract String getUserDependedPathQueryList(String uid);
    protected abstract GeneralRecyclerViewAdapter getConcreteRecyclerViewAdapter(List<MyItem> items, FragmentActivity activity);
}
