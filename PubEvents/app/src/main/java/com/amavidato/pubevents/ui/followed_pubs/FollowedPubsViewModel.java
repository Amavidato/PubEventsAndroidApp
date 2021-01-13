package com.amavidato.pubevents.ui.followed_pubs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class FollowedPubsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public FollowedPubsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is followed pubs fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}