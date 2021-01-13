package com.amavidato.pubevents;

import android.os.Bundle;

import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.MyFragment;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private NavigationView navigationView;

    private FirebaseAuth mAuth;
    private GoogleSignInOptions gso;
    private GoogleSignInClient mGoogleSignInClient;

    private TextView navUsername;
    private MenuItem itemFollowedPubs;
    private MenuItem itemInterestEvents;
    private MenuItem itemSettings;
    private MenuItem itemLogout;
    private MenuItem itemLogin;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        FloatingActionButton fab = findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(R.id.nav_home,
                R.id.nav_followed_pubs, R.id.nav_acquired_events, R.id.nav_find_pub,
                R.id.nav_settings, R.id.nav_login, R.id.nav_login,R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Lines to change dinamically the content of the header
        View headerView = navigationView.getHeaderView(0);
        navUsername = headerView.findViewById(R.id.nav_username);
        //Lines to change dinamically the content of the menu
        Menu menuView = navigationView.getMenu();
        itemFollowedPubs = menuView.findItem(R.id.nav_followed_pubs);
        itemInterestEvents = menuView.findItem(R.id.nav_acquired_events);
        itemSettings = menuView.findItem(R.id.nav_settings);
        itemLogout = menuView.findItem(R.id.nav_logout);
        itemLogout.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                mAuth.signOut();
                mGoogleSignInClient.signOut();
                MainActivity.this.onStart();
                return false;
            }
        });
        itemLogin = menuView.findItem(R.id.nav_login);

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        if(account != null){
            Log.d(TAG,"GAccount:"+account+"=====Current User:"+FirebaseAuth.getInstance().getCurrentUser());
            updateUI(account);
        }else{
            // Check if user is signed in (non-null) and update UI accordingly.
            FirebaseUser currentUser = mAuth.getCurrentUser();
            updateUI(currentUser);
        }

    }

    private void updateUI(Object currentUser) {

        if(currentUser == null){
            navUsername.setText("Guest");
            itemFollowedPubs.setVisible(false);
            itemInterestEvents.setVisible(false);
            itemSettings.setVisible(false);
            itemLogout.setVisible(false);
            itemLogin.setVisible(true);
        }else{
            username = "Nome non trovato";
            if(currentUser instanceof FirebaseUser){
                String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                if( ((FirebaseUser)currentUser).isEmailVerified()){
                    FirebaseFirestore.getInstance().document(DBManager.CollectionsPaths.USERS+"/"+uid)
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Map<String, Object> data = document.getData();
                                    username = (String) data.get(DBManager.CollectionsPaths.UserFields.USERNAME);
                                    navUsername.setText(username);
                                    Log.d(TAG, "DocumentSnapshot data: " + data);
                                } else {
                                    Log.d(TAG, "No such document");
                                }
                            } else {
                                Log.d(TAG, "get failed with ", task.getException());
                            }
                        }
                    });
                }else{
                    Toast.makeText(this,R.string.email_not_verified_text,Toast.LENGTH_LONG).show();
                }
            }else if(currentUser instanceof GoogleSignInAccount){
                username = ((GoogleSignInAccount) currentUser).getDisplayName();
            }
            navUsername.setText(username);
            itemFollowedPubs.setVisible(true);
            itemInterestEvents.setVisible(true);
            itemSettings.setVisible(true);
            itemLogout.setVisible(true);
            itemLogin.setVisible(false);
        }
    }

    @Override
    public void onBackPressed() {
        for(Fragment frag : getSupportFragmentManager().getFragments()){
            if(frag instanceof MyFragment){
                ((MyFragment)frag).onBackPressed();
            }
        }
        super.onBackPressed();
    }
}
