package com.tosslab.jandi.app.ui.passcode.fingerprint;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Dialog;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.passcode.OnUnLockSuccessListener;
import com.tosslab.jandi.app.ui.passcode.fingerprint.presneter.FingerprintAuthPresenter;
import com.tosslab.jandi.app.ui.passcode.fingerprint.view.FingerprintAuthView;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;

/**
 * Created by tonyjs on 16. 3. 24..
 */
@EFragment
public class FingerprintAuthDialogFragment extends DialogFragment implements FingerprintAuthView {

    public static final int REQUEST_USE_FINGERPRINT = 325;

    @Bean
    FingerprintAuthPresenter presenter;

    FingerprintManager fingerprintManager;
    View view;
    private CancellationSignal cancellationSignal;

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        fingerprintManager = JandiApplication.getService(Context.FINGERPRINT_SERVICE);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_fingerprint, null);
        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.jandi_verify_fingerprint)
                .setView(view)
                .setNegativeButton(R.string.jandi_cancel, null)
                .create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        presenter.setView(this);
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void onResume() {
        super.onResume();

        Permissions.getChecker()
                .permission(() -> Manifest.permission.USE_FINGERPRINT)
                .hasPermission(this::startFingerprintAuth)
                .noPermission(() -> {
                    String[] permissions = {Manifest.permission.USE_FINGERPRINT};
                    requestPermissions(permissions, REQUEST_USE_FINGERPRINT);
                })
                .check();
    }

    @Override
    public void onPause() {
        presenter.setCancelled(true);
        cancellationSignal.cancel();
        cancellationSignal = null;
        super.onPause();
    }

    private void startFingerprintAuth() {
        presenter.setCancelled(false);
        cancellationSignal = new CancellationSignal();
        presenter.onStartListeningFingerprint();
    }

    @Override
    public void startListening(final FingerprintManager.CryptoObject cryptoObject) {
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, presenter, null);
    }

    @Override
    public void showFingerprintAuthFailed() {
        getDialog().setTitle(R.string.jandi_try_again);

        float startX = -UiUtils.getPixelFromDp(5);
        float endX = UiUtils.getPixelFromDp(5);

        ValueAnimator bounceAnim = ValueAnimator.ofFloat(startX, endX);
        bounceAnim.setDuration(100);
        bounceAnim.setRepeatCount(3);
        bounceAnim.setRepeatMode(ValueAnimator.REVERSE);
        bounceAnim.addUpdateListener(animation -> {
            view.setTranslationX((Float) animation.getAnimatedValue());
        });
        bounceAnim.addListener(new SimpleEndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                view.setTranslationX(0);
            }
        });
        bounceAnim.start();
    }

    @Override
    public void setFingerprintAuthSuccess() {
        if (getActivity() instanceof OnUnLockSuccessListener) {
            ((OnUnLockSuccessListener) getActivity()).onUnLockSuccess();
        }
    }

    @Override
    public void showAuthenticationHelpDialog(CharSequence helpString) {
        AlertUtil.showConfirmDialog(getActivity(), helpString.toString(), null, false);
    }

    @Override
    public void showAuthenticationError(final int errorCode, CharSequence errString) {
        new AlertDialog.Builder(getActivity())
                .setMessage(errString)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    if (errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                        FingerprintAuthDialogFragment.this.dismiss();
                    }
                })
                .setOnCancelListener(dialog -> FingerprintAuthDialogFragment.this.dismiss())
                .create()
                .show();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Permissions.getResult()
                .addRequestCode(REQUEST_USE_FINGERPRINT)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::startFingerprintAuth)
                .resultPermission(Permissions.createPermissionResult(requestCode,
                        permissions,
                        grantResults));

    }
}
