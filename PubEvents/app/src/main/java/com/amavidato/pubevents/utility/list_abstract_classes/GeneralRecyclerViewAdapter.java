package com.amavidato.pubevents.utility.list_abstract_classes;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.widget.Filter;
import android.widget.Filterable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public abstract class GeneralRecyclerViewAdapter extends SimpleRecyclerViewAdapter implements Filterable {

    private static final String TAG = GeneralRecyclerViewAdapter.class.getSimpleName();

    protected final List<MyItem> toShow;

    protected String currentFilterString;
    protected Location lastKnownLoc;
    protected String selectedFilterOpt;
    protected String selectedSortOpt;

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            Log.d(TAG, "PERFORM FILTERING:"+charSequence+"***SelFiltOpt:"+selectedFilterOpt);
            List<MyItem> filtered = new ArrayList<>();
            currentFilterString = charSequence.toString();
            if(currentFilterString.isEmpty()){
                filtered.addAll(allItems);
            }else{
                for(MyItem item : allItems){
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
            Log.d("PUBLISH FILTER","filterResult:"+filterResults.values+"\nresults:ALL"+allItems+"\nTO SHOW"+toShow);
            onSortOptSelected(selectedSortOpt,lastKnownLoc);
            notifyDataSetChanged();
        }
    };

    public GeneralRecyclerViewAdapter(List<MyItem> items, Activity activity) {
        super(items,activity);
        toShow =  items;
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

    protected abstract boolean customOnSortOptSelected(String opt, Location currentLoc, Location lastKnownLoc);

}
