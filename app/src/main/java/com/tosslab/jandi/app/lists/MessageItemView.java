package com.tosslab.jandi.app.lists;

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
    LinearLayout mLayoutDateDevider;
    @ViewById(R.id.txt_message_date_devider)
    TextView mDateDevider;

    // 진짜 메시지 본문
    @ViewById(R.id.ly_message_item)
    LinearLayout mLayoutMessageItem;
    // 메시지에선 항상 보이는 컨텐츠들...
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
    LinearLayout mLayoutMessageComment;

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

    public void bind(MessageItem item) {
        // Initiate
        mLayoutDateDevider.setVisibility(GONE);
        mLayoutMessageItem.setVisibility(GONE);
        mLayoutMessageComment.setVisibility(GONE);
        mLayoutMessageImageFile.setVisibility(GONE);
        mLayoutMessageCommonFile.setVisibility(GONE);
        mMessageContent.setVisibility(GONE);

        if (item.isDateDivider) {
            // 날짜 경계선으로 표시
            mLayoutDateDevider.setVisibility(VISIBLE);
            if (item.isToday)
                mDateDevider.setText(R.string.today);
            else
                mDateDevider.setText(DateTransformator.getTimeStringForDivider(item.getCurrentDateDevider()));
        } else {
            // 실제 컨탠츠를 표시
            mLayoutMessageItem.setVisibility(VISIBLE);
            mUserName.setText(item.getUserNickName());
            // 시간
            String createTime = DateTransformator.getTimeStringForSimple(item.getLinkTime());
            mCreateTime.setText(createTime);
            // 프로필 사진
            Picasso.with(mContext).load(item.getUserProfileUrl()).placeholder(R.drawable.jandi_profile).transform(new CircleTransform()).into(mUserProfileImage);
            // 메시지 String
            if (item.getContentType() == MessageItem.TYPE_STRING) {
                // 일반 메시지일 경우
                mMessageContent.setVisibility(VISIBLE);
                mMessageContent.setText(item.getContentString());
            } else if (item.getContentType() == MessageItem.TYPE_COMMENT) {
                // 메시지 타입이 댓글 표시일 경우
                mLayoutMessageComment.setVisibility(VISIBLE);
                mMessageContent.setVisibility(VISIBLE);
                mMessageContent.setText(item.getContentString());
            } else if (item.getContentType() == MessageItem.TYPE_IMAGE) {
                // 메시지 타입이 이미지인 경우
                mLayoutMessageImageFile.setVisibility(VISIBLE);
                mTextImageFileName.setText(item.getContentFileName());
                mTextImageFileType.setText(item.getContentFileSize() + " " + item.getContentFileType());
                if (item.getContentSmallThumbnailUrl() != null) {
                    String imageUrl = item.getContentSmallThumbnailUrl().replaceAll(" ", "%20");
                    Picasso.with(mContext).load(imageUrl).placeholder(R.drawable.jandi_sicon_thumbnail).centerCrop().fit().into(mImagePhoto);
                }
            } else if (item.getContentType() == MessageItem.TYPE_FILE) {
                // 일반 파일인 경우
                mLayoutMessageCommonFile.setVisibility(VISIBLE);
                mTextMessageCommonFileName.setText(item.getContentFileName());
                mTextMessageCommonFileType.setText(item.getContentFileSize());
            }
        }
    }
}
