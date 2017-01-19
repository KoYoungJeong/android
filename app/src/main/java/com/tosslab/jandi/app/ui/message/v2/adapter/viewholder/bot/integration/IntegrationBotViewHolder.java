package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration;

import android.content.Context;
import android.graphics.Color;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.WebhookBot;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.BodyViewHolder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.HighlightView;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.bot.integration.util.IntegrationBotUtil;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.linkpreview.LinkPreviewViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
import com.tosslab.jandi.app.utils.image.transform.TransformConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

public class IntegrationBotViewHolder implements BodyViewHolder, HighlightView {

    private static final String TAG = "IntegrationBotViewHolder";
    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvMessage;
    private View vDisableLineThrough;
    private View vConnectLine;
    private TextView tvMessageTime;
    private TextView tvMessageBadge;
    private View vgConnectInfoWrapper;
    private LinearLayout vgConnectInfo;
    private ViewGroup vLastRead;
    private View vBottomMargin;

    private boolean hasBottomMargin = false;
    private boolean hasOnlyBadge = false;

    private LinkPreviewViewModel linkPreviewViewModel;
    private boolean hasBotProfile;

    private IntegrationBotViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        if (hasBotProfile) {
            ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
            tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
            vDisableLineThrough = rootView.findViewById(R.id.iv_name_line_through);
        }
        tvMessage = (TextView) rootView.findViewById(R.id.tv_message_content);
        tvMessageTime = (TextView) rootView.findViewById(R.id.tv_message_time);
        tvMessageBadge = (TextView) rootView.findViewById(R.id.tv_message_badge);
        vConnectLine = rootView.findViewById(R.id.v_message_sub_menu_connect_color);

        vgConnectInfoWrapper = rootView.findViewById(R.id.vg_message_connect_info_wrapper);
        vgConnectInfo = ((LinearLayout) rootView.findViewById(R.id.vg_message_sub_menu));
        vLastRead = (ViewGroup) rootView.findViewById(R.id.vg_message_last_read);

        linkPreviewViewModel = new LinkPreviewViewModel(rootView.getContext());
        linkPreviewViewModel.initView(rootView);

        vBottomMargin = rootView.findViewById(R.id.v_margin);

        if (hasBottomMargin) {
            vBottomMargin.setVisibility(View.VISIBLE);
        } else {
            vBottomMargin.setVisibility(View.GONE);
        }

        ViewGroup.MarginLayoutParams layoutParams =
                (ViewGroup.MarginLayoutParams) tvMessage.getLayoutParams();
        if (hasBotProfile) {
            ivProfile.setVisibility(View.VISIBLE);
            tvName.setVisibility(View.VISIBLE);
            layoutParams.topMargin = (int) UiUtils.getPixelFromDp(5f);
        } else {
            layoutParams.topMargin = (int) UiUtils.getPixelFromDp(6f);
        }
        tvMessage.setLayoutParams(layoutParams);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        long fromEntityId = link.fromEntity;

        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
        if (!(teamInfoLoader.isBot(fromEntityId))) {
            return;
        }

        WebhookBot webhookBot = TeamInfoLoader.getInstance().getBot(fromEntityId);


        if (hasBotProfile) {
            ImageLoader.newInstance()
                    .placeHolder(R.drawable.profile_img, ImageView.ScaleType.FIT_CENTER)
                    .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    .transformation(new JandiProfileTransform(ivProfile.getContext(),
                            TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                            TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                            Color.WHITE))
                    .uri(Uri.parse(webhookBot.getPhotoUrl()))
                    .into(ivProfile);
            tvName.setText(webhookBot.getName());
            if (webhookBot.isEnabled()) {
                tvName.setTextColor(tvName.getResources().getColor(R.color.jandi_messages_name));
                vDisableLineThrough.setVisibility(View.GONE);
            } else {
                tvName.setTextColor(
                        tvName.getResources().getColor(R.color.deactivate_text_color));
                vDisableLineThrough.setVisibility(View.VISIBLE);
            }
        }


        ResMessages.TextMessage textMessage = (ResMessages.TextMessage) link.message;

