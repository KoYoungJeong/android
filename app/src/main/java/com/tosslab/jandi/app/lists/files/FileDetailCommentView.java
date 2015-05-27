package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.graphics.Color;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.LinkifyUtil;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@EViewGroup(R.layout.item_file_detail_comment)
public class FileDetailCommentView extends LinearLayout {

    @ViewById(R.id.img_file_detail_comment_user_profile)
    ImageView imageViewCommentUserProfile;
    @ViewById(R.id.txt_file_detail_comment_user_name)
    TextView textViewCommentUserName;
    @ViewById(R.id.txt_file_detail_comment_create_date)
    TextView textViewCommentFileCreateDate;
    @ViewById(R.id.txt_file_detail_comment_content_2)
    TextView textViewCommentContent;

    @ViewById(R.id.img_entity_listitem_line_through)
    View disableLineThrougView;

    @ViewById(R.id.view_entity_listitem_warning)
    View disableCoverView;

    Context mContext;

    public FileDetailCommentView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(ResMessages.CommentMessage commentMessage) {
        // 프로필
        final FormattedEntity writer = EntityManager.getInstance(mContext).getEntityById(commentMessage.writerId);

        String profileUrl = writer.getUserSmallProfileUrl();
        EntityManager entityManager = EntityManager.getInstance(imageViewCommentUserProfile.getContext());
        if (TextUtils.equals(entityManager.getEntityById(commentMessage.writerId).getUser().status, "enabled")) {
            disableLineThrougView.setVisibility(View.GONE);
            disableCoverView.setVisibility(View.GONE);
            textViewCommentUserName.setTextColor(Color.BLACK);
        } else {
            disableLineThrougView.setVisibility(View.VISIBLE);
            disableCoverView.setVisibility(View.VISIBLE);
            textViewCommentUserName.setTextColor(getResources().getColor(R.color.deactivate_text_color));
        }

        Ion.with(imageViewCommentUserProfile)
                .placeholder(R.drawable.jandi_profile_comment)
                .error(R.drawable.jandi_profile_comment)
                .transform(new IonCircleTransform())
                .load(profileUrl);

        imageViewCommentUserProfile.setOnClickListener(view -> EventBus.getDefault().post(new RequestUserInfoEvent(writer.getId())));
        // 이름
        String userName = writer.getName();
        textViewCommentUserName.setText(userName);
        // 날짜
        String createTime = DateTransformator.getTimeDifference(commentMessage.createTime);
        textViewCommentFileCreateDate.setText(createTime);
        // 댓글 내용
        SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        spannableStringBuilder.append(commentMessage.content.body);

        boolean hasLink = LinkifyUtil.addLinks(textViewCommentContent.getContext(), spannableStringBuilder);

        if (hasLink) {
            textViewCommentContent.setText(Spannable.Factory.getInstance().newSpannable(spannableStringBuilder));
            LinkifyUtil.setOnLinkClick(textViewCommentContent);
        } else {
            textViewCommentContent.setText(spannableStringBuilder);
        }
    }
}
