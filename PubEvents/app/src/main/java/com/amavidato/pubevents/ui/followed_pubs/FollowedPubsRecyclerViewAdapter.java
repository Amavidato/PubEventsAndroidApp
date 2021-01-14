package com.amavidato.pubevents.ui.followed_pubs;

import android.app.Activity;
import android.location.Location;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.ui.acquired_events.AcquiredEventsFragment;
import com.amavidato.pubevents.ui.acquired_events.AcquiredEventsFragmentDirections;
import com.amavidato.pubevents.ui.acquired_events.EventItem;
import com.amavidato.pubevents.ui.findpub.list.PubItem;
import com.amavidato.pubevents.ui.findpub.list.PubListRecyclerViewAdapter;
import com.amavidato.pubevents.ui.findpub.list.PubsListFragment;
import com.amavidato.pubevents.ui.findpub.list.PubsListFragmentDirections;
import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FollowedPubsRecyclerViewAdapter extends RecyclerView.Adapter<FollowedPubsRecyclerViewAdapter.ViewHolder> implements Filterable {
    private static final String TAG = FollowedPubsRecyclerViewAdapter.class.getSimpleName();

    private final List<PubItem> allFollowedPubs;
    private final List<PubItem> pubsToShow;
    private final Activity mActivity;
    private String currentFilterString;
    private Location lastKnownLoc;
    private PubsListFragment.FilterOptions selectedFilterOpt = PubsListFragment.FilterOptions.values()[0];
    private PubsListFragment.SortOptions selectedSortOpt = PubsListFragment.SortOptions.values()[0];


    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            Log.d(TAG, "PERFORM FILTERING:"+charSequence);
            List<PubItem> filtered = new ArrayList<>();
            currentFilterString = charSequence.toString();
            if(currentFilterString.isEmpty()){
                filtered.addAll(allFollowedPubs);
            }else{
                for(PubItem item : allFollowedPubs){
                    String lc = currentFilterString.toLowerCase();
                    switch (selectedFilterOpt){
                        case NAME:
                            if(item.pub.getName().toLowerCase().contains(lc)){
                                filtered.add(item);
                            }
                            break;
                        case CITY:
                            if(item.pub.getCity().toLowerCase().contains(lc)){
                                filtered.add(item);
                            }
                            break;
                        default:
                            break;
                    }
                }
            }
            FilterResults filterResults = new FilterResults();
            filterResults.values = filtered;
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            pubsToShow.clear();
            pubsToShow.addAll((Collection<? extends PubItem>) filterResults.values);
            Log.d("PUBLISH FILTER","results:ALL"+allFollowedPubs+"\nTO SHOW"+pubsToShow);
            //filter.filter(currentFilterString);
            onSortOptSelected(selectedSortOpt,lastKnownLoc);
            notifyDataSetChanged();
        }
    };

    public FollowedPubsRecyclerViewAdapter(List<PubItem> items, Activity activity) {
        pubsToShow =  items;
        allFollowedPubs = new ArrayList<>(items);
        mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pub_item, parent, false);
        return new ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = pubsToShow.get(position);
        holder.mImgView.setImageResource(R.drawable.ic_menu_gallery);
        holder.mNameView.setText(pubsToShow.get(position).pub.getName());
        holder.mCityView.setText(pubsToShow.get(position).pub.getCity());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PubsListFragmentDirections.ActionOpenPubPage action = PubsListFragmentDirections.actionOpenPubPage(holder.mItem.id);
                View navController = mActivity.findViewById(R.id.nav_host_fragment);
                Navigation.findNavController(navController).navigate(action);
            }
        });
    }

    @Override
    public int getItemCount() { return pubsToShow.size(); }

    @Override
    public Filter getFilter() { return filter; }



    public void onFilterOptSelected(PubsListFragment.FilterOptions opt, String s){
        selectedFilterOpt = opt;
        filter.filter(s);
    }

    public boolean onSortOptSelected(PubsListFragment.SortOptions opt, Location currentLoc){
        selectedSortOpt = opt;
        switch (opt){
            case NAME_ASC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return pubItem.pub.getName().compareToIgnoreCase(t1.pub.getName());
                    }
                });

                break;
            case NAME_DESC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return t1.pub.getName().compareToIgnoreCase(pubItem.pub.getName());
                    }
                });
                break;
            case CITY_NAME_ASC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return pubItem.pub.getCity().compareToIgnoreCase(t1.pub.getCity());
                    }
                });
                break;
            case CITY_NAME_DESC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return t1.pub.getCity().compareToIgnoreCase(pubItem.pub.getCity());
                    }
                });
                break;
            case CLOSEST_TO_FARTHEST:
                if(currentLoc != null) {
                    lastKnownLoc = currentLoc;
                }
                if(lastKnownLoc != null){
                    Collections.sort(pubsToShow, new Comparator<PubItem>() {
                        @Override
                        public int compare(PubItem pubItem, PubItem t1) {
                            Location pubItemLoc = new Location("");
                            Location t1Loc = new Location("");

                            GeoPoint tmp = pubItem.pub.getGeoLocation();
                            pubItemLoc.setLatitude(tmp.getLatitude());
                            pubItemLoc.setLongitude(tmp.getLongitude());

                            tmp = t1.pub.getGeoLocation();
                            t1Loc.setLatitude(tmp.getLatitude());
                            t1Loc.setLongitude(tmp.getLongitude());

                            return ((Float)pubItemLoc.distanceTo(lastKnownLoc)).compareTo(t1Loc.distanceTo(lastKnownLoc));
                        }
                    });
                }else{
                    return false;
                }
                break;
            case FARTHEST_TO_CLOSEST:
                if(currentLoc != null) {
                    lastKnownLoc = currentLoc;
                }
                if(lastKnownLoc != null){
                    Collections.sort(pubsToShow, new Comparator<PubItem>() {
                        @Override
                        public int compare(PubItem pubItem, PubItem t1) {
                            Location pubItemLoc = new Location("");
                            Location t1Loc = new Location("");

                            GeoPoint tmp = pubItem.pub.getGeoLocation();
                            pubItemLoc.setLatitude(tmp.getLatitude());
                            pubItemLoc.setLongitude(tmp.getLongitude());

                            tmp = t1.pub.getGeoLocation();
                            t1Loc.setLatitude(tmp.getLatitude());
                            t1Loc.setLongitude(tmp.getLongitude());

                            return ((Float)t1Loc.distanceTo(lastKnownLoc)).compareTo(pubItemLoc.distanceTo(lastKnownLoc));
                        }
                    });
                }else{
                    return false;
                }

                break;
            case OVERALL_RATE_ASC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return ((Double)pubItem.pub.getOverallRating()).compareTo(t1.pub.getOverallRating());
                    }
                });
                break;
            case OVERALL_RATE_DESC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return ((Double)t1.pub.getOverallRating()).compareTo(pubItem.pub.getOverallRating());
                    }
                });
                break;
            case PRICE_ASC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return t1.pub.getPrice().compareTo(pubItem.pub.getPrice());
                    }
                });
                break;
            case PRICE_DESC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return pubItem.pub.getPrice().compareTo(t1.pub.getPrice());
                    }
                });
                break;
            case AVG_AGE_ASC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return ((Integer)pubItem.pub.getAverageAge()).compareTo(t1.pub.getAverageAge());
                    }
                });
                break;
            case AVG_AGE_DESC:
                Collections.sort(pubsToShow, new Comparator<PubItem>() {
                    @Override
                    public int compare(PubItem pubItem, PubItem t1) {
                        return ((Integer)t1.pub.getAverageAge()).compareTo(pubItem.pub.getAverageAge());
                    }
                });
                break;
            default:
                break;
        }
        return true;
    }

    public List<PubItem> getPubsToShow() {
        return pubsToShow;
    }

    public String getCurrentFilterString() {
        return currentFilterString;
    }

    public void setCurrentFilterString(String currentFilterString) {
        this.currentFilterString = currentFilterString;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImgView;
        public final GridLayout mContentView;
        public final TextView mNameView;
        public final TextView mCityView;

        public PubItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImgView = (ImageView) view.findViewById(R.id.pub_item_image);
            mContentView = view.findViewById(R.id.pub_item_content);
            mNameView = view.findViewById(R.id.pub_item_name);
            mCityView = view.findViewById(R.id.pub_item_city);
        }

    }
}