        Context context = tvMessage.getContext();

        SpannableStringBuilder messageStringBuilder;
        if (textMessage.content.contentBuilder == null) {

            messageStringBuilder = SpannableLookUp.text(textMessage.content.body)
                    .hyperLink(false)
                    .markdown(false)
                    .webLink(false)
                    .emailLink(false)
                    .telLink(false)
                    .lookUp(context);
            textMessage.content.contentBuilder = messageStringBuilder;
        } else {
            messageStringBuilder = textMessage.content.contentBuilder;
        }

        tvMessage.setText(messageStringBuilder);

        LinkifyUtil.setOnLinkClick(tvMessage);

        if (link.unreadCnt > 0) {
            tvMessageBadge.setText(String.valueOf(link.unreadCnt));
            tvMessageBadge.setVisibility(View.VISIBLE);
        } else {
            tvMessageBadge.setVisibility(View.GONE);
        }

        if (!hasOnlyBadge) {
            tvMessageTime.setVisibility(View.VISIBLE);
            tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
        } else {
            tvMessageTime.setVisibility(View.GONE);
        }


        Collection<ResMessages.ConnectInfo> connectInfo = textMessage.content.connectInfo;
        if (isEmptyConnectInfos(connectInfo)) {
            textMessage.content.connectInfo = Collections.emptyList();
            vgConnectInfoWrapper.setVisibility(View.GONE);
        } else {
            vgConnectInfoWrapper.setVisibility(View.VISIBLE);
            IntegrationBotUtil.setIntegrationSubUI(textMessage.content, vConnectLine, vgConnectInfo);
        }

        linkPreviewViewModel.bindData(link);

    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (vLastRead != null) {
            if (currentLinkId == lastReadLinkId) {
                vLastRead.removeAllViews();
                LayoutInflater.from(vLastRead.getContext())
                        .inflate(R.layout.item_message_last_read_v2, vLastRead);
                vLastRead.setVisibility(View.VISIBLE);
            } else {
                vLastRead.setVisibility(View.GONE);
            }
        }
    }

    @Override
    public int getLayoutId() {
        if (hasBotProfile) {
            return R.layout.item_message_integration_bot_msg_v3;
        } else {
            return R.layout.item_message_integration_bot_msg_v3_collapse;
        }
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (tvMessage != null && itemClickListener != null) {
            tvMessage.setOnClickListener(itemClickListener);
        }
        if (vgConnectInfo != null) {
            vgConnectInfo.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (tvMessage != null && itemLongClickListener != null) {
            tvMessage.setOnLongClickListener(itemLongClickListener);
        }
        if (vgConnectInfo != null) {
            vgConnectInfo.setOnLongClickListener(itemLongClickListener);
        }
    }

    public void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    public void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    public void setHasBotProfile(boolean hasBotProfile) {
        this.hasBotProfile = hasBotProfile;
    }

    private boolean isEmptyConnectInfos(Collection<ResMessages.ConnectInfo> connectInfos) {
        if (connectInfos == null || connectInfos.isEmpty()) {
            return true;
        }

        boolean isEmpty = true;
        Iterator<ResMessages.ConnectInfo> iterator = connectInfos.iterator();
        while (iterator.hasNext()) {
            ResMessages.ConnectInfo connectInfo = iterator.next();
            if (!TextUtils.isEmpty(connectInfo.title) || !TextUtils.isEmpty(connectInfo.description)) {
                isEmpty = false;
                break;
            }
        }

        return isEmpty;
    }

    @Override
    public View getHighlightView() {
        return tvMessage;
    }

    public static class Builder extends BaseViewHolderBuilder {
        public IntegrationBotViewHolder build() {
            IntegrationBotViewHolder integrationBotViewHolder = new IntegrationBotViewHolder();
            integrationBotViewHolder.setHasOnlyBadge(hasOnlyBadge);
            integrationBotViewHolder.setHasBottomMargin(hasBottomMargin);
            integrationBotViewHolder.setHasBotProfile(hasProfile);
            return integrationBotViewHolder;
        }
    }

}
