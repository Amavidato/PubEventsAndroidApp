package com.amavidato.pubevents;

import android.net.Uri;
import android.os.Bundle;

import com.amavidato.pubevents.utility.ImageManager;
import com.amavidato.pubevents.utility.db.DBManager;
import com.amavidato.pubevents.utility.MyFragment;
import com.bumptech.glide.Glide;
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

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GetTokenResult;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.ImageView;
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
    private ImageView navUserImg;
    private MenuItem itemFollowedPubs;
    private MenuItem itemInterestEvents;
    private MenuItem itemLogout;
    private MenuItem itemLogin;
    private static final String TAG = MainActivity.class.getSimpleName();
    private String username;
    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String dialogStr;
        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if(extras == null) {
                dialogStr= null;
                Log.d(TAG, "SIS null. DialSTR null");
            } else {
                dialogStr= extras.getString("dialog_str");
                Log.d(TAG, "SIS null. GetSTR:"+dialogStr);
            }
        } else {
            dialogStr= (String) savedInstanceState.getSerializable("dialog_str");
            Log.d(TAG, "SIS NOT NULL. DialSTR: "+dialogStr);
        }
        //openDialog(dialogStr);
        setContentView(R.layout.activity_main);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                final FirebaseUser mUser = firebaseAuth.getCurrentUser();//FirebaseAuth.getInstance().getCurrentUser();
                final String[] username = {null};
                if(mUser != null){
                    mUser.getIdToken(true)
                            .addOnCompleteListener(new OnCompleteListener<GetTokenResult>() {
                                public void onComplete(@NonNull Task<GetTokenResult> task) {
                                    if (task.isSuccessful()) {
                                        String idToken = task.getResult().getToken();
                                        username[0] = mUser.getDisplayName();
                                        Log.d(MainActivity.TAG, "TOKEN EXISTS. Username:"+username[0]);
                                        // Send token to your backend via HTTPS
                                        // ...
                                        if(FirebaseAuth.getInstance().getCurrentUser().isEmailVerified()){
                                            updateUI(mUser);
                                            return;
                                        }else{
                                            openDialog("Can't sign in with your account", "It seems that the email associated to this account has not been verified.\nPlease remember to verify your email address to fully experience the app!");
                                        }
                                    } else {
                                        Log.d(MainActivity.TAG, "TOKEN DOESN'T EXISTS");
                                        // Handle error -> task.getException();
                                    }
                                    updateUI(null);
                                    //setUI(username[0]);
                                }
                            });
                }else{
                    updateUI(null);
                    //setUI(username[0]);
                }

                /*FirebaseUser user = firebaseAuth.getCurrentUser();
                String username = null;
                if(user != null){
                    Log.d(TAG, "Current User NOT NULL:"+user);
                    username = user.getDisplayName();

                }else{
                    Log.d(TAG, "Current User ***NULL***");
                }
                setUI(username);*/
            }
        });
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
                R.id.nav_followed_pubs, R.id.nav_acquired_events, R.id.nav_find_pub, R.id.nav_login, R.id.nav_login,R.id.nav_logout)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        //Lines to change dinamically the content of the header
        View headerView = navigationView.getHeaderView(0);
        navUsername = headerView.findViewById(R.id.nav_username);
        navUserImg = headerView.findViewById(R.id.nav_user_img);
        //Lines to change dinamically the content of the menu
        Menu menuView = navigationView.getMenu();
        itemFollowedPubs = menuView.findItem(R.id.nav_followed_pubs);
        itemInterestEvents = menuView.findItem(R.id.nav_acquired_events);
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
        /*itemLogin.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                finish();
                return false;
            }
        });*/

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
        //updateUI(FirebaseAuth.getInstance().getCurrentUser());
        // Check for existing Google Sign In account, if the user is already signed in
        // the GoogleSignInAccount will be non-null.
        //GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
        //if(account != null){

        //    Log.d(TAG,"GAccount:"+account+"=====Current User:"+FirebaseAuth.getInstance().getCurrentUser());
        //    updateUI(account);
        //}else{
            // Check if user is signed in (non-null) and update UI accordingly.

        //}

    }

    private void updateUI(final Object currentUser) {
        if(currentUser != null) {
            if(currentUser instanceof FirebaseUser){
                if( ((FirebaseUser)currentUser).isEmailVerified()){
                    String uid = FirebaseAuth.getInstance().getCurrentUser().getEmail();
                    FirebaseFirestore.getInstance().document(DBManager.CollectionsPaths.USERS+"/"+uid)
                            .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                DocumentSnapshot document = task.getResult();
                                if (document.exists()) {
                                    Map<String, Object> data = document.getData();
                                    username = (String) data.get(DBManager.CollectionsPaths.UserFields.USERNAME);
                                    Log.d(TAG, "USERNAME FROM DATA:"+username);
                                    setUI((FirebaseUser) currentUser);
                                    //navUsername.setText(username);
                                    Log.d(TAG, "DocumentSnapshot data: " + data + " currUs:"+currentUser);
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
                    return;
                }
            }else if(currentUser instanceof GoogleSignInAccount){
                Log.d(TAG, "Current google user:"+FirebaseAuth.getInstance().getCurrentUser().toString());
                username = ((GoogleSignInAccount) currentUser).getDisplayName();
                setUI((FirebaseUser) currentUser);
            }
        }else{
            setUI(null);
        }
        /*navUsername.setText(username);
        itemFollowedPubs.setVisible(true);
        itemInterestEvents.setVisible(true);
        itemSettings.setVisible(true);
        itemLogout.setVisible(true);
        itemLogin.setVisible(false);*/
    }

    private void setUI(FirebaseUser user){
        String username = "";
        Uri photoUri = null;
        if(user != null){
            username = user.getDisplayName();
            photoUri = user.getPhotoUrl();
        }
        invalidateOptionsMenu();
        navigationView.invalidate();
        boolean validUser = username != null && username != "";
        Log.d(TAG,"USERNAME SET UI:"+ username +"** valid:"+validUser+"**");
        navUsername.setText(validUser ? username :"Guest");
        /*((View)itemFollowedPubs).setVisibility(validUser?View.VISIBLE:View.GONE);
        ((View)itemInterestEvents).setVisibility(validUser?View.VISIBLE:View.GONE);
        ((View)itemSettings).setVisibility(validUser?View.VISIBLE:View.GONE);
        ((View)itemLogout).setVisibility(validUser?View.VISIBLE:View.GONE);
        ((View)itemLogin).setVisibility(!validUser?View.VISIBLE:View.GONE);*/
        Glide.with(this).load(photoUri).placeholder(R.drawable.ic_baseline_account_circle_24).into(navUserImg);
        //ImageManager.uploadImage("images/users/"+user.getEmail(),photoUri);
        itemFollowedPubs.setVisible(validUser);
        itemInterestEvents.setVisible(validUser);
        itemLogout.setVisible(validUser);
        itemLogin.setVisible(!validUser);
    }

    private void openDialog(String title, String dialogStr) {
        if(dialogStr != null){
            new MaterialAlertDialogBuilder(this).setMessage(dialogStr)
                    .setIcon(R.drawable.ic_baseline_error_24)
                    .setTitle(title)
                    .create().show();
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

    @Override
    protected void onResume() {
        Log.d(TAG,"ON RESUME");
        updateUI(FirebaseAuth.getInstance().getCurrentUser());
        super.onResume();

    }
}
