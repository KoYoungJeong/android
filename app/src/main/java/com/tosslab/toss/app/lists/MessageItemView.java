package com.tosslab.toss.app.lists;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.toss.app.R;
import com.tosslab.toss.app.utils.DateTransformator;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EViewGroup(R.layout.item_message)
public class MessageItemView extends LinearLayout {
    @ViewById(R.id.txt_message_user_name)
    TextView mUserName;
    @ViewById(R.id.txt_message_create_date)
    TextView mCreateTime;
    @ViewById(R.id.txt_message_content)
    TextView mMessageContent;
    @ViewById(R.id.img_message_user_profile)
    ImageView mUserProfileImage;

    @ViewById(R.id.ry_file_message)
    RelativeLayout mLayoutFileMessage;
    @ViewById(R.id.txt_file_name)
    TextView mTextFileName;
    // 사진 파일
    @ViewById(R.id.img_message_photo)
    ImageView mImagePhoto;
    @ViewById(R.id.txt_img_file_type)
    TextView mTextImageFileType;
    // 일반 파일
    @ViewById(R.id.img_message_common_file)
    ImageView mImageCommonFile;
    @ViewById(R.id.txt_common_file_type)
    TextView mTextCommonFileType;



    Context mContext;

    public MessageItemView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(MessageItem item) {
        // Initiate
        mLayoutFileMessage.setVisibility(GONE);
        mMessageContent.setText("");
        mImagePhoto.setVisibility(GONE);
        mTextImageFileType.setText("");
        mImageCommonFile.setVisibility(GONE);
        mTextCommonFileType.setText("");

        mUserName.setText(item.getUserNickName());
        // 메시지 String
        if (item.getContentType() == MessageItem.TYPE_STRING) {
            mMessageContent.setText(item.getContentString());
        } else if (item.getContentType() == MessageItem.TYPE_IMAGE) {
            mLayoutFileMessage.setVisibility(VISIBLE);
            mTextFileName.setText(item.getContentFileName());
            mImagePhoto.setVisibility(VISIBLE);
            mTextImageFileType.setText(item.getContentFileType() + " / " + item.getContentFileSize());
            Picasso.with(mContext).load(item.getContentUrl()).centerCrop().fit().into(mImagePhoto);
        } else if (item.getContentType() == MessageItem.TYPE_FILE) {
            mLayoutFileMessage.setVisibility(VISIBLE);
            mTextFileName.setText(item.getContentFileName());
            mImageCommonFile.setVisibility(VISIBLE);
            mTextCommonFileType.setText(item.getContentFileSize());
        }

        // 시간
        String createTime = DateTransformator.getTimeDifference(item.getTime());
        mCreateTime.setText(createTime);

        // 프로필 사진
        Picasso.with(mContext).load(item.getUserProfileUrl()).fit().into(mUserProfileImage);

    }
}
