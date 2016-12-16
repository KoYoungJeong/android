package com.tosslab.jandi.app.ui.passcode.model;

import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.passcode.PassCodeActivity;
import com.tosslab.jandi.app.utils.JandiPreference;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class PassCodeModel {
    public static final int MAX_PASSCODE_LENGTH = 4;

    private List<Integer> passCodeList = new ArrayList<>();
    private String previousPassCode;

    @Inject
    public PassCodeModel() { }

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

    public boolean isSetupMode(int mode) {
        return mode == PassCodeActivity.MODE_TO_SAVE_PASSCODE
                || mode == PassCodeActivity.MODE_TO_MODIFY_PASSCODE;
    }

    public boolean isUserWantsFingerPrintToAuth() {
        return JandiPreference.isUseFingerprint();
    }

    public boolean canUseFingerprintToAuth() {
        FingerprintManagerCompat fingerprintManagerCompat =
                FingerprintManagerCompat.from(JandiApplication.getContext());

        return fingerprintManagerCompat.isHardwareDetected()
                && fingerprintManagerCompat.hasEnrolledFingerprints();
    }
}
