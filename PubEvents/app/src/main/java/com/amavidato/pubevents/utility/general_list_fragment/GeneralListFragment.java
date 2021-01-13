package com.amavidato.pubevents.utility.general_list_fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
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

import androidx.appcompat.widget.AppCompatSpinner;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.ui.findpub.list.PubsListFragment;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import java.util.List;

public abstract class GeneralListFragment extends Fragment {
    private static final String TAG = GeneralListFragment.class.getSimpleName();

    private ProgressBar progressBar;
    private GeneralRecyclerViewAdapter recyclerAdapter;

    private FilterOptions filterOptions;
    private SortOptions sortOptions;
    private AppCompatSpinner spinnerFilter;
    private AppCompatSpinner spinnerSort;

    private SearchView searchView;

    private FusedLocationProviderClient fusedLocationClient;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
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

        return new View(getContext());
    }

    private void initializeRecyclerAdapter(List<MyItem> items) {
        if(recyclerAdapter != null) {
            String selected = spinnerFilter.getSelectedItem().toString();
            recyclerAdapter.onFilterOptSelected(filterOptions.valueOf(selected.toUpperCase()), searchView.getQuery().toString());

            selected = spinnerSort.getSelectedItem().toString();
            selected = selected.toUpperCase().replace("(","")
                    .replace(")","").replace(".","").replace(" ", "_");
            final String opt = sortOptions.valueOf(selected);
            if(opt.equals(PubsListFragment.SortOptions.CLOSEST_TO_FARTHEST) || opt.equals(PubsListFragment.SortOptions.FARTHEST_TO_CLOSEST)){
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

        if (recyclerAdapter != null && recyclerAdapter.getCurrentFilterString() != null && !recyclerAdapter.getCurrentFilterString().isEmpty()) {
            searchView.setQuery(recyclerAdapter.getCurrentFilterString(), false);
            searchItem.expandActionView();
        }
    }

    protected void onItemSortSelected(View view){
        if (view != null) {
            if (recyclerAdapter != null) {
                String selected = ((TextView) view).getText().toString();
                selected = selected.toUpperCase().replace("(","")
                        .replace(")","").replace(".","").replace(" ", "_");
                final String opt = sortOptions.valueOf(selected);
                if(opt.equals(SortOptions.CLOSEST_TO_FARTHEST) || opt.equals(SortOptions.FARTHEST_TO_CLOSEST)){
                    onSortProximity(opt);
                }else{
                    recyclerAdapter.onSortOptSelected(opt,null);
                    recyclerAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private void onSortProximity(final String opt){
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