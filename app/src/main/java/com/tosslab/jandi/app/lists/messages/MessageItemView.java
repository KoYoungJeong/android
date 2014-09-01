package com.tosslab.jandi.app.lists.messages;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.CircleTransform;
import com.tosslab.jandi.app.utils.DateTransformator;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EViewGroup(R.layout.item_message)
public class MessageItemView extends LinearLayout {
    private final Logger log = Logger.getLogger(MessageItemView.class);
    // 날짜 경계선
    @ViewById(R.id.ly_message_date_devider)
    LinearLayout mLayoutDateDivider;
    @ViewById(R.id.txt_message_date_devider)
    TextView mDateDevider;

    // 진짜 메시지 본문
    @ViewById(R.id.ly_message_item)
    LinearLayout mLayoutMessageItem;

    // 메시지에선 항상 보이는 컨텐츠들...
    @ViewById(R.id.ly_message_default)
    LinearLayout mLayoutMessageDefault;
    @ViewById(R.id.img_message_user_profile)
    ImageView mUserProfileImage;
    @ViewById(R.id.txt_message_user_name)
    TextView mUserName;
    @ViewById(R.id.txt_message_create_date)
    TextView mCreateTime;
    @ViewById(R.id.txt_message_content)
    TextView mMessageContent;

    // 댓글에 대한 표시일 경우
    @ViewById(R.id.ly_message_commented)
    RelativeLayout mLayoutMessageComment;
    @ViewById(R.id.txt_message_commented_owner)
    TextView mTextMessageCommentOwner;
    @ViewById(R.id.txt_message_commented_file_name)
    TextView mTextMessageCommentFileName;
    @ViewById(R.id.txt_message_commented_content)
    TextView mTextMessageCommentContent;

    // 내부 댓글일 경우
    @ViewById(R.id.ly_message_nested_comment)
    RelativeLayout mLayoutMessageNestedComment;
    @ViewById(R.id.txt_message_nested_comment_user_name)
    TextView mTextMessageNestedCommentUserName;
    @ViewById(R.id.txt_message_commented_create_date)
    TextView mTextMessageNestedCommentCreateDate;
    @ViewById(R.id.txt_message_nested_comment_content)
    TextView mTextMessageNestedCommentContent;


    // 이미지일 경우
    @ViewById(R.id.ry_message_image_file)
    RelativeLayout mLayoutMessageImageFile;
    @ViewById(R.id.txt_message_image_file_name)
    TextView mTextImageFileName;
    @ViewById(R.id.img_message_photo)
    ImageView mImagePhoto;
    @ViewById(R.id.txt_img_file_type)
    TextView mTextImageFileType;

    // 일반 파일
    @ViewById(R.id.ry_message_common_file)
    RelativeLayout mLayoutMessageCommonFile;
    @ViewById(R.id.txt_message_common_file_name)
    TextView mTextMessageCommonFileName;
    @ViewById(R.id.txt_common_file_type)
    TextView mTextMessageCommonFileType;

    Context mContext;

    public MessageItemView(Context context) {
        super(context);
        mContext = context;
    }

    private void hideAllView() {
        mLayoutDateDivider.setVisibility(GONE);
        mLayoutMessageItem.setVisibility(GONE);
        mLayoutMessageDefault.setVisibility(GONE);
        mLayoutMessageComment.setVisibility(GONE);
        mLayoutMessageNestedComment.setVisibility(GONE);
        mLayoutMessageImageFile.setVisibility(GONE);
        mLayoutMessageCommonFile.setVisibility(GONE);
        mMessageContent.setVisibility(GONE);
    }

    private void showDateDivider(MessageItem item) {
        // 날짜 경계선으로 표시
        mLayoutDateDivider.setVisibility(VISIBLE);
        if (item.isToday)
            mDateDevider.setText(R.string.today);
        else
            mDateDevider.setText(DateTransformator.getTimeStringForDivider(item.getCurrentDateDevider()));
    }

