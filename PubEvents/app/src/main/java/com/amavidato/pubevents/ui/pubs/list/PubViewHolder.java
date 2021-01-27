package com.amavidato.pubevents.ui.pubs.list;

import android.view.View;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.utility.ImageManager;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralViewHolder;

public class PubViewHolder extends GeneralViewHolder {
    public final ImageView mImgView;
    public final GridLayout mContentView;
    public final TextView mNameView;
    public final TextView mCityView;
    public final TextView mRatingView;
    public final ImageView mPrice1;
    public final ImageView mPrice2;
    public final ImageView mPrice3;

    public PubViewHolder(View view) {
        super(view);
        mImgView = view.findViewById(R.id.pub_item_image);
        mContentView = view.findViewById(R.id.pub_item_content);
        mNameView = view.findViewById(R.id.pub_item_name);
        mCityView = view.findViewById(R.id.pub_item_city);
        mRatingView = view.findViewById(R.id.pub_item_rating_value);
        mPrice1 = view.findViewById(R.id.pub_item_price_1);
        mPrice2 = view.findViewById(R.id.pub_item_price_2);
        mPrice3 = view.findViewById(R.id.pub_item_price_3);
    }

    public void setPrice(Pub.Price price){
        switch (price){
            case LOW:
                mPrice1.setVisibility(View.VISIBLE);
                mPrice2.setVisibility(View.GONE);
                mPrice3.setVisibility(View.GONE);
                break;
            case MEDIUM:
                mPrice1.setVisibility(View.VISIBLE);
                mPrice2.setVisibility(View.VISIBLE);
                mPrice3.setVisibility(View.GONE);
                break;
            case HIGH:
                mPrice1.setVisibility(View.VISIBLE);
                mPrice2.setVisibility(View.VISIBLE);
                mPrice3.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }

    }

    public void initImage(){
        String path = "images/pubs/" + mItem.id + ".jpg";
        ImageManager.loadImageIntoView(path,mImgView);
    }
}
