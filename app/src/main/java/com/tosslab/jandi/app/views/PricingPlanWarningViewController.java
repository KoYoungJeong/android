package com.tosslab.jandi.app.views;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.ApplicationUtil;

import java.lang.ref.WeakReference;
import java.util.Locale;

import butterknife.Bind;
import butterknife.ButterKnife;
import io.intercom.android.sdk.Intercom;

/**
 * Created by tee on 2016. 10. 25..
 */

public class PricingPlanWarningViewController {

    public static final int TYPE_UPLOAD = 0x01;
    public static final int TYPE_MSG_SEARCH = 0x02;

    @Bind(R.id.iv_pricing_plan_warning_remove)
    ImageView ivRemoveViewButton;
    @Bind(R.id.tv_show_pricing_plan)
    TextView tvShowPricePlanButton;
    @Bind(R.id.tv_pricing_plan_warning_inquiry_button)
    TextView tvRequestInquiryButton;
    @Bind(R.id.tv_pricing_plan_warning_msg)
    TextView tvMessage;
    @Bind(R.id.tv_pricing_plan_warning_team_name)
    TextView tvTeamName;

    private WeakReference<Context> contextWeakReference;

    private PricingPlanWarningViewController(Context context, View view, int type) {
        contextWeakReference = new WeakReference<>(context);
        ButterKnife.bind(this, view);
        tvShowPricePlanButton.setOnClickListener(v -> onClickShowPricePlan());
        tvRequestInquiryButton.setOnClickListener(v -> onClickRequestInquiry());
        initInfos(type);
    }

    public static PricingPlanWarningViewController newInstance(Context context, View view, int type) {
        return new PricingPlanWarningViewController(context, view, type);
    }

    private void initInfos(int type) {
        String teamName = TeamInfoLoader.getInstance().getTeamName();
        String message = "";
        switch (type) {
            case TYPE_UPLOAD:
                message = contextWeakReference.get().getString(R.string.common_pricingplan_fileupload);
                break;
            case TYPE_MSG_SEARCH:
                message = contextWeakReference.get().getString(R.string.common_pricingplan_msgsearch);
                break;
        }
        tvMessage.setText(message);
        tvTeamName.setText(teamName);
    }

    public void showRemoveButton(OnClickRemoveViewListener onClickRemoveViewListener) {
        ivRemoveViewButton.setVisibility(View.VISIBLE);
        ivRemoveViewButton.setOnClickListener(v -> onClickRemoveViewListener.onClickRemoveViewListener());
    }

    public void onClickShowPricePlan() {
        Locale locale = contextWeakReference.get().getResources().getConfiguration().locale;
        String lang = locale.getLanguage();
        String url = "https://www.jandi.com/landing/ko/pricing";

        if (TextUtils.equals(lang, "en")) {
            url = "www.jandi.com/landing/en/pricing";
        } else if (TextUtils.equals(lang, "ja")) {
            url = "www.jandi.com/landing/ja/pricing";
        } else if (TextUtils.equals(lang, "ko")) {
            url = "www.jandi.com/landing/ko/pricing";
        } else if (TextUtils.equals(lang, "zh-cn")) {
            url = "www.jandi.com/landing/zh-cn/pricing";
        } else if (TextUtils.equals(lang, "zh-tw")) {
            url = "www.jandi.com/landing/zh-tw/pricing";
        }

        ApplicationUtil.startWebBrowser(contextWeakReference.get(), url);
    }

    public void onClickRequestInquiry() {
        Intercom.client().displayMessenger();
    }

    public interface OnClickRemoveViewListener {
        void onClickRemoveViewListener();
    }

}
