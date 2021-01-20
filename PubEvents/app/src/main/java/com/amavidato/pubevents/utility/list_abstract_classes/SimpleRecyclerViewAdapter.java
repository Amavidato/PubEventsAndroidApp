package com.amavidato.pubevents.utility.list_abstract_classes;

import android.app.Activity;
import android.util.Log;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public abstract class SimpleRecyclerViewAdapter extends RecyclerView.Adapter<GeneralViewHolder>{

    private static final String TAG = SimpleRecyclerViewAdapter.class.getSimpleName();

    protected final List<MyItem> allItems;
    protected Activity mActivity;

    protected SimpleRecyclerViewAdapter(List<MyItem> allItems, Activity activity) {
        this.allItems = allItems;
        this.mActivity = activity;
    }

    @Override
    public GeneralViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return customOnCreateViewHolder(parent,viewType);
    }

    @Override
    public void onBindViewHolder(final GeneralViewHolder holder, int position) {
        if(holder != null){
            customOnBindViewHolder(holder,position);
        }else{
            Log.d("PubEventListRVA","ERROR onBindViewHolder: ViewHolderNull");
        }
    }

    @Override
    public int getItemCount() {
        return allItems.size();
    }

    protected abstract GeneralViewHolder customOnCreateViewHolder(ViewGroup parent, int viewType);

    protected abstract void customOnBindViewHolder(GeneralViewHolder holder, int position);



}
