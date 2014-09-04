package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.CircleTransform;
import com.tosslab.jandi.app.utils.DateTransformator;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 19..
 */
@EViewGroup(R.layout.item_file_detail_comment)
public class FileDetailCommentView extends LinearLayout {
    private final Logger log = Logger.getLogger(FileDetailCommentView.class);

    @ViewById(R.id.img_file_detail_comment_user_profile)
    ImageView imageViewCommentUserProfile;
    @ViewById(R.id.txt_file_detail_comment_user_name)
    TextView textViewCommentUserName;
    @ViewById(R.id.txt_file_detail_comment_create_date)
    TextView textViewCommentFileCreateDate;
    @ViewById(R.id.txt_file_detail_comment_content_2)
    TextView textViewCommentContent;

    Context mContext;

    public FileDetailCommentView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(ResMessages.CommentMessage commentMessage) {
        // 프로필
        final FormattedEntity writer = new FormattedEntity(commentMessage.writer);
        String profileUrl = writer.getUserSmallProfileUrl();
        Picasso.with(mContext)
                .load(profileUrl)
                .placeholder(R.drawable.jandi_profile_comment)
                .transform(new CircleTransform())
                .into(imageViewCommentUserProfile);
        imageViewCommentUserProfile.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                EventBus.getDefault().post(new RequestUserInfoEvent(writer.getId()));
            }
        });
        // 이름
        String userName = writer.getUserName();
        textViewCommentUserName.setText(userName);
        // 날짜
        String createTime = DateTransformator.getTimeDifference(commentMessage.updateTime);
        textViewCommentFileCreateDate.setText(createTime);
        // 댓글 내용
        textViewCommentContent.setText(commentMessage.content.body);
    }
}
