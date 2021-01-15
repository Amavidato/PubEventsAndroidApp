package com.amavidato.pubevents.ui.pubs.pub;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class PubViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public PubViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is PUB fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}
