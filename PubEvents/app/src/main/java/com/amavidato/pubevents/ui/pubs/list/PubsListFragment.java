package com.amavidato.pubevents.ui.pubs.list;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Build;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralListFragment;
import com.amavidato.pubevents.utility.list_abstract_classes.GeneralRecyclerViewAdapter;
import com.amavidato.pubevents.utility.list_abstract_classes.MyItem;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;

import java.util.List;
import java.util.Map;

public class PubsListFragment extends GeneralListFragment {

    private static final String TAG = PubsListFragment.class.getSimpleName();
    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public PubsListFragment() {
    }

    @Override
    protected void initFilterAndSort() {
        initSpinners(new FilterOptionsPub(),new SortOptionsPub(), R.array.filter_options_pubs, R.array.sort_options_pubs);
    }

    @Override
    protected View createSpecificListLayout() {
        return (LinearLayout) getLayoutInflater().inflate(R.layout.fragment_publist, null);
    }

    @Override
    protected View createSpecificRecyclerView() {
        return specificListLayout.findViewById(R.id.frag_recycler_view_publist);
    }

    @Override
    protected void fillModelObjectValues(final DocumentSnapshot document, final List<MyItem> items, final int[] itemsDone, final int numItems, final RecyclerView recyclerView) {
        Map<String, Object> data = document.getData();
        Log.d(TAG, document.getId() + " => " + data);

        final Pub pub = new Pub();
        pub.setName((String) data.get(DBManager.CollectionsPaths.PubFields.NAME));
        pub.setGeoLocation((GeoPoint) data.get(DBManager.CollectionsPaths.PubFields.GEOLOCATION));
        pub.setAverageAge(((Long) data.get(DBManager.CollectionsPaths.PubFields.AVG_AGE)).intValue());
        pub.setPrice(Pub.Price.valueOf(((String) data.get(DBManager.CollectionsPaths.PubFields.PRICE)).toUpperCase()));
        Object ratingTemp = data.get(DBManager.CollectionsPaths.PubFields.OVERALL_RATING);
        if (ratingTemp instanceof Double) {
            pub.setOverallRating((Double) ratingTemp);
        } else if (ratingTemp instanceof Long) {
            pub.setOverallRating(((Long) ratingTemp).doubleValue());
        }
        final String cityID = (String) data.get(DBManager.CollectionsPaths.PubFields.CITY);
        //Nested query to retrieve city's information
        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.CITY)
                .document(cityID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            Map<String, Object> data = task.getResult().getData();
                            Log.d(TAG + "--CITy", task.getResult().getId() + " => " + data);
                            pub.setCity((String) data.get(DBManager.CollectionsPaths.CityFields.NAME));
                            addItemToList(items,new PubItem(document.getId(),pub),itemsDone,numItems,recyclerView);
                        } else {
                            Log.d(TAG, "Error getting documents: ", task.getException());
                            pub.setCity("ERROR");
                        }
                    }
                });
    }

    @Override
    protected boolean getUserDependedListFragArgument() {
        return PubsListFragmentArgs.fromBundle(getArguments()).getStringOpenUserDependentList();
    }

    @Override
    protected String getGeneralPathQueryList() {
        return DBManager.CollectionsPaths.PUBS;
    }


    @Override
    protected String getUserDependedPathQueryList(String uid) {
        return DBManager.CollectionsPaths.USERS + "/" + uid + "/" + DBManager.CollectionsPaths.UserFields.FOLLOWED_PUBS;
    }

    @Override
    protected GeneralRecyclerViewAdapter getConcreteRecyclerViewAdapter(List<MyItem> items, FragmentActivity activity) {
        return new PubListRecyclerViewAdapter(items,activity);
    }

}