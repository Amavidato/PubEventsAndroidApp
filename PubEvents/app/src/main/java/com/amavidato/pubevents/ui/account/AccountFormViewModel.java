package com.amavidato.pubevents.ui.account;

import android.util.Log;
import android.util.Patterns;
import android.widget.Toast;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.amavidato.pubevents.R;

public class AccountFormViewModel extends ViewModel {

    private MutableLiveData<AccountFormState> formState;

    public AccountFormViewModel() {
        this.formState = new MutableLiveData<AccountFormState>();
    }

    public AccountFormViewModel(MutableLiveData<AccountFormState> formState) {
        this.formState = formState;
    }

    public void onDataChanged(String email, String password) {
        if (!isEmailValid(email)) {
            this.formState.setValue(getEmailErrorFormState());
        } if (!isPasswordValid(password)) {
            this.formState.setValue(getPasswordErrorFormState());
        } else {
            this.formState.setValue(getDataValidFormState());
        }
    }

    public void onDataChanged(String nickname, String email, String password) {
        if(!isNicknameValid(nickname)) {
            this.formState.setValue(getNicknameErrorFormState());
        }
        onDataChanged(email,password);
    }

    // A placeholder nickname validation check
    private boolean isNicknameValid(String nickname) {
        return nickname != null && nickname.trim().length() > 5 && nickname.trim().length() < 30;
    }

    // A placeholder email validation check
    private boolean isEmailValid(String email) {
        if (email.contains("@")) {
            Log.d(this.getClass().getSimpleName(),"Email 1: "+Patterns.EMAIL_ADDRESS.matcher(email).matches());
            return Patterns.EMAIL_ADDRESS.matcher(email).matches();
        } else {
            Log.d(this.getClass().getSimpleName(),"Email 2");
            return false;
        }
    }

    // A placeholder password validation check
    private boolean isPasswordValid(String password) {
        return password != null && password.trim().length() > 5;
    }

    private AccountFormState getNicknameErrorFormState() {
        return new AccountFormState(R.string.invalid_username, null, null);
    }

    private AccountFormState getEmailErrorFormState() {
        return new AccountFormState(null, R.string.invalid_email, null);
    }

    private AccountFormState getPasswordErrorFormState() {
        return new AccountFormState(null, null, R.string.invalid_password);
    }

    private AccountFormState getDataValidFormState() {
        return new AccountFormState(true);
    }

    public MutableLiveData<AccountFormState> getFormState(){
        return this.formState;
    }
}
