package com.tosslab.jandi.app.ui.passcode.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 15. 10. 13..
 */
@EBean
public class PassCodeModel {
    public static final int MAX_PASSCODE_LENGTH = 4;

    private List<Integer> passCodeList = new ArrayList<>();
    private String previousPassCode;

    public boolean putPassCode(int passCode) {
        if (getPassCodeLength() >= MAX_PASSCODE_LENGTH) {
            return false;
        }

        passCodeList.add(passCode);
        return true;
    }

    public boolean popPassCode() {
        if (getPassCodeLength() <= 0) {
            return false;
        }

        passCodeList.remove(getPassCodeLength() - 1);
        return true;
    }

    public int getPassCodeLength() {
        return passCodeList.size();
    }

    public boolean validatePassCode(String savedPassCode) {
        if (TextUtils.isEmpty(savedPassCode)) {
            return false;
        }

        if (getPassCodeLength() < MAX_PASSCODE_LENGTH) {
            return false;
        }

        return savedPassCode.equals(getPassCode());
    }

    public void clearPassCode() {
        passCodeList.clear();
    }

    public String getPassCode() {
        StringBuilder sb = new StringBuilder();
        for (Integer i : passCodeList) {
            sb.append(i);
        }
        return sb.toString();
    }

    public void savePassCode() {
        JandiPreference.setPassCode(JandiApplication.getContext(), getPassCode());
    }

    public String getPreviousPassCode() {
        return previousPassCode;
    }

    public void setPreviousPassCode(String previousPassCode) {
        this.previousPassCode = previousPassCode;
    }
}