    private void showUserNickName(MessageItem item) {
        mUserName.setText(item.getUserName());
    }

    private void showMessageDate(MessageItem item) {
        // 시간
        String createTime = DateTransformator.getTimeStringForSimple(item.getLinkTime());
        mCreateTime.setText(createTime);
    }

    private void showProfilePhoto(MessageItem item) {
        // 프로필 사진
        mUserProfileImage.setVisibility(VISIBLE);
        Picasso.with(mContext).load(item.getUserProfileUrl()).placeholder(R.drawable.jandi_profile).transform(new CircleTransform()).into(mUserProfileImage);
    }

    private void showStringMessage(MessageItem item) {
        // 일반 메시지일 경우
        mMessageContent.setVisibility(VISIBLE);
        mMessageContent.setText(item.getContentString());
    }

    private void showComment(MessageItem item) {
        // 메시지 타입이 댓글 표시일 경우
        mLayoutMessageComment.setVisibility(VISIBLE);
        mTextMessageCommentOwner.setText(item.getFeedbackWriterNickName());
        mTextMessageCommentFileName.setText(item.getFeedbackFileName());
        mTextMessageCommentContent.setText(item.getContentString());
    }

    private void showNestedComment(MessageItem item) {
        mUserProfileImage.setVisibility(INVISIBLE);
        // 메시지 타입이 댓글이고 아래에 위치한 경우
        mLayoutMessageNestedComment.setVisibility(VISIBLE);
        mTextMessageNestedCommentUserName.setText(item.getUserName());
        String createTime = DateTransformator.getTimeStringForSimple(item.getLinkTime());
        mTextMessageNestedCommentCreateDate.setText(createTime);
        mTextMessageNestedCommentContent.setText(item.getContentString());

    }

    private void showImageFileView(MessageItem item) {
        // 메시지 타입이 이미지인 경우
        mLayoutMessageImageFile.setVisibility(VISIBLE);
        mTextImageFileName.setText(item.getContentFileName());
        mTextImageFileType.setText(item.getContentFileSize() + " " + item.getContentFileType());
        if (item.getContentSmallThumbnailUrl() != null) {
            String imageUrl = item.getContentSmallThumbnailUrl().replaceAll(" ", "%20");
            Picasso.with(mContext).load(imageUrl).placeholder(R.drawable.jandi_sicon_thumbnail).centerCrop().fit().into(mImagePhoto);
        }
    }

    private void showCommonFileView(MessageItem item) {
        // 일반 파일인 경우
        mLayoutMessageCommonFile.setVisibility(VISIBLE);
        mTextMessageCommonFileName.setText(item.getContentFileName());
        mTextMessageCommonFileType.setText(item.getContentFileSize());
    }

    private void showDefaultInfos(MessageItem item) {
        mLayoutMessageDefault.setVisibility(VISIBLE);
        showUserNickName(item);
        showMessageDate(item);
        showProfilePhoto(item);
    }

    public void bind(MessageItem item) {
        // Initiate
        hideAllView();

        if (item.isDateDivider) {
            showDateDivider(item);
        } else {
            // 실제 컨탠츠를 표시
            mLayoutMessageItem.setVisibility(VISIBLE);

            // 메시지 String
            if (item.getContentType() == MessageItem.TYPE_STRING) {
                showDefaultInfos(item);
                showStringMessage(item);
            } else if (item.getContentType() == MessageItem.TYPE_COMMENT) {
                if (item.isNested) {
                    showNestedComment(item);
                } else {
                    showDefaultInfos(item);
                    showComment(item);
                }
            } else if (item.getContentType() == MessageItem.TYPE_IMAGE) {
                showDefaultInfos(item);
                showImageFileView(item);
            } else if (item.getContentType() == MessageItem.TYPE_FILE) {
                showDefaultInfos(item);
                showCommonFileView(item);
            }
        }
    }
}
