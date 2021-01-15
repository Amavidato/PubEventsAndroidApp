package com.amavidato.pubevents.ui.pubs.list;

import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.utility.general_list_fragment.GeneralViewHolder;

public class PubViewHolder extends GeneralViewHolder {
    public final ImageView mImgView;
    public final GridLayout mContentView;
    public final TextView mNameView;
    public final TextView mCityView;

    public PubViewHolder(View view) {
        super(view);
        mImgView = view.findViewById(R.id.pub_item_image);
        mContentView = view.findViewById(R.id.pub_item_content);
        mNameView = view.findViewById(R.id.pub_item_name);
        mCityView = view.findViewById(R.id.pub_item_city);
    }
}
