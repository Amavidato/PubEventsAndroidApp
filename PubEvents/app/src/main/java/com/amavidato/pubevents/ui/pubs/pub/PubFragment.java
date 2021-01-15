package com.amavidato.pubevents.ui.pubs.pub;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.RecyclerView;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.model.Pub;
import com.amavidato.pubevents.model.Rating;
import com.amavidato.pubevents.ui.pubs.pub.ratings.RatingItem;
import com.amavidato.pubevents.ui.pubs.pub.ratings.RatingsListRecyclerViewAdapter;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.MyFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PubFragment extends MyFragment {

    private static final String TAG = PubFragment.class.getSimpleName();
    private PubViewModel pubViewModel;
    private TextView pub_name;
    private TextView pub_rating;
    private TextView pub_city;
    private ImageView pub_img;
    private CardView pub_follow_btn;
    private ImageView pub_follow_btn_add;
    private ImageView pub_follow_btn_remove;
    private ImageView pub_price_1;
    private ImageView pub_price_2;
    private ImageView pub_price_3;
    private TextView pub_avg_age;
    private AppCompatButton btn_open_ratings;
    private ProgressBar progressBar;
    private ConstraintLayout containerLayout;
    private RatingsListRecyclerViewAdapter recyclerAdapter;
    private ConstraintLayout ratingsLayoutContainer;
    private ConstraintLayout pubContainerRatings;
    private ConstraintLayout pubContainerRatingForm;
    private AppCompatButton btn_add_rating;

    private String pubID;

    public PubFragment() {}

    public PubFragment(String pubID) {
        this.pubID = pubID;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d("PubFragment","CREATE VIEW");
        Log.d(TAG,PubFragmentArgs.fromBundle(getArguments()).toString());
        pubID = PubFragmentArgs.fromBundle(getArguments()).getStringPubId();
        pubViewModel =
                ViewModelProviders.of(this).get(PubViewModel.class);
        View root = inflater.inflate(R.layout.fragment_pub, container, false);

        pub_name = root.findViewById(R.id.pubview_name);
        pub_city = root.findViewById(R.id.pubview_city);
        pub_rating = root.findViewById(R.id.pubview_overall_rating);
        pub_img = root.findViewById(R.id.pubview_image);
        pub_follow_btn = root.findViewById(R.id.pubview_follow_btn);
        pub_follow_btn_add = root.findViewById(R.id.pubview_follow_btn_add);
        pub_follow_btn_remove = root.findViewById(R.id.pubview_follow_btn_rmv);

        pub_follow_btn_add.setVisibility(View.GONE);
        pub_follow_btn_remove.setVisibility(View.GONE);

        if(FirebaseAuth.getInstance().getCurrentUser()!= null){
            pub_follow_btn.setVisibility(View.VISIBLE);
            String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS)
                    .document(uid)
                    .collection(DBManager.CollectionsPaths.UserFields.FOLLOWED_PUBS)
                    .document(pubID)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        if(task.getResult().exists()){
                            pub_follow_btn_add.setVisibility(View.GONE);
                            pub_follow_btn_remove.setVisibility(View.VISIBLE);
                        }else{
                            pub_follow_btn_add.setVisibility(View.VISIBLE);
                            pub_follow_btn_remove.setVisibility(View.GONE);
                        }
                    }else{

                    }
                }
            });
        }else{
            pub_follow_btn.setVisibility(View.GONE);
        }

        pub_follow_btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if(uid != null){
                    HashMap<String,String> data = new HashMap<>();
                    FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS)
                            .document(uid)
                            .collection(DBManager.CollectionsPaths.UserFields.FOLLOWED_PUBS)
                            .document(pubID)
                            .set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            pub_follow_btn_add.setVisibility(View.GONE);
                            pub_follow_btn_remove.setVisibility(View.VISIBLE);
                            Toast.makeText(PubFragment.this.getContext(),"Now you follow this pub! click the '-' button to unfollow it.",Toast.LENGTH_SHORT).show();
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PubFragment.this.getContext(),"Addition to followed pubs failed. ERROR: " + e,Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
        pub_follow_btn_remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if(uid!=null){
                    FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS)
                            .document(uid)
                            .collection(DBManager.CollectionsPaths.UserFields.FOLLOWED_PUBS)
                            .document(pubID)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(PubFragment.this.getContext(),"Pub unfollowed",Toast.LENGTH_SHORT).show();
                                    pub_follow_btn_remove.setVisibility(View.GONE);
                                    pub_follow_btn_add.setVisibility(View.VISIBLE);
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(PubFragment.this.getContext(),"Deletion of followed pubs failed. ERROR: " + e,Toast.LENGTH_SHORT).show();
                        }
                    });
                }

            }
        });

        btn_add_rating = root.findViewById(R.id.rating_form_add_btn);
        pub_price_1 = root.findViewById(R.id.price_img_1);
        pub_price_2 = root.findViewById(R.id.price_img_2);
        pub_price_3 = root.findViewById(R.id.price_img_3);
        pub_avg_age = root.findViewById(R.id.avg_age_value);
        btn_open_ratings = root.findViewById(R.id.pub_btn_open_ratings);
        progressBar = root.findViewById(R.id.pub_loading_list_progressBar);
        containerLayout = root.findViewById(R.id.pub_fragment_layout_container);
        pubContainerRatings = root.findViewById(R.id.pub_container_ratings);
        pubContainerRatingForm = root.findViewById(R.id.pubview_container_rating_form);

        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS).document(pubID).get()
                .addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
                    @Override
                    public void onSuccess(DocumentSnapshot documentSnapshot) {
                        Map<String, Object> data = documentSnapshot.getData();
                        pub_name.setText((String)data.get(DBManager.CollectionsPaths.PubFields.NAME));
                        Object rTmp = data.get(DBManager.CollectionsPaths.PubFields.OVERALL_RATING);
                        if(rTmp instanceof Double){
                            pub_rating.setText(((Double)rTmp).toString());
                        }else if (rTmp instanceof Long){
                            pub_rating.setText(((Long)rTmp).toString());
                        }
                        switch (Pub.Price.valueOf(((String)data.get(DBManager.CollectionsPaths.PubFields.PRICE)).toUpperCase())){
                            case LOW:
                                pub_price_1.setImageResource(R.drawable.ic_price_24);
                                break;
                            case MEDIUM:
                                pub_price_1.setImageResource(R.drawable.ic_price_24);
                                pub_price_2.setImageResource(R.drawable.ic_price_24);
                                break;
                            case HIGH:
                                pub_price_1.setImageResource(R.drawable.ic_price_24);
                                pub_price_2.setImageResource(R.drawable.ic_price_24);
                                pub_price_3.setImageResource(R.drawable.ic_price_24);
                                break;
                            default:
                                break;
                        }
                        pub_avg_age.setText(((Long)data.get(DBManager.CollectionsPaths.PubFields.AVG_AGE)).toString());

                        btn_open_ratings.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                btn_open_ratings.setVisibility(View.GONE);
                                OpenRatings();
                            }
                        });
                        btn_add_rating.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                final int rating = Integer.decode(((Spinner)((ViewGroup)(view.getParent())).findViewById(R.id.rating_form_value)).getSelectedItem().toString());
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                String name = user.getDisplayName();
                                String email = user.getEmail();

                                String comment = ((EditText) ((ViewGroup)(view.getParent())).findViewById(R.id.rating_form_comment)).getText().toString();

                                final Map<String, Object> data = new HashMap<>();
                                data.put(DBManager.CollectionsPaths.PubFields.Ratings.USER,name);
                                data.put(DBManager.CollectionsPaths.PubFields.Ratings.RATING,rating);
                                if(comment != null && !comment.equals("")){
                                    data.put(DBManager.CollectionsPaths.PubFields.Ratings.COMMENT,comment);
                                }
                                FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS).document(pubID)
                                        .collection(DBManager.CollectionsPaths.PubFields.RATINGS).document(email).set(data).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d("ADD_RATING","Rating added succesfully");
                                        Toast.makeText(PubFragment.this.getContext(),"Your rating has been added succesfully.",Toast.LENGTH_SHORT).show();
                                        updatePubOverallRanking(rating);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(PubFragment.this.getContext(),"ERROR: Your rating has not been added.\nCheck your connection and Try again.",Toast.LENGTH_SHORT).show();
                                        Log.d("ADD_RATING","ERROR: rating not added. Error msg:"+e);
                                    }
                                });
                            }
                        });
                    }
                });
        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS)
                .document(pubID)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(task.isSuccessful()){
                            Map<String, Object> data = task.getResult().getData();
                            String cityID = (String) data.get(DBManager.CollectionsPaths.PubFields.CITY);
                            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.CITY)
                                    .document(cityID)
                                    .get()
                                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                Map<String, Object> data = task.getResult().getData();
                                                final String cityName = (String) data.get(DBManager.CollectionsPaths.CityFields.NAME);
                                                String provID = (String) data.get(DBManager.CollectionsPaths.CityFields.province);
                                                FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PROVINCE)
                                                        .document(provID)
                                                        .get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                if(task.isSuccessful()){
                                                                    Map<String, Object> data = task.getResult().getData();
                                                                    final String provName = (String) data.get(DBManager.CollectionsPaths.ProvinceFields.SHORT_NAME);
                                                                    String regID = (String) data.get(DBManager.CollectionsPaths.ProvinceFields.REGION);
                                                                    FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.REGIONS)
                                                                            .document(regID)
                                                                            .get()
                                                                            .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                                                    if(task.isSuccessful()){
                                                                                        Map<String, Object> data = task.getResult().getData();
                                                                                        String regName = (String) data.get(DBManager.CollectionsPaths.RegionFields.NAME);
                                                                                        pub_city.setText(cityName+" ("+ provName+") - "+regName);
                                                                                    }else{

                                                                                    }
                                                                                }
                                                                            });
                                                                }else{

                                                                }
                                                            }
                                                        });
                                            }else{

                                            }
                                        }
                                    });
                        }else{

                        }
                    }
                });

        return root;
    }

    private void updatePubOverallRanking(final int rating) {
        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS).document(pubID).collection(DBManager.CollectionsPaths.PubFields.RATINGS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    double allRatings = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> data = document.getData();
                        Log.d(TAG, document.getId() + " => " + data);
                        allRatings += ((Long) data.get(DBManager.CollectionsPaths.PubFields.Ratings.RATING)).intValue();
                    }
                    double overallRating = allRatings / task.getResult().size();
                    Log.d("OVERALL RATING:","Value: "+overallRating);

                    Map ratingData = new HashMap<String,Float>();
                    ratingData.put(DBManager.CollectionsPaths.PubFields.OVERALL_RATING,overallRating);
                    FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS).document(pubID)
                            .update(DBManager.CollectionsPaths.PubFields.OVERALL_RATING,overallRating)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d("OVERALL RATING:","updated successfully");
                                    refreshFragment();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("OVERALL RATING:","ERROR: Not updated");
                        }
                    });
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                    Toast.makeText(PubFragment.this.getContext(),"Error getting pub list from database.\n Please check your connection and try again.",Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void OpenRatings() {
        progressBar.setVisibility(View.VISIBLE);
        //ratingsLayoutContainer = (ConstraintLayout) View.inflate(this.getContext(),R.layout.pub_ratings_list, (ViewGroup) getView());
        ratingsLayoutContainer = (ConstraintLayout) getLayoutInflater().inflate(R.layout.pub_ratings_list, null);

        // Set the adapter
        View listView = ratingsLayoutContainer.findViewById(R.id.pub_ratings);
        if (listView instanceof RecyclerView) {
            final Context context = listView.getContext();
            final RecyclerView recyclerView = (RecyclerView) listView;

            DividerItemDecoration divider = new DividerItemDecoration(recyclerView.getContext(),DividerItemDecoration.VERTICAL);
            recyclerView.addItemDecoration(divider);
            final List<RatingItem> ratings = new ArrayList<>();
            //Query to retrieve ratings information

            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.PUBS).document(pubID).collection(DBManager.CollectionsPaths.PubFields.RATINGS).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @RequiresApi(api = Build.VERSION_CODES.N)
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            final Rating rating = new Rating();
                            Map<String, Object> data = document.getData();
                            Log.d(TAG, document.getId() + " => " + data);

                            rating.setValue(((Long) data.get(DBManager.CollectionsPaths.PubFields.Ratings.RATING)).intValue());
                            rating.setUser((String)data.get(DBManager.CollectionsPaths.PubFields.Ratings.USER));
                            rating.setEmail((String)data.get(DBManager.CollectionsPaths.PubFields.Ratings.EMAIL));
                            rating.setComment((String)data.get(DBManager.CollectionsPaths.PubFields.Ratings.COMMENT));

                            ratings.add(new RatingItem(rating));
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        recyclerAdapter = new RatingsListRecyclerViewAdapter(ratings);
                        recyclerView.setAdapter(recyclerAdapter);

                        if(FirebaseAuth.getInstance().getCurrentUser()==null){
                            pubContainerRatingForm.setVisibility(View.GONE);
                        }else{
                            pubContainerRatingForm.setVisibility(View.VISIBLE);
                        }
                        pubContainerRatings.addView(ratingsLayoutContainer);

                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                        Toast.makeText(context,"Error getting pub list from database.\n Please check your connection and try again.",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        ViewGroup parent = (ViewGroup) pub_img.getParent().getParent();
        Log.d("OnBackPressed",this.getClass().getSimpleName());
        if(parent != null){
            parent.removeView((View)pub_img.getParent());
        }
    }

    private void refreshFragment(){
        Log.d("PubFragment","Refreshing fragment");
        //Fragment f = null;
        //f = this.getActivity().getSupportFragmentManager().findFragmentById(R.id.pubFragment);
        FragmentTransaction ft = this.getFragmentManager().beginTransaction();
        ft.detach(this);
        ft.attach(this);
        ft.commit();
    }
}
