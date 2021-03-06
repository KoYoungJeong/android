package com.tosslab.jandi.app.ui.maintab.dialog;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.util.Pair;
import android.view.WindowManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.settings.model.SettingsModel;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.TokenUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class UsageInformationDialogFragment extends DialogFragment {

    public static UsageInformationDialogFragment create() {
        return new UsageInformationDialogFragment();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        String userName = TeamInfoLoader.getInstance().getMemberName(TeamInfoLoader.getInstance().getMyId());

        CharSequence message = getMessage();
        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(message)
                .setTitle("JANDI Usage Information")
                .setCancelable(false)
                .setNegativeButton(R.string.jandi_close, (dialog, which) -> {
                    AnalyticsUtil.sendEvent(
                            AnalyticsValue.Screen.HamburgerMenu, AnalyticsValue.Action.VersionInfo_Close);
                })
                .setPositiveButton(R.string.jandi_send_to_email, (dialog, which) -> {
                    Intent intent =
                            new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:support@tosslab.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "JANDI Usage Information - " + userName);
                    intent.putExtra(Intent.EXTRA_TEXT, message.toString());
                    try {
                        startActivity(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    AnalyticsUtil.sendEvent(
                            AnalyticsValue.Screen.HamburgerMenu, AnalyticsValue.Action.VersionInfo_Email);
                })
                .create();

        WindowManager.LayoutParams layoutParams = alertDialog.getWindow().getAttributes();
        layoutParams.height = getResources().getDisplayMetrics().heightPixels * 2 / 3;
        alertDialog.getWindow().setAttributes(layoutParams);
        alertDialog.setCanceledOnTouchOutside(false);
        return alertDialog;
    }

    private CharSequence getMessage() {
        List<Pair<String, String>> userInfos = new ArrayList<>();

        userInfos.add(new Pair<>("Device", Build.MODEL));
        userInfos.add(new Pair<>("Android OS Version", Build.VERSION.RELEASE));
        userInfos.add(new Pair<>("JANDI App Version", SettingsModel.getVersionName()));

        userInfos.add(new Pair<>("Account Name",
                AccountRepository.getRepository().getAccountInfo().getName()));
        userInfos.add(new Pair<>("Member ID",
                String.valueOf(TeamInfoLoader.getInstance().getMyId())));
        userInfos.add(new Pair<>("Member Email",
                TeamInfoLoader.getInstance()
                        .getUser(TeamInfoLoader.getInstance().getMyId())
                        .getEmail()));

        userInfos.add(new Pair<>("Team", TeamInfoLoader.getInstance().getTeamName()));
        userInfos.add(new Pair<>("Team ID",
                String.valueOf(TeamInfoLoader.getInstance().getTeamId())));

        userInfos.add(new Pair<>("Account ID",
                String.valueOf(AccountUtil.getAccountUUID(JandiApplication.getContext()))));

        List<PushToken> pushTokenList = PushTokenRepository.getInstance().getPushTokenList();
        StringBuilder builder = new StringBuilder();
        boolean first = true;
        for (PushToken pushToken : pushTokenList) {
            if (first) {
                first = false;
            } else {
                builder.append("\n");
            }
            builder.append(pushToken.getService()).append(" : ").append(pushToken.getToken());
        }

        userInfos.add(new Pair<>("Device Token", builder.toString()));

        if (TokenUtil.getTokenObject() != null && TokenUtil.getTokenObject().getDeviceId() != null) {
            userInfos.add(new Pair<>("Device ID", TokenUtil.getTokenObject().getDeviceId()));
        }

        SpannableStringBuilder ssb = new SpannableStringBuilder();
        Observable.from(userInfos)
                .subscribe(pair -> {
                    if (ssb.length() > 0) {
                        ssb.append("\n\n");
                    }
                    ssb.append(pair.first);
                    ssb.append("\n").append(pair.second);
                });
        return ssb;
    }
}
