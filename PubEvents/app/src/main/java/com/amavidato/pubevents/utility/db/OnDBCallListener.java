package com.amavidato.pubevents.utility.db;

import com.google.firebase.firestore.QuerySnapshot;

public interface OnDBCallListener {

    public void onDocumentReceivedCallback();
    public void onColletionReceivedCallback(QuerySnapshot result);
}
