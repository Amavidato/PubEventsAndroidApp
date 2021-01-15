package com.amavidato.pubevents.ui.events.list;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.ui.pubs.list.FilterOptionsPub;
import com.amavidato.pubevents.utility.MyFragment;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.general_list_fragment.GeneralListFragment;
import com.amavidato.pubevents.utility.general_list_fragment.MyItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;

public class EventListFragment extends GeneralListFragment {

    private static final String TAG = EventListFragment.class.getSimpleName();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventListFragment() {
    }

    @Override
    protected void initFilterAndSort() {
        initSpinners(new FilterOptionsEvent(),new SortOptionsEvent(), R.array.filter_options_events, R.array.sort_options_events);
    }

    @Override
    protected View createSpecificListLayout() {
        return (LinearLayout) getLayoutInflater().inflate(R.layout.fragment_eventlist, null);
    }

    @Override
    protected View createSpecificRecyclerView() {
        return specificListLayout.findViewById(R.id.frag_recycler_view_eventlist);
    }
/*
    @Override
    protected void popolateSpecificRecyclerView() {
        if (specificRecyclerView instanceof RecyclerView) {
            progressBar.setVisibility(View.VISIBLE);
            final Context context = specificRecyclerView.getContext();
            final RecyclerView recyclerView = (RecyclerView) specificRecyclerView;
            recyclerView.setLayoutManager(new LinearLayoutManager(context));

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(divider);
            //Query to retrieve pubs information
            String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            if(uid!=null){
                FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS+"/"+uid+"/"+DBManager.CollectionsPaths.UserFields.ACQUIRED_EVENTS)
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        final List<MyItem> events = new ArrayList<>();
                        final int[] acquiredEvents = {task.getResult().size()};
                        for(final QueryDocumentSnapshot document : task.getResult()){

                            final String eventID = document.getId();

                            FirebaseFirestore.getInstance().document(DBManager.CollectionsPaths.EVENTS+"/"+eventID).get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if(task.isSuccessful()){
                                                Map<String, Object> data = task.getResult().getData();
                                                Log.d(TAG, "=>"+task.getResult().getId() + "=> " + data);

                                                final Event event = new Event();
                                                event.setName((String)data.get(DBManager.CollectionsPaths.EventFields.NAME));
                                                event.setDate(((Timestamp) data.get(DBManager.CollectionsPaths.EventFields.DATE)).toDate());
                                                Object eTmp = data.get(DBManager.CollectionsPaths.EventFields.PRICE);
                                                if(eTmp instanceof Double){
                                                    event.setPrice(((Double)eTmp));
                                                }else {//if (eTmp instanceof Long){
                                                    event.setPrice(((Long)eTmp).doubleValue());
                                                }
                                                String type = (String) data.get(DBManager.CollectionsPaths.EventFields.TYPE);
                                                event.setType(Event.EventType.valueOf(type.toUpperCase()));
                                                event.setReserved_seats(((Long)data.get(DBManager.CollectionsPaths.EventFields.RESERVED_SEATS)).intValue());
                                                event.setMax_capacity(((Long)data.get(DBManager.CollectionsPaths.EventFields.MAX_CAPACITY)).intValue());

                                                String pubID = (String)data.get(DBManager.CollectionsPaths.EventFields.PUB);
                                                FirebaseFirestore.getInstance().document(DBManager.CollectionsPaths.PUBS+"/"+pubID)
                                                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                        String pubName = (String) task.getResult().getData().get(DBManager.CollectionsPaths.PubFields.NAME);
                                                        event.setPub(pubName);
                                                        events.add(new EventItem(eventID, event));
                                                        acquiredEvents[0]--;
                                                        if(acquiredEvents[0] == 0){
                                                            recyclerAdapter = new EventListRecyclerViewAdapter(events,getActivity());
                                                            recyclerView.setAdapter(recyclerAdapter);
                                                            initializeRecyclerAdapter(events);
                                                            return;
                                                        }
                                                    }
                                                });                                        }else{
                                                Log.d(TAG, "ERROR");
                                            }
                                        }
                                    });
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }
        }
    }*/

    @Override
    protected void fillModelObjectValues(DocumentSnapshot document, List<MyItem> items, int[] itemsDone, int numItems, RecyclerView recyclerView) {
        
    }

    @Override
    protected boolean getUserDependedListFragArgument() {
        return false;
    }

    @Override
    protected String getGeneralPathQueryList() {
        return null;
    }

    @Override
    protected String getUserDependedPathQueryList(String uid) {
        return null;
    }
}