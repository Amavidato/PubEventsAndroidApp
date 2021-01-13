package com.amavidato.pubevents.ui.acquired_events;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class AcquiredEventsViewModel extends ViewModel {

    private MutableLiveData<String> mText;

    public AcquiredEventsViewModel() {
        mText = new MutableLiveData<>();
        mText.setValue("This is interested events fragment");
    }

    public LiveData<String> getText() {
        return mText;
    }
}