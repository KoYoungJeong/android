package com.tosslab.toss.app.navigation;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
    public static final String sRootUrl = "https://112.219.215.146:3000/";

    @ViewById(R.id.txt_message_user_name)
    TextView mUserName;

    @ViewById(R.id.txt_message_create_date)
    TextView mCreateTime;

    @ViewById(R.id.txt_message_content)
    TextView mMessageContent;

    @ViewById(R.id.img_message_user_profile)
    ImageView mUserProfileImage;

    @ViewById(R.id.img_message_photo)
    ImageView mMessagePhoto;

    Context mContext;

    public MessageItemView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(MessageItem item) {
        // Initiate
        mMessagePhoto.setVisibility(GONE);
        mMessageContent.setText("");


        mUserName.setText(item.userNickName);
        // 메시지 String
        if (item.contentType == MessageItem.TYPE_STRING) {
            mMessageContent.setText(item.contentString);
        } else if (item.contentType == MessageItem.TYPE_IMAGE) {
            mMessagePhoto.setVisibility(VISIBLE);
            Picasso.with(mContext).load(sRootUrl + item.contentString).centerCrop().fit().into(mMessagePhoto);
        }

        // 시간
        String createTime = DateTransformator.getTimeDifference(item.createTime);
        mCreateTime.setText(createTime);

        Picasso.with(mContext).load(sRootUrl + item.userProfileUrl).fit().into(mUserProfileImage);

    }
}
