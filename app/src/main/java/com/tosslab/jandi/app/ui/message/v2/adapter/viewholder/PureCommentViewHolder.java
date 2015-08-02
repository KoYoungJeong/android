package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class PureCommentViewHolder implements BodyViewHolder {

    private TextView nameTextView;
    private TextView dateTextView;
    private TextView commentTextView;
    private View disableLineThroughView;
    private TextView unreadTextView;
    private Context context;

    @Override
    public void initView(View rootView) {
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_nested_comment_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_create_date);
        commentTextView = (TextView) rootView.findViewById(R.id.txt_message_nested_comment_content);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);
        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);
        context = rootView.getContext();
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById.getUser();
        if (entityById != null && user != null && TextUtils.equals(user.status, "enabled")) {
            disableLineThroughView.setVisibility(View.GONE);
            nameTextView.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
        } else {
            nameTextView.setTextColor(context.getResources().getColor(R.color.deactivate_text_color));
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        nameTextView.setText(fromEntity.name);
        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.time));

        int unreadCount = UnreadCountUtil.getUnreadCount(context,
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        unreadTextView.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            unreadTextView.setVisibility(View.GONE);
        } else {
            unreadTextView.setVisibility(View.VISIBLE);
        }

        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;
            commentTextView.setText(commentMessage.content.body);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");

            boolean hasLink = LinkifyUtil.addLinks(context, spannableStringBuilder);

            for (MentionObject mention : commentMessage.mentions) {
                String name = spannableStringBuilder.subSequence(mention.getOffset() + 1, mention.getLength() + mention.getOffset()).toString();
                MensionCommentSpannable spannable1 = new MensionCommentSpannable(commentTextView.getContext(), name);
                spannableStringBuilder.setSpan(spannable1, mention.getOffset(), mention.getLength() + mention.getOffset(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            if (hasLink) {
                commentTextView.setText(
                        Spannable.Factory.getInstance().newSpannable(spannableStringBuilder));
                LinkifyUtil.setOnLinkClick(commentTextView);
            } else {
                commentTextView.setText(spannableStringBuilder);
            }


        }

        nameTextView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_cmt_without_file_v2;

    }

}
