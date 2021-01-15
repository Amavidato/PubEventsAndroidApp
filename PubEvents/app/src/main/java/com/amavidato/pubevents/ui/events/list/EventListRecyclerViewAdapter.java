package com.amavidato.pubevents.ui.events.list;

import android.app.Activity;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.Navigation;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.utility.general_list_fragment.GeneralRecyclerViewAdapter;
import com.amavidato.pubevents.utility.general_list_fragment.GeneralViewHolder;
import com.amavidato.pubevents.utility.general_list_fragment.MyItem;

import java.util.List;

public class EventListRecyclerViewAdapter extends GeneralRecyclerViewAdapter {

    public EventListRecyclerViewAdapter(List<MyItem> items, Activity activity) {
        super(items,activity);
    }
    @Override
    protected void searchForFilteringResults(List<MyItem> filtered, MyItem item, String selectedFilterOpt, String lc) {
        Event event = (Event) item.object;
        switch (selectedFilterOpt){
            case FilterOptionsEvent.NAME:
                if(event.getName().toLowerCase().contains(lc)){
                    filtered.add(item);
                }
                break;
            case FilterOptionsEvent.PUB_NAME:
                if(event.getPub().toLowerCase().contains(lc)){
                    filtered.add(item);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected EventViewHolder customOnCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.event_item, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    protected void customOnBindViewHolder(final GeneralViewHolder holder, int position) {
        final EventViewHolder tmp = (EventViewHolder) holder;
        EventItem item = (EventItem) toShow.get(position);
        Event event = (Event) item.object;
        tmp.mItem = item;
        tmp.mImgView.setImageResource(R.drawable.ic_menu_gallery);
        tmp.mNameView.setText(event.getName());
        tmp.mPubNameView.setText(event.getPub());

        tmp.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventListFragmentDirections.ActionOpenEventFragment action = EventListFragmentDirections.actionOpenEventFragment(tmp.mItem.id);
                View navController = mActivity.findViewById(R.id.nav_host_fragment);
                Navigation.findNavController(navController).navigate(action);
            }
        });
    }

    @Override
    protected boolean customOnSortOptSelected(String opt, Location currentLoc, Location lastKnownLoc) {
        return false;
    }
}
