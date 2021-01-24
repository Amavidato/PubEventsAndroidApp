package com.amavidato.pubevents.utility;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountManager {

    public static boolean isCurrentUserValid(){
        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseUser user = auth.getCurrentUser();
        return user != null && user.isEmailVerified();
    }
}
