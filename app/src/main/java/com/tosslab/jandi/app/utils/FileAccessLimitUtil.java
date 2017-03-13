package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import java.util.Locale;

/**
 * Created by tee on 2017. 2. 21..
 */

public class FileAccessLimitUtil {

    private FileAccessLimitUtil() {
    }

    public static FileAccessLimitUtil newInstance() {
        return new FileAccessLimitUtil();
    }

    public void execute(Context context, OnUsableFileAccessListener onUsableFileAccessListener) {
        if (TeamInfoLoader.getInstance().getTeamPlan().isExceedFile()) {
            showFileAccessLimitDialog(context);
        } else {
            if (onUsableFileAccessListener != null) {
                onUsableFileAccessListener.onUsableFileAccess();
            }
        }
    }

    public void execute(Context context, long teamId, OnUsableFileAccessListener onUsableFileAccessListener) {
        if (TeamInfoLoader.getInstance(teamId).getTeamPlan().isExceedFile()) {
            showFileAccessLimitDialog(context);
        } else {
            if (onUsableFileAccessListener != null) {
                onUsableFileAccessListener.onUsableFileAccess();
            }
        }
    }

    private void showFileAccessLimitDialog(Context context) {

        new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.pricingplan_restrictions_fileupload_popup_title)
                .setMessage(R.string.pricingplan_restrictions_fileupload_popup_desc)
                .setNegativeButton(context.getText(R.string.intercom_close), (dialog, which) -> {
                    dialog.dismiss();
                })
                .setPositiveButton(R.string.pricingplan_restrictions_fileupload_popup_seedetail,
                        (dialog, which) -> {
                            onClickShowPricePlan(context);
                        }).show();


    }

    private void onClickShowPricePlan(Context context) {
        if (context != null) {
            Locale locale = context.getResources().getConfiguration().locale;
            String lang = locale.getLanguage();
            String url = "https://www.jandi.com/landing/kr/pricing";

            if (TextUtils.equals(lang, "en")) {
                url = "www.jandi.com/landing/en/pricing";
            } else if (TextUtils.equals(lang, "ja")) {
                url = "www.jandi.com/landing/jp/pricing";
            } else if (TextUtils.equals(lang, "ko")) {
                url = "www.jandi.com/landing/kr/pricing";
            } else if (TextUtils.equals(lang, "zh-cn")) {
                url = "www.jandi.com/landing/zh-cn/pricing";
            } else if (TextUtils.equals(lang, "zh-tw")) {
                url = "www.jandi.com/landing/zh-tw/pricing";
            }

            ApplicationUtil.startWebBrowser(context, url);
        }
    }

    public interface OnUsableFileAccessListener {
        void onUsableFileAccess();
    }

}
