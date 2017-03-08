package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.ApplicationUtil;

import java.util.Locale;

/**
 * Created by tee on 2017. 2. 22..
 */

public class LimitMessageViewHolder implements BodyViewHolder {

    private TextView tvSeeDetailButton;

    @Override
    public void initView(View rootView) {
        tvSeeDetailButton = (TextView) rootView.findViewById(R.id.tv_see_detail_button);
        tvSeeDetailButton.setOnClickListener(v -> {
            onClickShowPricePlan(rootView.getContext());
        });
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

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {

    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_limit;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {

    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {

    }

    public static class Builder extends BaseViewHolderBuilder {
        public LimitMessageViewHolder build() {
            LimitMessageViewHolder viewHolder = new LimitMessageViewHolder();
            return viewHolder;
        }
    }

}
