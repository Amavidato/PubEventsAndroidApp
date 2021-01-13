package com.amavidato.pubevents.ui.pub.ratings;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.amavidato.pubevents.R;

import java.util.ArrayList;
import java.util.List;

public class RatingsListRecyclerViewAdapter extends RecyclerView.Adapter<RatingsListRecyclerViewAdapter.ViewHolder>{

    private final List<RatingItem> allRating;

    public RatingsListRecyclerViewAdapter(List<RatingItem> items) {
        allRating = new ArrayList<>(items);
    }

    @Override
    public RatingsListRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.rating_template, parent, false);
        return new RatingsListRecyclerViewAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RatingsListRecyclerViewAdapter.ViewHolder holder, int position) {
        holder.mItem = allRating.get(position);
        holder.mImgView.setImageResource(R.drawable.ic_menu_gallery);
        holder.mNameView.setText(allRating.get(position).rating.getUser());
        holder.mValueView.setText(((Integer)allRating.get(position).rating.getValue()).toString());
        holder.mCommentView.setText(allRating.get(position).rating.getComment());
    }

    @Override
    public int getItemCount() {
        return allRating.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final ImageView mImgView;
        public final TextView mNameView;
        public final TextView mValueView;
        public final TextView mCommentView;

        public RatingItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mImgView = (ImageView) view.findViewById(R.id.rating_user_img);
            mNameView = view.findViewById(R.id.rating_user_name);
            mValueView = view.findViewById(R.id.rating_user_value);
            mCommentView = view.findViewById(R.id.rating_user_comment);
        }

    }
}
