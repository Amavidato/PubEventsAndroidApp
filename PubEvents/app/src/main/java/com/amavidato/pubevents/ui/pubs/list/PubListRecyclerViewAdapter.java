package com.amavidato.pubevents.ui.pubs.list;

import androidx.navigation.Navigation;

import android.app.Activity;
import android.location.Location;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralRecyclerViewAdapter;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralViewHolder;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.google.firebase.firestore.GeoPoint;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static com.amavidato.pubevents.utility.list_abstract_classes.SortOptions.PROXIMITY_NEAREST;
import static com.amavidato.pubevents.utility.list_abstract_classes.SortOptions.PROXIMITY_FURTHEST;

public class PubListRecyclerViewAdapter extends GeneralRecyclerViewAdapter {
    private static final String TAG = PubListRecyclerViewAdapter.class.getSimpleName();

    public PubListRecyclerViewAdapter(List<MyItem> items, Activity activity) {
        super(items, activity);
    }

    @Override
    protected void searchForFilteringResults(List<MyItem> filtered, MyItem item, String selectedFilterOpt, String lc) {
        Pub pub = (Pub) item.object;
        switch (selectedFilterOpt){
            case FilterOptionsPub.NAME:
                if(pub.getName().toLowerCase().contains(lc)){
                    filtered.add(item);
                }
                break;
            case FilterOptionsPub.CITY:
                if(pub.getCity().toLowerCase().contains(lc)){
                    filtered.add(item);
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected GeneralViewHolder customOnCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.pub_item, parent, false);
        return new PubViewHolder(view);
    }

    @Override
    protected void customOnBindViewHolder(final GeneralViewHolder holder, int position) {
        final PubViewHolder tmp = (PubViewHolder) holder;
        PubItem item = (PubItem) toShow.get(position);
        Pub pub = (Pub) item.object;
        tmp.mItem = item;
        tmp.mImgView.setImageResource(R.drawable.ic_menu_gallery);
        tmp.mNameView.setText(pub.getName());
        tmp.mCityView.setText(pub.getCity());

        tmp.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PubsListFragmentDirections.ActionOpenPubPage action = PubsListFragmentDirections.actionOpenPubPage(tmp.mItem.id);
                View navController = mActivity.findViewById(R.id.nav_host_fragment);
                Navigation.findNavController(navController).navigate(action);
            }
        });
    }

    @Override
    protected boolean customOnSortOptSelected(String opt, Location currentLoc, Location lastKnownLoc) {
        if(opt != null){
            switch (opt){
                case SortOptionsPub.NAME_ASC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Pub)pubItem.object).getName().compareToIgnoreCase(((Pub)t1.object).getName());
                        }
                    });

                    break;
                case SortOptionsPub.NAME_DESC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Pub)t1.object).getName().compareToIgnoreCase(((Pub)pubItem.object).getName());
                        }
                    });
                    break;
                case SortOptionsPub.CITY_NAME_ASC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Pub)pubItem.object).getCity().compareToIgnoreCase(((Pub)t1.object).getCity());
                        }
                    });
                    break;
                case SortOptionsPub.CITY_NAME_DESC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Pub)t1.object).getCity().compareToIgnoreCase(((Pub)pubItem.object).getCity());
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
                            public int compare(MyItem pubItem, MyItem t1) {
                                Location pubItemLoc = new Location("");
                                Location t1Loc = new Location("");

                                GeoPoint tmp = ((Pub)pubItem.object).getGeoLocation();
                                pubItemLoc.setLatitude(tmp.getLatitude());
                                pubItemLoc.setLongitude(tmp.getLongitude());

                                tmp = ((Pub)t1.object).getGeoLocation();
                                t1Loc.setLatitude(tmp.getLatitude());
                                t1Loc.setLongitude(tmp.getLongitude());

                                return ((Float)pubItemLoc.distanceTo(finalLastKnownLoc)).compareTo(t1Loc.distanceTo(finalLastKnownLoc));
                            }
                        });
                    }else{
                        return false;
                    }
                    break;
                case PROXIMITY_FURTHEST:
                    if(currentLoc != null) {
                        lastKnownLoc = currentLoc;
                    }
                    if(lastKnownLoc != null){
                        final Location finalLastKnownLoc1 = lastKnownLoc;
                        Collections.sort(toShow, new Comparator<MyItem>() {
                            @Override
                            public int compare(MyItem pubItem, MyItem t1) {
                                Location pubItemLoc = new Location("");
                                Location t1Loc = new Location("");

                                GeoPoint tmp = ((Pub)pubItem.object).getGeoLocation();
                                pubItemLoc.setLatitude(tmp.getLatitude());
                                pubItemLoc.setLongitude(tmp.getLongitude());

                                tmp = ((Pub)t1.object).getGeoLocation();
                                t1Loc.setLatitude(tmp.getLatitude());
                                t1Loc.setLongitude(tmp.getLongitude());

                                return ((Float)t1Loc.distanceTo(finalLastKnownLoc1)).compareTo(pubItemLoc.distanceTo(finalLastKnownLoc1));
                            }
                        });
                    }else{
                        return false;
                    }

                    break;
                case SortOptionsPub.OVERALL_RATE_ASC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Double)((Pub)pubItem.object).getOverallRating()).compareTo(((Pub)t1.object).getOverallRating());
                        }
                    });
                    break;
                case SortOptionsPub.OVERALL_RATE_DESC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Double)((Pub)t1.object).getOverallRating()).compareTo(((Pub)pubItem.object).getOverallRating());
                        }
                    });
                    break;
                case SortOptionsPub.PRICE_ASC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Pub)t1.object).getPrice().compareTo(((Pub)pubItem.object).getPrice());
                        }
                    });
                    break;
                case SortOptionsPub.PRICE_DESC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Pub)pubItem.object).getPrice().compareTo(((Pub)t1.object).getPrice());
                        }
                    });
                    break;
                case SortOptionsPub.AVG_AGE_ASC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Integer)((Pub)pubItem.object).getAverageAge()).compareTo(((Pub)t1.object).getAverageAge());
                        }
                    });
                    break;
                case SortOptionsPub.AVG_AGE_DESC:
                    Collections.sort(toShow, new Comparator<MyItem>() {
                        @Override
                        public int compare(MyItem pubItem, MyItem t1) {
                            return ((Integer)((Pub)t1.object).getAverageAge()).compareTo(((Pub)pubItem.object).getAverageAge());
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
