package com.amavidato.pubevents.ui.account;

import androidx.annotation.Nullable;

public class AccountFormState {
    @Nullable
    private Integer nicknameError;
    @Nullable
    private Integer emailError;
    @Nullable
    private Integer passwordError;
    private boolean isDataValid;

    public AccountFormState() {
        this.emailError = null;
        this.passwordError = null;
        this.isDataValid = false;
    }

    public AccountFormState(@Nullable Integer nicknameError, @Nullable Integer emailError, @Nullable Integer passwordError) {
        this.nicknameError = nicknameError;
        this.emailError = emailError;
        this.passwordError = passwordError;
        this.isDataValid = false;
    }

    public AccountFormState(boolean isDataValid) {
        this.nicknameError = null;
        this.emailError = null;
        this.passwordError = null;
        this.isDataValid = isDataValid;
    }

    @Nullable
    public Integer getNicknameError() {
        return nicknameError;
    }

    @Nullable
    public Integer getEmailError() {
        return emailError;
    }

    @Nullable
    public Integer getPasswordError() {
        return passwordError;
    }

    public boolean isDataValid() {
        return isDataValid;
    }

    public void setNicknameError(@Nullable Integer nicknameError) {
        this.nicknameError = nicknameError;
    }

    public void setEmailError(@Nullable Integer emailError) {
        this.emailError = emailError;
    }

    public void setPasswordError(@Nullable Integer passwordError) {
        this.passwordError = passwordError;
    }

    public void setDataValid(boolean dataValid) {
        isDataValid = dataValid;
    }
}
