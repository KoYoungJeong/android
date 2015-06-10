package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.app.Activity;
import android.content.Intent;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;

/**
 * Created by tonyjs on 15. 6. 10..
 */
public class SocialSnippetMessageViewHolder extends MessageViewHolder {
    private TextView tvTitle;
    private TextView tvDomain;
    private TextView tvDescription;
    private ImageView ivThumb;
    private OnSnippetClickListener onSnippetClickListener;

    @Override
    public void initView(View rootView) {
        super.initView(rootView);

        ViewGroup vgSnippet = (ViewGroup) rootView.findViewById(R.id.vg_snippet);
        vgSnippet.setOnClickListener(onSnippetClickListener = new OnSnippetClickListener());

        tvTitle = (TextView) rootView.findViewById(R.id.tv_snippet_title);
        tvDomain = (TextView) rootView.findViewById(R.id.tv_snippet_domain);
        tvDescription = (TextView) rootView.findViewById(R.id.tv_snippet_description);
        ivThumb = (ImageView) rootView.findViewById(R.id.iv_snippet_thumb);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId) {
        super.bindData(link, teamId, roomId);

        ResMessages.TextMessage message = (ResMessages.TextMessage) link.message;
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

    private class OnSnippetClickListener implements View.OnClickListener {
        private String linkUrl;

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

    @Override
    public int getLayoutId() {
        return R.layout.item_message_msg_snippet_v2;
    }
}