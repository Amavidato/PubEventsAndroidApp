package com.amavidato.pubevents.ui.pubs.pub.ratings;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Rating;
import com.amavidato.pubevents.ui.pubs.pub.events.PubEventsListRecyclerViewAdapter;
import com.amavidato.pubevents.utility.list_abstract_classes.SimpleRecyclerViewAdapter;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralViewHolder;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;

import java.util.List;

public class RatingsListRecyclerViewAdapter extends SimpleRecyclerViewAdapter {

    private static final String TAG = PubEventsListRecyclerViewAdapter.class.getSimpleName();

    public RatingsListRecyclerViewAdapter(List<MyItem> ratingItems, Activity activity) {
        super(ratingItems, activity);
    }

    @Override
    protected GeneralViewHolder customOnCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rating_template, parent, false);
        return new RatingViewHolder(view);
    }

    @Override
    protected void customOnBindViewHolder(GeneralViewHolder holder, int position) {
        RatingViewHolder tmp = (RatingViewHolder) holder;
        tmp.mImgView.setImageResource(R.drawable.ic_menu_gallery);
        Rating rating = (Rating) allItems.get(position).object;
        tmp.mNameView.setText(rating.getUser());
        tmp.mValueView.setText(((Integer)rating.getValue()).toString());
        tmp.mCommentView.setText(rating.getComment());
    }

}
