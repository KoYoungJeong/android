package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class PureCommentViewHolder implements BodyViewHolder {

    private TextView nameTextView;
    private TextView dateTextView;
    private TextView commentTextView;
    private View disableLineThroughView;
    private TextView unreadTextView;

    @Override
    public void initView(View rootView) {
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_nested_comment_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_create_date);
        commentTextView = (TextView) rootView.findViewById(R.id.txt_message_nested_comment_content);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);

        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);
    }

    @Override
    public void bindData(ResMessages.Link link) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance(nameTextView.getContext()).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        EntityManager entityManager = EntityManager.getInstance(nameTextView.getContext());
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        if (entityById != null && entityById.getUser() != null && TextUtils.equals(entityById.getUser().status, "enabled")) {

            disableLineThroughView.setVisibility(View.GONE);
            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.jandi_messages_name));
        } else {
            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.deactivate_text_color));
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        nameTextView.setText(fromEntity.name);
        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.time));
        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;
            commentTextView.setText(commentMessage.content.body);

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(commentMessage.content.body);

            boolean hasLink = LinkifyUtil.addLinks(commentTextView.getContext(), spannableStringBuilder);

            commentTextView.setText(spannableStringBuilder);
            if (hasLink) {
                commentTextView.setMovementMethod(LinkMovementMethod.getInstance());
            }
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_cmt_without_file_v2;

    }
}
