package com.tosslab.jandi.app.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;

import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by tee on 2016. 10. 25..
 */

public class PricingPlanWarningViewController {

    @Bind(R.id.tv_show_deatil_button)
    TextView tvShowDetailButton;
    @Bind(R.id.tv_noshow_3days_button)
    TextView tvNoShow3daysButton;
    @Bind(R.id.tv_pricing_plan_warning_msg)
    TextView tvMessage;
    @Bind(R.id.tv_pricing_plan_warning_team_name)
    TextView tvTeamName;

    private WeakReference<Context> contextWeakReference;

    private OnClickRemoveViewListener onClickRemoveViewListener;

    private PricingPlanWarningViewController(Context context, View view) {
        contextWeakReference = new WeakReference<>(context);
        ButterKnife.bind(this, view);
        tvShowDetailButton.setOnClickListener(v -> onClickShowDetail());
        tvNoShow3daysButton.setOnClickListener(v -> onClickNoShow3days());
    }

    public static PricingPlanWarningViewController with(Context context, View view) {
        return new PricingPlanWarningViewController(context, view);
    }

    public void bind() {
        initInfos();
    }

    private void initInfos() {
        String teamName = TeamInfoLoader.getInstance().getTeamName();
        tvTeamName.setText(teamName);
    }

    public void onClickShowDetail() {
        Context context = contextWeakReference.get();
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
        context = null;
    }

    public void onClickNoShow3days() {
        JandiPreference.setExceedPopupNotShowRecordTime();
        onClickRemoveViewListener.onClickRemoveViewListener();
    }

    public void setOnClickRemoveViewListener(OnClickRemoveViewListener onClickRemoveViewListener) {
        this.onClickRemoveViewListener = onClickRemoveViewListener;
    }

    public interface OnClickRemoveViewListener {
        void onClickRemoveViewListener();
    }

}