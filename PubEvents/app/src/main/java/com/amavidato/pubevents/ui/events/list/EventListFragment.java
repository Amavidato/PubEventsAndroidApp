package com.amavidato.pubevents.ui.events.list;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Event;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralListFragment;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralRecyclerViewAdapter;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Map;

import static java.lang.String.valueOf;

public class EventListFragment extends GeneralListFragment{// implements FirebaseAuth.AuthStateListener {

    private static final String TAG = EventListFragment.class.getSimpleName();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public EventListFragment() {
    }

   /* @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        FirebaseAuth.getInstance().addAuthStateListener(this);
        return super.onCreateView(inflater, container, savedInstanceState);
    }*/

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

    @Override
    protected void fillModelObjectValues(final DocumentSnapshot document, final List<MyItem> items, final int[] itemsDone, final int numItems, final RecyclerView recyclerView) {
        Map<String, Object> data = document.getData();
        Log.d(TAG, document.getId() + " => " + data);

        final Event event = new Event();
        String stmp = (String)data.get(DBManager.CollectionsPaths.EventFields.NAME);
        event.setName(stmp != null ? stmp : "NULL");

        Timestamp ttmp = (Timestamp) data.get(DBManager.CollectionsPaths.EventFields.DATE);
        event.setDate(ttmp != null ? ttmp.toDate() : null);

        Object ptmp = data.get(DBManager.CollectionsPaths.EventFields.PRICE);
        Double price = null;
        if(ptmp != null){
            if(ptmp instanceof Double){
                price = (Double)ptmp;
            }else {//if (eTmp instanceof Long){
                price = ((Long)ptmp).doubleValue();
            }
        }
        event.setPrice(price);

        stmp = (String) data.get(DBManager.CollectionsPaths.EventFields.TYPE);
        stmp = stmp != null ? stmp : "NULL";
        event.setType(Event.EventType.valueOf(stmp.toUpperCase()));

        Long ltmp = (Long)data.get(DBManager.CollectionsPaths.EventFields.RESERVED_SEATS);
        ltmp = ltmp != null ? ltmp : new Long(0);
        event.setReserved_seats(ltmp.intValue());

        ltmp = (Long)data.get(DBManager.CollectionsPaths.EventFields.MAX_CAPACITY);
        event.setMax_capacity(ltmp != null ? ltmp.intValue() : null);

        final String pubID = (String)data.get(DBManager.CollectionsPaths.EventFields.PUB);
        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                .document(pubID)
                .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Map<String, Object> data = task.getResult().getData();
                    Log.d(TAG, task.getResult().getId() + " => " + data);

                    final Pub pub = new Pub();
                    pub.setName((String) data.get(DBManager.CollectionsPaths.PubFields.NAME));
                    pub.setGeoLocation((GeoPoint) data.get(DBManager.CollectionsPaths.PubFields.GEOLOCATION));
                    pub.setAverageAge(((Long) data.get(DBManager.CollectionsPaths.PubFields.AVG_AGE)).intValue());
                    event.setPub(pub);
                    addItemToList(items, new EventItem(document.getId(), event), itemsDone, numItems, recyclerView);
                }else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    @Override
    protected boolean getUserDependedListFragArgument() {
        return EventListFragmentArgs.fromBundle(getArguments()).getStringOpenUserDependentList();
    }

    @Override
    protected String getGeneralPathQueryList() { return DBManager.CollectionsPaths.EVENTS; }

    @Override
    protected String getUserDependedPathQueryList(String uid) {
        return DBManager.CollectionsPaths.USERS+"/"+uid+"/"+DBManager.CollectionsPaths.UserFields.ACQUIRED_EVENTS;
    }

    @Override
    protected GeneralRecyclerViewAdapter getConcreteRecyclerViewAdapter(List<MyItem> items, FragmentActivity activity) {
        return new EventListRecyclerViewAdapter(items,activity);
    }
    /*private void backToHome(){
        Intent intent = new Intent(getContext(), MainActivity.class);
        intent.putExtra("dialog_str", "Sorry! It seems that the account's token was obsolete. Please, Login again with your account."); // so you can pass what activity you're coming from, if needed
        startActivity(intent);
        this.getActivity().finish();

        //FragmentTransaction transaction = getFragmentManager().beginTransaction();
        //transaction.replace(R.id.nav_host_fragment, new HomeFragment());
        //transaction.commit();
    }*/

    /*@Override
    public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if(user != null){
            Log.d(TAG, "Current User NOT NULL:"+user);
        }else{
            Log.d(TAG, "Current User ***NULL***");
            backToHome();
        }
    }*/
}