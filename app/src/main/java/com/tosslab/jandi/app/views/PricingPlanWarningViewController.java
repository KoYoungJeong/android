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

    private PricingPlanWarningViewController(Context context, View view) {
        contextWeakReference = new WeakReference<>(context);
        ButterKnife.bind(this, view);
        tvShowPricePlanButton.setOnClickListener(v -> onClickShowPricePlan());
        tvRequestInquiryButton.setOnClickListener(v -> onClickRequestInquiry());
    }

    public static PricingPlanWarningViewController with(Context context, View view) {
        return new PricingPlanWarningViewController(context, view);
    }

    public void bind(int type) {
        initInfos(type);
    }

    private void initInfos(int type) {
        String teamName = TeamInfoLoader.getInstance().getTeamName();
        String message = "";
        Context context = contextWeakReference.get();
        if (context != null) {
            switch (type) {
                case TYPE_UPLOAD:
                    message = context.getString(R.string.common_pricingplan_fileupload);
                    break;
                case TYPE_MSG_SEARCH:
                    message = context.getString(R.string.common_pricingplan_msgsearch);
                    break;
            }
        }
        tvMessage.setText(message);
        tvTeamName.setText(teamName);
    }

    public PricingPlanWarningViewController addViewRemoveButton(OnClickRemoveViewListener onClickRemoveViewListener) {
        ivRemoveViewButton.setVisibility(View.VISIBLE);
        ivRemoveViewButton.setOnClickListener(v -> onClickRemoveViewListener.onClickRemoveViewListener());
        return this;
    }

    public void onClickShowPricePlan() {
        Context context = contextWeakReference.get();
        if (context != null) {
            Locale locale = context.getResources().getConfiguration().locale;
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

            ApplicationUtil.startWebBrowser(context, url);
        }
    }

    public void onClickRequestInquiry() {
        Intercom.client().displayMessenger();
    }

    public interface OnClickRemoveViewListener {
        void onClickRemoveViewListener();
    }

}
