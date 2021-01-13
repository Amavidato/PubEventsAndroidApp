package com.amavidato.pubevents.ui.acquired_events;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.utility.db.DBManager;
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

public class AcquiredEventsFragment extends Fragment {

    private static final String TAG = AcquiredEventsFragment.class.getSimpleName();

    private ProgressBar progressBar;
    private AcquiredEventsRecyclerViewAdapter recyclerAdapter;
    private AppCompatSpinner spinnerFilter;
    enum FilterOptions {NAME,PUB};

    private SearchView searchView;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AcquiredEventsFragment() {
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_acquired_events, container, false);
        View listView = view.findViewById(R.id.acquired_events_list);
        progressBar = view.findViewById(R.id.acquired_events_loading_list);
        progressBar.setVisibility(View.VISIBLE);
        spinnerFilter = view.findViewById(R.id.acquired_events_filter_option);
        spinnerFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(view != null){
                    String selected = ((TextView) view).getText().toString();
                    if(recyclerAdapter != null){
                        recyclerAdapter.onFilterOptSelected(FilterOptions.valueOf(selected.toUpperCase()),searchView.getQuery().toString());
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        // Set the adapter
        if (listView instanceof RecyclerView) {
            progressBar.setVisibility(View.VISIBLE);
            final Context context = listView.getContext();
            final RecyclerView recyclerView = (RecyclerView) listView;
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
                        final List<EventItem> events = new ArrayList<>();
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
                                                            recyclerAdapter = new AcquiredEventsRecyclerViewAdapter(events,getActivity());
                                                            recyclerView.setAdapter(recyclerAdapter);
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

        setHasOptionsMenu(true);

        return view;
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(true);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                //if query is empty, override default onClose by doing nothing
                if(searchView.getQuery().toString().isEmpty()){
                    return true;
                }
                //else run default onClose (it will delete the query)
                return false;
            }
        });

        searchView.setImeOptions(EditorInfo.IME_ACTION_DONE);
        searchView.setIconified(false);
        searchView.setQueryHint("Search...");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) { return false; }

            @Override
            public boolean onQueryTextChange(String s) {
                recyclerAdapter.getFilter().filter(s);
                return false;
            }
        });


        super.onPrepareOptionsMenu(menu);
    }


}