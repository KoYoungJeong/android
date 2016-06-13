package com.tosslab.jandi.app.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.sign.signin.SignInActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.TokenUtil;

public class SignOutService extends IntentService {
    public static final String TAG = SignOutService.class.getSimpleName();

    public SignOutService() {
        super(TAG);
    }

    public static synchronized void start() {
        Context context = JandiApplication.getContext();
        Intent intent = new Intent(context, SignOutService.class);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        boolean hasRefreshToken = !TextUtils.isEmpty(TokenUtil.getRefreshToken());
        if (!hasRefreshToken) {
            return;
        }

        SignOutUtil.removeSignData();

        final Context context = getApplicationContext();
        JandiSocketService.stopService(context);
        Intent intentForSignIn = new Intent(JandiApplication.getContext(), SignInActivity.class);
        intentForSignIn.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        JandiApplication.getContext().startActivity(intentForSignIn);

        new Handler(Looper.getMainLooper())
                .post(() -> ColoredToast.show(context.getString(R.string.err_expired_session)));
    }
}