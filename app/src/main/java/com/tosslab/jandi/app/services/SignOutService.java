package com.tosslab.jandi.app.services;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccessTokenRepository;
import com.tosslab.jandi.app.network.models.ResAccessToken;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.login.IntroMainActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.SignOutUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 15. 11. 4..
 */
public class SignOutService extends IntentService {
    public static final String ACTION_SIGN_OUT = "action_sign_out";
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
        ResAccessToken accessToken = AccessTokenRepository.getRepository().getAccessToken();
        boolean hasRefreshToken = !TextUtils.isEmpty(accessToken.getRefreshToken());
        if (!hasRefreshToken) {
            return;
        }

        LogUtil.i(TAG, "Log out");

        SignOutUtil.removeSignData();

        final Context context = getApplicationContext();
        JandiSocketService.stopService(context);
        IntroMainActivity_
                .intent(context)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();

        new Handler(Looper.getMainLooper())
                .post(() -> {
                    ColoredToast.showError(context, context.getString(R.string.err_expired_session));
                });
    }
}
