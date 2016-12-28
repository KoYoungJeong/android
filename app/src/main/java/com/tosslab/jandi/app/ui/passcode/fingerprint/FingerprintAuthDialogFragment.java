package com.tosslab.jandi.app.ui.passcode.fingerprint;

import android.Manifest;
import android.animation.Animator;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.passcode.OnUnLockSuccessListener;
import com.tosslab.jandi.app.ui.passcode.fingerprint.dagger.DaggerFingerprintAuthComponent;
import com.tosslab.jandi.app.ui.passcode.fingerprint.dagger.FingerprintAuthModule;
import com.tosslab.jandi.app.ui.passcode.fingerprint.presneter.FingerprintAuthPresenter;
import com.tosslab.jandi.app.ui.passcode.fingerprint.view.FingerprintAuthView;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import javax.inject.Inject;

@TargetApi(Build.VERSION_CODES.M)
public class FingerprintAuthDialogFragment extends DialogFragment implements FingerprintAuthView {

    public static final int REQUEST_USE_FINGERPRINT = 325;

    @Inject
    FingerprintAuthPresenter presenter;

    View rootView;

    FingerprintManager fingerprintManager;

    private CancellationSignal cancellationSignal;
    private AlertDialog helpDialog;
    private AlertDialog errorDialog;
    private OnFingerPrintErrorListener onFingerPrintErrorListener;

    private int tryCount = 1;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        fingerprintManager = (FingerprintManager) JandiApplication.getContext().getSystemService(Context.FINGERPRINT_SERVICE);
        DaggerFingerprintAuthComponent.builder()
                .fingerprintAuthModule(new FingerprintAuthModule(this))
                .build()
                .inject(this);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        helpDialog = new AlertDialog.Builder(activity)
                .setPositiveButton(R.string.jandi_confirm, null)
                .setCancelable(true)
                .create();

        errorDialog = new AlertDialog.Builder(activity)
                .setCancelable(true)
                .create();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        rootView = LayoutInflater.from(getActivity()).inflate(R.layout.layout_fingerprint, null);

        return new AlertDialog.Builder(getActivity())
                .setTitle(R.string.jandi_verify_fingerprint)
                .setView(rootView)
                .setNegativeButton(R.string.jandi_cancel, null)
                .create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();

        Permissions.getChecker()
                .activity(getActivity())
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
        if (errorDialog != null && errorDialog.isShowing()) {
            errorDialog.dismiss();
        }
        if (helpDialog != null && helpDialog.isShowing()) {
            helpDialog.dismiss();
        }

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

    @SuppressWarnings("ResourceType")
    @Override
    public void startListening(final FingerprintManager.CryptoObject cryptoObject) {
        fingerprintManager.authenticate(cryptoObject, cancellationSignal, 0, presenter, null);
    }

    @Override
    public void showFingerprintAuthFailed() {
        ++tryCount;
        getDialog().setTitle(R.string.jandi_please_retry);

        float startX = -UiUtils.getPixelFromDp(5);
        float endX = UiUtils.getPixelFromDp(5);

        ValueAnimator bounceAnim = ValueAnimator.ofFloat(startX, endX);
        bounceAnim.setDuration(100);
        bounceAnim.setRepeatCount(3);
        bounceAnim.setRepeatMode(ValueAnimator.REVERSE);
        bounceAnim.addUpdateListener(animation ->
                rootView.setTranslationX((Float) animation.getAnimatedValue()));
        bounceAnim.addListener(new SimpleEndAnimatorListener() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rootView.setTranslationX(0);
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
        if (helpDialog != null && !helpDialog.isShowing()) {
            helpDialog.setMessage(helpString);
            helpDialog.show();
        }
    }

    @Override
    public void showAuthenticationError(final int errorCode, CharSequence errString) {
        if (onFingerPrintErrorListener != null) {
            onFingerPrintErrorListener.call();
        }
        if (tryCount <= 1) {
            // TODO Show error message it cannot use period time.
            dismiss();
        }
        if (errorDialog != null && !errorDialog.isShowing()) {
            String confirm = JandiApplication.getContext()
                    .getResources().getString(R.string.jandi_confirm);
            errorDialog.setButton(DialogInterface.BUTTON_POSITIVE, confirm,
                    (dialog, which) -> {
                        if (errorCode == FingerprintManager.FINGERPRINT_ERROR_LOCKOUT) {
                            // TODO message use pin
                            FingerprintAuthDialogFragment.this.dismiss();
                        }
                    });
            errorDialog.setOnCancelListener(dialog -> FingerprintAuthDialogFragment.this.dismiss());
            errorDialog.setMessage(errString);
            errorDialog.show();
        }
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Permissions.getResult()
                .addRequestCode(REQUEST_USE_FINGERPRINT)
                .addPermission(Manifest.permission.USE_FINGERPRINT, this::startFingerprintAuth)
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(getActivity());
                })
                .resultPermission(
                        Permissions.createPermissionResult(requestCode,
                                permissions,
                                grantResults));

    }

    public void setOnFingerPrintErrorListener(OnFingerPrintErrorListener onFingerPrintErrorListener) {
        this.onFingerPrintErrorListener = onFingerPrintErrorListener;
    }

    public interface OnFingerPrintErrorListener {
        void call();
    }
}
