package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.markdown.MarkdownLookUp;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel.MarkdownViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.LinkifyUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class PureCommentViewHolder implements BodyViewHolder {

    private TextView tvName;
    private TextView tvDate;
    private TextView tvComment;
    private View vDisableLineThrough;
    private TextView tvUnread;
    private Context context;
    private View vLastRead;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_nested_comment_user_name);
        tvDate = (TextView) rootView.findViewById(R.id.tv_message_commented_create_date);
        tvComment = (TextView) rootView.findViewById(R.id.tv_message_nested_comment_content);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);
        tvUnread = (TextView) rootView.findViewById(R.id.tv_entity_listitem_unread);
        context = rootView.getContext();
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        long fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance().getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById.getUser();
        if (entityById != EntityManager.UNKNOWN_USER_ENTITY && user != null && TextUtils.equals(user.status, "enabled")) {
            vDisableLineThrough.setVisibility(View.GONE);
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
        } else {
            tvName.setTextColor(context.getResources().getColor(R.color.deactivate_text_color));
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        tvName.setText(fromEntity.name);
        tvDate.setText(DateTransformator.getTimeStringForSimple(link.time));

        int unreadCount = UnreadCountUtil.getUnreadCount(
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        tvUnread.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
        }

        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;
            tvComment.setText(commentMessage.content.body);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");

            GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                    tvComment, spannableStringBuilder, commentMessage.mentions, entityManager.getMe().getId())
                    .setPxSize(R.dimen.jandi_mention_comment_item_font_size);
            spannableStringBuilder = generateMentionMessageUtil.generate(true);

            MarkdownLookUp.text(spannableStringBuilder).lookUp(tvComment.getContext());

            MarkdownViewModel markdownViewModel = new MarkdownViewModel(tvComment, spannableStringBuilder, false);
            markdownViewModel.execute();

            LinkifyUtil.addLinks(context, spannableStringBuilder);
            LinkifyUtil.setOnLinkClick(tvComment);

            tvComment.setText(spannableStringBuilder);


        }

        tvName.setOnClickListener(v ->
                EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
    }

    @Override
    public void setLastReadViewVisible(long currentLinkId, long lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            vLastRead.setVisibility(View.VISIBLE);
        } else {
            vLastRead.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_cmt_without_file_v2;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (contentView != null && itemClickListener != null) {
            contentView.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (contentView != null && itemLongClickListener != null) {
            contentView.setOnLongClickListener(itemLongClickListener);
        }
    }

}
