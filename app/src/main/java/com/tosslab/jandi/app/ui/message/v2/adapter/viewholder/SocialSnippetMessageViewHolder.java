package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;

/**
 * Created by tonyjs on 15. 6. 10..
 */
public class SocialSnippetMessageViewHolder extends MessageViewHolder {
    private TextView tvTitle;
    private TextView tvAuthor;
    private TextView tvContent;
    private ImageView ivThumb;
    @Override
    public void initView(View rootView) {
        super.initView(rootView);

        tvTitle = (TextView) rootView.findViewById(R.id.tv_snippet_title);
        tvAuthor = (TextView) rootView.findViewById(R.id.tv_snippet_author);
        tvContent = (TextView) rootView.findViewById(R.id.tv_snippet_content);
        ivThumb = (ImageView) rootView.findViewById(R.id.iv_snippet_thumb);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId) {
        super.bindData(link, teamId, roomId);

        ResMessages.TextMessage message = (ResMessages.TextMessage) link.message;
        ResMessages.SocialSnippetContent content = (ResMessages.SocialSnippetContent) message.content;

        tvTitle.setText(content.title);
        tvAuthor.setText(content.author);
        tvContent.setText(content.content);
        Glide.with(context)
                .load(content.thumbnail)
                .asBitmap()
                .centerCrop()
                .into(ivThumb);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_msg_snippet_v2;
    }
}