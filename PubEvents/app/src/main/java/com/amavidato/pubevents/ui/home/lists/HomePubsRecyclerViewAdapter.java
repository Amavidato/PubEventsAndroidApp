package com.amavidato.pubevents.ui.home.lists;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.navigation.Navigation;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.ui.home.HomeFragmentDirections;
import com.amavidato.pubevents.ui.pubs.list.PubItem;
import com.amavidato.pubevents.ui.pubs.list.PubViewHolder;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralViewHolder;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.amavidato.pubevents.utility.list_abstract_classes.SimpleRecyclerViewAdapter;

import java.util.List;

public class HomePubsRecyclerViewAdapter extends SimpleRecyclerViewAdapter {

    private static final String TAG = HomePubsRecyclerViewAdapter.class.getSimpleName();

    public HomePubsRecyclerViewAdapter(List<MyItem> pubItems, Activity activity) {
        super(pubItems, activity);
    }

    @Override
    protected GeneralViewHolder customOnCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.cardview_pub, parent, false);
        return new PubViewHolder(view);
    }

    @Override
    protected void customOnBindViewHolder(GeneralViewHolder holder, int position) {
        final PubViewHolder tmp = (PubViewHolder) holder;
        if (tmp != null) {
            final PubItem item = (PubItem) allItems.get(position);
            Log.d(TAG, "PubItem:" + item);
            Pub pub = (Pub) item.object;
            tmp.mItem = item;
            tmp.initImage();
            tmp.mNameView.setText(pub.getName());
            tmp.mCityView.setText(pub.getCity());
            tmp.mRatingView.setText(((Double)pub.getOverallRating()).toString());
            tmp.setPrice(pub.getPrice());

            tmp.mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    HomeFragmentDirections.ActionOpenPubFragment action = HomeFragmentDirections.actionOpenPubFragment(item.id);
                    View navController = mActivity.findViewById(R.id.nav_host_fragment);
                    Navigation.findNavController(navController).navigate(action);
                }
            });
        }
    }
}