package com.amavidato.pubevents.ui.pubs.pub.ratings;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralViewHolder;

public class RatingViewHolder extends GeneralViewHolder {
    public final ImageView mImgView;
    public final TextView mNameView;
    public final TextView mValueView;
    public final TextView mCommentView;

    public RatingViewHolder(View view) {
        super(view);
        mImgView = (ImageView) view.findViewById(R.id.rating_user_img);
        mNameView = view.findViewById(R.id.rating_user_name);
        mValueView = view.findViewById(R.id.rating_user_value);
        mCommentView = view.findViewById(R.id.rating_user_comment);
    }
}
