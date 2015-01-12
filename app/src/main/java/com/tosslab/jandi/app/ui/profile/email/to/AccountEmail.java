package com.tosslab.jandi.app.ui.profile.email.to;

/**
 * Created by Steve SeongUg Jung on 15. 1. 12..
 */
public class AccountEmail {
    private final String email;
    private final boolean isConfirmed;
    private boolean isSelected;

    public AccountEmail(String email, boolean isConfirmed) {
        this.email = email;
        this.isConfirmed = isConfirmed;
    }

    public String getEmail() {
        return email;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean isSelected) {
        this.isSelected = isSelected;
    }

    public boolean isConfirmed() {
        return isConfirmed;
    }

    public static class DummyEmail extends AccountEmail {

        public DummyEmail() {
            super("", false);
        }
    }
}
