package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class PureCommentViewHolder implements BodyViewHolder {

    private TextView nameTextView;
    private TextView dateTextView;
    private TextView commentTextView;
    private View disableLineThroughView;

    @Override
    public void initView(View rootView) {
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_nested_comment_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_create_date);
        commentTextView = (TextView) rootView.findViewById(R.id.txt_message_nested_comment_content);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);
    }

    @Override
    public void bindData(ResMessages.Link link) {
        EntityManager entityManager = EntityManager.getInstance(nameTextView.getContext());
        FormattedEntity entityById = entityManager.getEntityById(link.message.writerId);
        if (entityById != null && entityById.getUser() != null && TextUtils.equals(entityById.getUser().status, "enabled")) {

            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.deactivate_text_color));
            disableLineThroughView.setVisibility(View.GONE);
        } else {
            disableLineThroughView.setVisibility(View.VISIBLE);
            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.jandi_messages_name));
        }

        nameTextView.setText(link.message.writer.name);
        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));
        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;
            commentTextView.setText(commentMessage.content.body);
        }

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_cmt_without_file_v2;

    }
}
