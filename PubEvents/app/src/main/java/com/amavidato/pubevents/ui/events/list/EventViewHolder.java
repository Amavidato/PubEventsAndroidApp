package com.amavidato.pubevents.ui.events.list;

import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.utility.general_list_fragment.GeneralViewHolder;

public class EventViewHolder extends GeneralViewHolder {
    public final ImageView mImgView;
    public final GridLayout mContentView;
    public final TextView mNameView;
    public final TextView mPubNameView;

    public EventViewHolder(View view) {
        super(view);
        this.mImgView = view.findViewById(R.id.event_item_image);
        this.mContentView = view.findViewById(R.id.event_item_content);
        this.mNameView = view.findViewById(R.id.event_item_name);
        this.mPubNameView = view.findViewById(R.id.event_item_pub);
    }
}
