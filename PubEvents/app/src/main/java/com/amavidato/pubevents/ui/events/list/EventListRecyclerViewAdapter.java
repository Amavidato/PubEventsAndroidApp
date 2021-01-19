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
import com.google.firebase.firestore.GeoPoint;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.DATE_FURTHEST;
import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.DATE_NEAREST;
import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.PROXIMITY_NEAREST;
import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.PROXIMITY_FURTHEST;
import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.NAME_ASC;
import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.NAME_DESC;
import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.PRICE_ASC;
import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.PRICE_DESC;
import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.PUB_NAME_ASC;
import static com.amavidato.pubevents.ui.events.list.SortOptionsEvent.PUB_NAME_DESC;

public class EventListRecyclerViewAdapter extends GeneralRecyclerViewAdapter {
    private static final String TAG = EventListRecyclerViewAdapter.class.getSimpleName();

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
                if(event.getPub().getName().toLowerCase().contains(lc)){
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
        tmp.mPubNameView.setText(event.getPub().getName());

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
        if(opt != null){
            switch (opt){
                case NAME_ASC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem item, MyItem t1) {
                            return ((Event)item.object).getName().compareToIgnoreCase(((Event)t1.object).getName());
                        }
                    });
                    break;
                case NAME_DESC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem item, MyItem t1) {
                            return ((Event)t1.object).getName().compareToIgnoreCase(((Event)item.object).getName());
                        }
                    });
                    break;
                case DATE_NEAREST:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem item, MyItem t1) {
                            return ((Event)item.object).getDate().compareTo(((Event)t1.object).getDate());
                        }
                    });
                    break;
                case DATE_FURTHEST:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem item, MyItem t1) {
                            return ((Event)t1.object).getDate().compareTo(((Event)item.object).getDate());
                        }
                    });
                    break;
                case PUB_NAME_ASC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem item, MyItem t1) {
                            return ((Event)item.object).getPub().getName().compareToIgnoreCase(((Event)t1.object).getPub().getName());
                        }
                    });
                    break;
                case PUB_NAME_DESC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem item, MyItem t1) {
                            return ((Event)t1.object).getPub().getName().compareToIgnoreCase(((Event)item.object).getPub().getName());
                        }
                    });
                    break;
                case PROXIMITY_NEAREST:
                    if(currentLoc != null) {
                        lastKnownLoc = currentLoc;
                    }
                    if(lastKnownLoc != null){
                        final Location finalLastKnownLoc = lastKnownLoc;
                        Collections.sort(toShow, new Comparator<MyItem>() {
                            @Override
                            public int compare(MyItem item, MyItem t1) {
                                Location pubItemLoc = new Location("");
                                Location t1Loc = new Location("");

                                GeoPoint tmp = ((Event)item.object).getPub().getGeoLocation();
                                pubItemLoc.setLatitude(tmp.getLatitude());
                                pubItemLoc.setLongitude(tmp.getLongitude());

                                tmp = ((Event)t1.object).getPub().getGeoLocation();
                                t1Loc.setLatitude(tmp.getLatitude());
                                t1Loc.setLongitude(tmp.getLongitude());

                                return ((Float)pubItemLoc.distanceTo(finalLastKnownLoc)).compareTo(t1Loc.distanceTo(finalLastKnownLoc));
                            }
                        });
                    }else {
                        return false;
                    }
                    break;
                case PROXIMITY_FURTHEST:
                    if(currentLoc != null) {
                        lastKnownLoc = currentLoc;
                    }
                    if(lastKnownLoc != null){
                        final Location finalLastKnownLoc = lastKnownLoc;
                        Collections.sort(toShow, new Comparator<MyItem>() {
                            @Override
                            public int compare(MyItem item, MyItem t1) {
                                Location pubItemLoc = new Location("");
                                Location t1Loc = new Location("");

                                GeoPoint tmp = ((Event)item.object).getPub().getGeoLocation();
                                pubItemLoc.setLatitude(tmp.getLatitude());
                                pubItemLoc.setLongitude(tmp.getLongitude());

                                tmp = ((Event)t1.object).getPub().getGeoLocation();
                                t1Loc.setLatitude(tmp.getLatitude());
                                t1Loc.setLongitude(tmp.getLongitude());

                                return ((Float)t1Loc.distanceTo(finalLastKnownLoc)).compareTo(pubItemLoc.distanceTo(finalLastKnownLoc));
                            }
                        });
                    }else {
                        return false;
                    }
                    break;
                case PRICE_ASC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem item, MyItem t1) {
                            return ((Double)((Event)item.object).getPrice()).compareTo(((Event)t1.object).getPrice());
                        }
                    });
                    break;
                case PRICE_DESC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem item, MyItem t1) {
                            return ((Double)((Event)t1.object).getPrice()).compareTo(((Event)item.object).getPrice());
                        }
                    });
                    break;
                default:
                    return false;
            }
        }
        return true;
    }
}
