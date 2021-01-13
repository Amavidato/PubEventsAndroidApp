package com.amavidato.pubevents.ui.acquired_events;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.amavidato.pubevents.R;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AcquiredEventsRecyclerViewAdapter extends RecyclerView.Adapter<AcquiredEventsRecyclerViewAdapter.ViewHolder> implements Filterable {

    private final List<EventItem> allAcquiredEvents;
    private final List<EventItem> eventsToShow;
    private final Activity mActivity;
    private AcquiredEventsFragment.FilterOptions selectedFilterOpt = AcquiredEventsFragment.FilterOptions.values()[0];

    private Filter filter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence charSequence) {
            List<EventItem> filtered = new ArrayList<>();
            if(charSequence.toString().isEmpty()){
                filtered.addAll(allAcquiredEvents);
            }else{
                for(EventItem item : allAcquiredEvents){
                    switch (selectedFilterOpt){
                        case NAME:
                            if(item.event.getName().toLowerCase().contains(charSequence.toString().toLowerCase())){
                                filtered.add(item);
                            }
                            break;
                        case PUB:
                            if(item.event.getPub().toLowerCase().contains(charSequence.toString().toLowerCase())){
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
            eventsToShow.clear();
            eventsToShow.addAll((Collection<? extends EventItem>) filterResults.values);
            Log.d("PUBLISH FILTER","results:ALL"+ allAcquiredEvents +"\nTO SHOW"+ eventsToShow);
            notifyDataSetChanged();
        }
    };

    public AcquiredEventsRecyclerViewAdapter(List<EventItem> items, Activity activity) {
        eventsToShow =  items;
        allAcquiredEvents = new ArrayList<>(items);
        mActivity = activity;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final AcquiredEventsRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = eventsToShow.get(position);
        holder.mImgView.setImageResource(R.drawable.ic_menu_gallery);
        holder.mNameView.setText(eventsToShow.get(position).event.getName());
        holder.mPubNameView.setText(eventsToShow.get(position).event.getPub());

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AcquiredEventsFragmentDirections.ActionOpenEventFragment action = AcquiredEventsFragmentDirections.actionOpenEventFragment(holder.mItem.id);
                View navController = mActivity.findViewById(R.id.nav_host_fragment);
                Navigation.findNavController(navController).navigate(action);
            }
        });
    }

    @Override
    public int getItemCount() { return eventsToShow.size(); }

    @Override
    public Filter getFilter() { return filter; }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImgView;
        public final GridLayout mContentView;
        public final TextView mNameView;
        public final TextView mPubNameView;

        public EventItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImgView = view.findViewById(R.id.event_item_image);
            mContentView = view.findViewById(R.id.event_item_content);
            mNameView = view.findViewById(R.id.event_item_name);
            mPubNameView = view.findViewById(R.id.event_item_pub);
        }

    }

    public void onFilterOptSelected(AcquiredEventsFragment.FilterOptions opt, String s){
        selectedFilterOpt = opt;
        filter.filter(s);
    }
}
