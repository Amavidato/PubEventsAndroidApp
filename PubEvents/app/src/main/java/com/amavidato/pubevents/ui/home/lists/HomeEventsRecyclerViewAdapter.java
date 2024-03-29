package com.amavidato.pubevents.ui.home.lists;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.Navigation;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.ui.events.list.EventItem;
import com.amavidato.pubevents.ui.events.list.EventViewHolder;
import com.amavidato.pubevents.ui.home.HomeFragmentDirections;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralViewHolder;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.amavidato.pubevents.utility.list_abstract_classes.SimpleRecyclerViewAdapter;

import java.util.List;

public class HomeEventsRecyclerViewAdapter extends SimpleRecyclerViewAdapter {

    private static final String TAG = HomeEventsRecyclerViewAdapter.class.getSimpleName();

    public HomeEventsRecyclerViewAdapter(List<MyItem> eventItems, Activity activity) {
        super(eventItems, activity);
    }

    @Override
    protected GeneralViewHolder customOnCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_event, parent, false);
        return new EventViewHolder(view);
    }

    @Override
    protected void customOnBindViewHolder(GeneralViewHolder holder, int position) {
        final EventViewHolder tmp = (EventViewHolder) holder;
        if (tmp != null) {
            final EventItem item = (EventItem) allItems.get(position);
            Log.d(TAG, "EventItem:" + item);
            Event event = (Event) item.object;
            tmp.mItem = item;
            tmp.initImage();
            tmp.mNameView.setText(event.getName());
            tmp.mPubNameView.setText(event.getPub().getName());
            tmp.mPrice.setText(((Double) event.getPrice()).toString());
            tmp.mEventType.setText(event.getType().toString());
            tmp.mDate.setText(event.getDateStrShort());
            tmp.mSeats.setText(event.getSeatsStr());

            tmp.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeFragmentDirections.ActionOpenEventFragment action = HomeFragmentDirections.actionOpenEventFragment(item.id);
                    View navController = mActivity.findViewById(R.id.nav_host_fragment);
                    Navigation.findNavController(navController).navigate(action);
                }
            });
        }
    }
}