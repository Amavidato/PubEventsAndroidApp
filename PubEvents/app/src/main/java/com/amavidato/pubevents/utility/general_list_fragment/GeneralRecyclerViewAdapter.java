package com.amavidato.pubevents.utility.general_list_fragment;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public abstract class GeneralRecyclerViewAdapter extends RecyclerView.Adapter<GeneralViewHolder> implements Filterable {

    private static final String TAG = GeneralRecyclerViewAdapter.class.getSimpleName();

    private final List<MyItem> all;
    private final List<MyItem> toShow;
    private final Activity mActivity;
    private String currentFilterString;
    private Location lastKnownLoc;
    private String selectedFilterOpt;
    private String selectedSortOpt;


    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            Log.d(TAG, "PERFORM FILTERING:"+charSequence);
            List<MyItem> filtered = new ArrayList<>();
            currentFilterString = charSequence.toString();
            if(currentFilterString.isEmpty()){
                filtered.addAll(all);
            }else{
                for(MyItem item : all){
                    String lc = currentFilterString.toLowerCase();
                    searchForFilteringResults(filtered,item,selectedFilterOpt,lc);
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filtered;
            return filterResults;
        }
        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            toShow.clear();
            toShow.addAll((Collection<? extends MyItem>) filterResults.values);
            Log.d("PUBLISH FILTER","results:ALL"+all+"\nTO SHOW"+toShow);
            //filter.filter(currentFilterString);
            onSortOptSelected(selectedSortOpt,lastKnownLoc);
            notifyDataSetChanged();
        }
    };

    public GeneralRecyclerViewAdapter(List<MyItem> items, Activity activity) {
        toShow =  items;
        all = new ArrayList<>(items);
        mActivity = activity;
    }

    @Override
    public GeneralViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return customOnCreateViewHolder();
    }

    @Override
    public void onBindViewHolder(final GeneralViewHolder holder, int position) {
        customOnBindViewHolder(holder,position);
    }

    protected abstract void searchForFilteringResults(List<MyItem> filtered, MyItem item, String selectedFilterOpt, String lc);

    @Override
    public int getItemCount() {
        return toShow.size();
    }

    @Override
    public Filter getFilter() {
        return filter;
    }


    public void onFilterOptSelected(String opt, String s){
        selectedFilterOpt = opt;
        filter.filter(s);
    }

    public boolean onSortOptSelected(String opt, Location currentLoc){
        selectedSortOpt = opt;
        return customOnSortOptSelected(opt,currentLoc,lastKnownLoc);
    }

    public List<MyItem> getToShow() {
        return toShow;
    }

    public String getCurrentFilterString() {
        return currentFilterString;
    }

    public void setCurrentFilterString(String currentFilterString) {
        this.currentFilterString = currentFilterString;
    }

    protected abstract GeneralViewHolder customOnCreateViewHolder();

    protected abstract void customOnBindViewHolder(GeneralViewHolder holder, int position);

    protected abstract boolean customOnSortOptSelected(String opt, Location currentLoc, Location lastKnownLoc);
}
