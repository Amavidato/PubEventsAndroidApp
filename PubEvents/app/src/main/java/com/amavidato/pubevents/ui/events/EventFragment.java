package com.amavidato.pubevents.ui.events;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.utility.AccountManager;
import com.amavidato.pubevents.utility.ImageManager;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.MyFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class EventFragment extends MyFragment {

    private static final String TAG = EventFragment.class.getSimpleName();
    private String eventID;

    private EventViewModel eventViewModel;
    private TextView event_name;
    private ImageView event_img;
    private TextView event_date;
    private TextView event_time;
    private TextView event_pub;
    private TextView event_price;
    private TextView event_maxCapacity;
    private TextView event_reservedSeats;
    private TextView event_type;
    private TextView event_bought_number;
    private TextView event_buy_price;
    private TextView event_buy_tot;
    private TextInputEditText event_buy_input;
    private Button event_buy_btn;

    private ConstraintLayout ticketsInfoUserContainerLayout;

    public EventFragment() {}

    public EventFragment(String eventID) {
        this.eventID = eventID;
    }

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG,EventFragmentArgs.fromBundle(getArguments()).toString());
        eventID = EventFragmentArgs.fromBundle(getArguments()).getStringEventId();
        eventViewModel =
                ViewModelProviders.of(this).get(EventViewModel.class);
        View root = inflater.inflate(R.layout.fragment_event, container, false);

        event_name = root.findViewById(R.id.eventview_name);
        event_img = root.findViewById(R.id.eventview_image);
        event_pub = root.findViewById(R.id.eventview_pub);
        event_date = root.findViewById(R.id.eventview_date);
        event_time = root.findViewById(R.id.eventview_time);
        event_reservedSeats = root.findViewById(R.id.eventview_reserved_seats);
        event_maxCapacity = root.findViewById(R.id.eventview_max_capacity);
        event_price = root.findViewById(R.id.eventview_price);
        event_type = root.findViewById(R.id.eventview_type);
        event_bought_number = root.findViewById(R.id.eventview_tickets_bought_number);
        event_buy_price = root.findViewById(R.id.eventview_buy_value_ticket);
        event_buy_tot = root.findViewById(R.id.eventview_buy_tot_value);
        event_buy_input = root.findViewById(R.id.eventview_buy_number);
        event_buy_btn = root.findViewById(R.id.eventview_buy_btn);
        ticketsInfoUserContainerLayout = root.findViewById(R.id.eventview_tickets_info_user);


        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        if(AccountManager.isCurrentUserValid()){
            ticketsInfoUserContainerLayout.setVisibility(View.VISIBLE);

            String uid = user.getEmail();
            Log.d(TAG,  "USER_ID => " + uid);

            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS)
                    .document(uid)
                    .collection(DBManager.CollectionsPaths.UserFields.ACQUIRED_EVENTS)
                    .document(eventID)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                Map<String,Object> data = task.getResult().getData();
                                Log.d(TAG, task.getResult().getId() + " => " + data);
                                if(data != null){
                                    Object tmp = data.get(DBManager.CollectionsPaths.UserFields.AcquiredEventsFields.TICKETS_BOUGHT);
                                    if(tmp instanceof Double){
                                        event_bought_number.setText(((Integer)((Double)tmp).intValue()).toString());
                                    }else if (tmp instanceof Long){
                                        event_bought_number.setText(((Integer)((Long)tmp).intValue()).toString());
                                    }
                                }else{
                                    Log.d(TAG, "get bought tickets DATA NULL");
                                }
                            }else{

                            }
                        }
                    });
        }else{
            ticketsInfoUserContainerLayout.setVisibility(View.GONE);
        }

        event_buy_input.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                Double pricePerUnit = Double.parseDouble(event_buy_price.getText().toString());
                String s = event_buy_input.getText().toString();
                Double numberOfTickets = Double.parseDouble(s.isEmpty() ? "0" : s);
                Double tot = pricePerUnit * numberOfTickets;
                event_buy_tot.setText(BigDecimal.valueOf(tot).setScale(2).toString());
            }
        });

        FirebaseFirestore.getInstance().document(DBManager.CollectionsPaths.EVENTS+"/"+eventID)
        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    Map<String,Object> data = task.getResult().getData();
                    Log.d(TAG, task.getResult().getId() + " => " + data);
                    if(data != null){
                        event_name.setText((String) data.get(DBManager.CollectionsPaths.EventFields.NAME));
                        event_pub.setText((String) data.get(DBManager.CollectionsPaths.EventFields.PUB));
                        Date date = ((Timestamp)data.get(DBManager.CollectionsPaths.EventFields.DATE)).toDate();
                        DateFormat formatterDate = new SimpleDateFormat("E, dd MMMM YYYY");
                        DateFormat formatterTime = new SimpleDateFormat("HH:mm");

                        event_date.setText(formatterDate.format(date));
                        event_time.setText(formatterTime.format(date));
                        event_reservedSeats.setText(((Long) data.get(DBManager.CollectionsPaths.EventFields.RESERVED_SEATS)).toString());
                        event_maxCapacity.setText(((Long) data.get(DBManager.CollectionsPaths.EventFields.MAX_CAPACITY)).toString());
                        Object pTmp = data.get(DBManager.CollectionsPaths.PubFields.OVERALL_RATING);
                        if(pTmp instanceof Double){
                            event_price.setText(BigDecimal.valueOf((Double) data.get(DBManager.CollectionsPaths.EventFields.PRICE)).setScale(2).toString());
                        }else{// if (pTmp instanceof Long){
                            event_price.setText(BigDecimal.valueOf((Double)(((Long) data.get(DBManager.CollectionsPaths.EventFields.PRICE)).doubleValue())).setScale(2).toString());

                        }
                        event_type.setText((String) data.get(DBManager.CollectionsPaths.EventFields.TYPE));
                        ImageManager.loadImageIntoView("images/events/"+event_type.getText()+".jpg",event_img);

                        event_buy_price.setText(event_price.getText());
                        String toBuy = event_buy_input.getText().toString();
                        Double tot = Double.parseDouble((String) event_price.getText()) * Double.parseDouble( toBuy.isEmpty() ? "0" : toBuy);
                        event_buy_tot.setText(BigDecimal.valueOf(tot).setScale(2).toString());
                    }else{
                        Log.d(TAG, "get event DATA NULL");
                    }

                }else{

                }
            }
        });

        event_buy_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkAndPerformPurchase();
            }
        });
        return root;
    }

    private void checkAndPerformPurchase() {
        String strToBuy = event_buy_input.getText().toString();
        final int toBuy = Integer.parseInt(strToBuy.isEmpty() ? "0" : strToBuy);
        if(toBuy>0){
            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.EVENTS)
                    .document(eventID)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        Map<String, Object> data = task.getResult().getData();
                        int maxCapacity = ((Long) data.get(DBManager.CollectionsPaths.EventFields.MAX_CAPACITY)).intValue();
                        int reservedSeats = ((Long)data.get(DBManager.CollectionsPaths.EventFields.RESERVED_SEATS)).intValue();
                        int availableSeats = maxCapacity - reservedSeats;
                        if(toBuy<=availableSeats){
                            buyTickets(toBuy,reservedSeats);
                        }else{
                            Toast.makeText(EventFragment.this.getContext(),"Sorry, there are not enough tickets available!",Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toast.makeText(EventFragment.this.getContext(),"Error retrieving data!\n Please check your internet connection and try again.",Toast.LENGTH_LONG).show();
                    }
                }
            });
        }else{
            Toast.makeText(EventFragment.this.getContext(),"Number not inserted!\n Please insert a valid number of thickets you want to buy",Toast.LENGTH_LONG).show();
        }
    }

    private void buyTickets(final int toBuy, final int oldReserved){
        final String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
        if(uid!=null) {
            FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS)
                    .document(uid)
                    .collection(DBManager.CollectionsPaths.UserFields.ACQUIRED_EVENTS)
                    .document(eventID)
                    .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        Map<String, Object> data = task.getResult().getData();
                        Log.d(TAG, task.getResult().getId() + " => " + data);

                        Integer tickets = 0;
                        if(data != null){
                            Object tmp = data.get(DBManager.CollectionsPaths.UserFields.AcquiredEventsFields.TICKETS_BOUGHT);
                            if (tmp instanceof Double) {
                                tickets = ((Double) tmp).intValue();
                            } else if (tmp instanceof Long) {
                                tickets = ((Long) tmp).intValue();
                            }
                            if (tickets == null) {
                                tickets = 0;
                            }
                        }else{
                            data = new HashMap<>();
                        }
                        data.put(DBManager.CollectionsPaths.UserFields.AcquiredEventsFields.TICKETS_BOUGHT, tickets + toBuy);
                        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS)
                                .document(uid)
                                .collection(DBManager.CollectionsPaths.UserFields.ACQUIRED_EVENTS)
                                .document(eventID)
                                .set(data)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        updateEventSeats(eventID, oldReserved+toBuy);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(EventFragment.this.getContext(), "ERROR: " + e, Toast.LENGTH_SHORT).show();
                            }
                        });

                        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.USERS)
                                .document(uid)
                                .collection(DBManager.CollectionsPaths.UserFields.LAST_EVENTS)
                                .document(eventID)
                                .set(new HashMap<String,String>())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "OnSuccess add Event in LastEvents");
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d(TAG,"OnFailure add Event in LastEvents. Error:"+e);
                                Toast.makeText(EventFragment.this.getContext(), "ERROR: " + e, Toast.LENGTH_SHORT).show();
                            }
                        });
                    } else {

                    }
                }
            });
        }
    }

    private void updateEventSeats(String eventID, int newReserved){
        Map<String, Object> data = new HashMap<>();
        data.put(DBManager.CollectionsPaths.EventFields.RESERVED_SEATS,newReserved);
        FirebaseFirestore.getInstance().collection(DBManager.CollectionsPaths.EVENTS)
                .document(eventID)
                .update(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(EventFragment.this.getContext(), "Ticket buyed correctly!", Toast.LENGTH_SHORT).show();
                        refreshFragment();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(EventFragment.this.getContext(), "ERROR: " + e, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void refreshFragment(){
        Log.d("EventFragment","Refreshing fragment");
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.detach(this);
        ft.attach(this);
        ft.commit();
    }

    @Override
    public void onBackPressed() {

    }
}
