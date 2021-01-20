package com.amavidato.pubevents.utility.list_abstract_classes;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class GeneralViewHolder extends RecyclerView.ViewHolder {
    public final View mView;

    public MyItem mItem;

    public GeneralViewHolder(View view) {
        super(view);
        mView = view;
    }

}