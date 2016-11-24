package com.tosslab.jandi.app.network;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.services.SignOutService;
import com.tosslab.jandi.app.utils.logger.LogUtil;

public class DomainUtil {
    private static final String KEY_DOMAIN = "domain";

    private static SharedPreferences getPreference() {
        return JandiApplication.getContext().getSharedPreferences("domain", Context.MODE_PRIVATE);
    }

    public static String getDomain() {
        return getPreference().getString(KEY_DOMAIN, JandiConstantsForFlavors.SERVICE_DOMAIN_BASE);
    }

    public static void putDomain(String domain) {
        getPreference().edit().putString(KEY_DOMAIN, domain).apply();
    }

    public static AlertDialog showDomainDialog(Context context) {
        String[] domains = {"jandi.com",
                "a-jandi.com",
                "b-jandi.com",
                "d-jandi.com",
                "jandi.io"};
        String domain = DomainUtil.getDomain();
        int selected = 0;
        for (int idx = 0; idx < domains.length; idx++) {
            if (TextUtils.equals(domain, domains[idx])) {
                selected = idx;
            }
        }
        AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setSingleChoiceItems(domains, selected, (dialog, which) -> {
                    LogUtil.d("DomainUtil.showDomainDialog() / Selected : " + which + ", " + domains[which]);
                    dialog.dismiss();
                    DomainUtil.putDomain(domains[which]);
                    RetrofitBuilder.reset();
                    SignOutService.start();
                })
                .setNegativeButton(R.string.jandi_cancel, null)
                .create();
        alertDialog.show();
        return alertDialog;
    }
}
