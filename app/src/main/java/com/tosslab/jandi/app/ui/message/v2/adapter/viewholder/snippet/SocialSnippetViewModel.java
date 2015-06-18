package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.snippet;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.views.AutoScaleImageView;

/**
 * Created by Steve SeongUg Jung on 15. 6. 18..
 */
public class SocialSnippetViewModel {

    private TextView tvTitle;
    private TextView tvDomain;
    private TextView tvDescription;
    private AutoScaleImageView ivThumb;
    private OnSnippetClickListener onSnippetClickListener;

    private Context context;
    private ViewGroup vgSnippet;

    public SocialSnippetViewModel(Context context) {
        this.context = context;
    }

    public void initView(View rootView) {
        vgSnippet = (ViewGroup) rootView.findViewById(R.id.vg_snippet);
        vgSnippet.setOnClickListener(onSnippetClickListener = new OnSnippetClickListener(context));

        tvTitle = (TextView) rootView.findViewById(R.id.tv_snippet_title);
        tvDomain = (TextView) rootView.findViewById(R.id.tv_snippet_domain);
        tvDescription = (TextView) rootView.findViewById(R.id.tv_snippet_description);
        ivThumb = (AutoScaleImageView) rootView.findViewById(R.id.iv_snippet_thumb);
        ivThumb.setRatio(274, 115);
    }

    public void bindData(ResMessages.Link link) {

        ResMessages.TextMessage message = (ResMessages.TextMessage) link.message;

        if (message.socialSnippet == null || TextUtils.isEmpty(message.socialSnippet.linkUrl)) {
            vgSnippet.setVisibility(View.GONE);
            return ;
        } else {
            vgSnippet.setVisibility(View.VISIBLE);
        }

        ResMessages.SocialSnippet snippet = message.socialSnippet;

        tvTitle.setText(snippet.title);
        tvDomain.setText(snippet.domain);

        String description = snippet.description;
        tvDescription.setText(TextUtils.isEmpty(description) ? "" : description);
        tvDescription.setVisibility(TextUtils.isEmpty(description) ? View.GONE : View.VISIBLE);

        onSnippetClickListener.setLinkUrl(snippet.linkUrl);

        String imageUrl = snippet.imageUrl;
        if (TextUtils.isEmpty(imageUrl)) {
            ivThumb.setVisibility(View.GONE);
            ivThumb.setImageDrawable(null);
        } else {
            ivThumb.setVisibility(View.VISIBLE);
            Glide.with(context)
                    .load(imageUrl)
                    .asBitmap()
                    .centerCrop()
                    .into(ivThumb);
        }
    }

    private static class OnSnippetClickListener implements View.OnClickListener {
        private final Context context;
        private String linkUrl;

        public OnSnippetClickListener(Context context) {

            this.context = context;
        }

        public void setLinkUrl(String linkUrl) {
            this.linkUrl = linkUrl;
        }

        @Override
        public void onClick(View v) {
            if (TextUtils.isEmpty(linkUrl)) {
                return;
            }

            InternalWebActivity_.intent(context)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .url(linkUrl)
                    .start();

            if (context instanceof Activity) {
                Activity activity = ((Activity) context);
                activity.overridePendingTransition(
                        R.anim.origin_activity_open_enter, R.anim.origin_activity_open_exit);
            }

        }
    }
}
