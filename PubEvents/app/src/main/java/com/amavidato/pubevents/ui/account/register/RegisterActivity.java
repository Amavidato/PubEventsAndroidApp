package com.amavidato.pubevents.ui.account.register;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amavidato.pubevents.R;
import com.amavidato.pubevents.ui.account.AccountFormState;
import com.amavidato.pubevents.ui.account.AccountFormViewModel;
import com.amavidato.pubevents.utility.db.DBManager;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private ProgressBar loadingProgressBar;
    private AccountFormViewModel registerViewModel;
    private FirebaseAuth mAuth;
    private String TAG = this.getClass().getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mAuth = FirebaseAuth.getInstance();

        final EditText nicknameEditText = findViewById(R.id.nickname);
        final EditText emailEditText = findViewById(R.id.email_register);
        final EditText passwordEditText = findViewById(R.id.password_register);
        final Button registerButton = findViewById(R.id.register_activity_register);
        loadingProgressBar = findViewById(R.id.loading);

        registerViewModel = ViewModelProviders.of(this).get(AccountFormViewModel.class);

        registerViewModel.getFormState().observe(this, new Observer<AccountFormState>() {
            @Override
            public void onChanged(@Nullable AccountFormState registerFormState) {
                if (registerFormState == null) {
                    return;
                }
                registerButton.setEnabled(registerFormState.isDataValid());
                if(registerFormState.getNicknameError() != null){
                    nicknameEditText.setError(getString(registerFormState.getNicknameError()));
                }
                if (registerFormState.getEmailError() != null) {
                    emailEditText.setError(getString(registerFormState.getEmailError()));
                }
                if (registerFormState.getPasswordError() != null) {
                    passwordEditText.setError(getString(registerFormState.getPasswordError()));
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
                registerViewModel.onDataChanged(nicknameEditText.getText().toString(), emailEditText.getText().toString(),
                        passwordEditText.getText().toString());
            }
        };
        nicknameEditText.addTextChangedListener(afterTextChangedListener);
        emailEditText.addTextChangedListener(afterTextChangedListener);
        passwordEditText.addTextChangedListener(afterTextChangedListener);

        registerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadingProgressBar.setVisibility(View.VISIBLE);
                mAuth.createUserWithEmailAndPassword(emailEditText.getText().toString(), passwordEditText.getText().toString())
                        .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                loadingProgressBar.setVisibility(View.INVISIBLE);
                                if (task.isSuccessful()) {
                                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                                    Map<String,Object> user = new HashMap<>();
                                    user.put(DBManager.CollectionsPaths.UserFields.USERNAME, nicknameEditText.getText().toString());
                                    user.put(DBManager.CollectionsPaths.UserFields.EMAIL, emailEditText.getText().toString());

                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    db.collection(DBManager.CollectionsPaths.USERS).document(currentUser.getEmail()).set(user);
                                    // Sign in success, update UI with the signed-in user's information
                                    Log.d(TAG, "createUserWithEmail:success");
                                    currentUser.sendEmailVerification()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(RegisterActivity.this, R.string.verify_email_text,
                                                                Toast.LENGTH_LONG).show();
                                                        Log.d(TAG, "Email Verification sent");
                                                    }
                                                }
                                            });
                                    FirebaseAuth.getInstance().signOut();
                                    finish();
                                    //updateUI(user);
                                } else {
                                    // If sign up fails, display a message to the user.
                                    Log.w(TAG, "createUserWithEmail:failure", task.getException());
                                        Toast.makeText(RegisterActivity.this, "Registration Error.\n"+ task.getException().getMessage(),
                                                Toast.LENGTH_LONG).show();
                                    //updateUI(null);
                                }

                                // ...
                            }
                        });
            }
        });


    }
}
