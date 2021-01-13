package com.amavidato.pubevents.ui.account.login;

import androidx.annotation.NonNull;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AppCompatActivity;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.ui.account.AccountFormState;
import com.amavidato.pubevents.ui.account.AccountFormViewModel;
import com.amavidato.pubevents.ui.account.register.RegisterActivity;
import com.amavidato.pubevents.utility.db.DBManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.SignInMethodQueryResult;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {

    private AccountFormViewModel loginViewModel;

    private FirebaseAuth mAuth;
    // Configure Google Sign In
    private GoogleSignInOptions gso;
    private SignInButton loginWithGoogleButton;
    private String TAG = this.getClass().getSimpleName();
    private GoogleSignInClient mGoogleSignInClient;
    private int RC_SIGN_IN = 1; //TODO: DA vedere
    private ProgressBar loadingProgressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        loginViewModel = ViewModelProviders.of(this).get(AccountFormViewModel.class);

        final EditText emailEditText = findViewById(R.id.email_login);
        final EditText passwordEditText = findViewById(R.id.password_login);
        final Button loginButton = findViewById(R.id.login);
        loginWithGoogleButton = findViewById(R.id.login_withGoogle);
        final TextView registerText = findViewById(R.id.register_activity_login);
        loadingProgressBar = findViewById(R.id.loading);
        loginViewModel.getFormState().observe(this, new Observer<AccountFormState>() {
            @Override
            public void onChanged(@Nullable AccountFormState loginFormState) {
                if (loginFormState == null) {
                    return;
                }
                loginButton.setEnabled(loginFormState.isDataValid());
                if (loginFormState.getEmailError() != null) {
                    emailEditText.setError(getString(loginFormState.getEmailError()));
                }
                if (loginFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(loginFormState.getPasswordError()));
                }
            }
        });

        TextWatcher afterTextChangedListener = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // ignore
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // ignore
            }

            @Override
            public void afterTextChanged(Editable s) {
                loginViewModel.onDataChanged(emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                loadingProgressBar.setVisibility(View.INVISIBLE);
                                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                                boolean isEmailVerified = currentUser != null ? currentUser.isEmailVerified() : false;
                                if (task.isSuccessful()) {
                                    if(isEmailVerified){
                                        // Sign in success, update UI with the signed-in user's information
                                        Log.d(TAG, "signInWithEmail:success");
                                        finish();
                                    }else{
                                        Log.w(TAG, "signInWithEmail:failure (email not verified)", task.getException());
                                        Toast.makeText(LoginActivity.this, R.string.email_not_verified_text,
                                                Toast.LENGTH_LONG).show();
                                    }
                                    //updateUI(user);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.w(TAG, "signInWithEmail:failure", task.getException());
                                    if(task.getException() instanceof FirebaseAuthInvalidUserException){
                                        Toast.makeText(LoginActivity.this, R.string.no_account_with_this_email,
                                                Toast.LENGTH_LONG).show();
                                    }else if (task.getException() instanceof FirebaseAuthInvalidCredentialsException){
                                        Toast.makeText(LoginActivity.this, R.string.wrong_email,
                                                Toast.LENGTH_LONG).show();
                                    }else{
                                        Toast.makeText(LoginActivity.this, "Authentication failed.\n "+task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    }
                                    //updateUI(null);
                                }

                                // ...
                            }
                        });
            }
        });

        loginWithGoogleButton.setOnClickListener(this);
        registerText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                finish();  //Kill the activity from which you will go to next activity
                startActivity(i);
            }
        });
    }

    private void showLoginFailed(@StringRes Integer errorString) {
        Toast.makeText(getApplicationContext(), errorString, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.login_withGoogle:
                loadingProgressBar.setVisibility(View.VISIBLE);
                Intent signInIntent = mGoogleSignInClient.getSignInIntent();
                startActivityForResult(signInIntent, RC_SIGN_IN);
                break;
            // ...
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Log.w(TAG, "Google sign in failed", e);
                Toast.makeText(getApplicationContext(),"Google sign in failed",Toast.LENGTH_LONG).show();
                loadingProgressBar.setVisibility(View.INVISIBLE);
                // ...
            }

        }
    }

    private void firebaseAuthWithGoogle(final GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogle:" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        loadingProgressBar.setVisibility(View.INVISIBLE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithCredential:success");
                            AddDBUserIfNotExists(acct);

                            finish();
                            //updateUI(user);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Snackbar.make(findViewById(R.id.container), "Authentication Failed.", Snackbar.LENGTH_SHORT).show();
                            //updateUI(null);
                        }
                        // ...
                    }
                });
    }

    private void AddDBUserIfNotExists(final GoogleSignInAccount acc) {


        mAuth.fetchSignInMethodsForEmail(acc.getEmail()).addOnCompleteListener(new OnCompleteListener<SignInMethodQueryResult>() {
            @Override
            public void onComplete(@NonNull Task<SignInMethodQueryResult> task) {
                if(task.isSuccessful()){
                    List<String> providers = task.getResult().getSignInMethods();
                    for (String str: providers) {
                        Log.d("PROVIDERS",str);
                    }
                    providers.remove("google.com");
                    if(task.getResult().getSignInMethods().size() == 0){
                        //User with this email NOT exists.
                        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                        Log.d("GLogin","Email NOT associated to user");
                        Map<String, Object> user = new HashMap<>();
                        user.put(DBManager.CollectionsPaths.UserFields.EMAIL, acc.getEmail());
                        user.put(DBManager.CollectionsPaths.UserFields.USERNAME, acc.getDisplayName());

                        if(currentUser == null){
                            Log.d("Reg. dbUser from GLogin","Current user null");
                        }else{
                            Log.d("Reg. dbUser from GLogin","Current user NOT null:"+currentUser.toString());
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection(DBManager.CollectionsPaths.USERS).document(currentUser.getEmail()).set(user);
                        }
                    }else{
                        //User with this email ALREADY exists.
                        Log.d("GLogin","Email already associated to user");
                    }
                }
            }
        });

        //updateUI(user);
    }



}
