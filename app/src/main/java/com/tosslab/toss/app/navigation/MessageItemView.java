package com.tosslab.toss.app.navigation;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.toss.app.R;
import com.tosslab.toss.app.utils.DateTransformator;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
@EViewGroup(R.layout.item_message)
public class MessageItemView extends LinearLayout {
    private static final String sRootUrl = "https://192.168.0.11:3000/";

    @ViewById(R.id.txt_message_user_name)
    TextView mUserName;

    @ViewById(R.id.txt_message_create_date)
    TextView mCreateTime;

    @ViewById(R.id.txt_message_content)
    TextView mMessageContent;

    @ViewById(R.id.img_message_user_profile)
    ImageView mUserProfileImage;

    Context mContext;

    public MessageItemView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(MessageItem item) {
        mUserName.setText(item.userNickName);
        // 메시지 String
        if (item.contentType == MessageItem.TYPE_STRING) {
            mMessageContent.setText(item.contentString);
        }
        // 시간
        String createTime = DateTransformator.getTimeDifference(item.createTime);
        mCreateTime.setText(createTime);

        Picasso.with(mContext).load(sRootUrl + item.userProfileUrl).fit().into(mUserProfileImage);

    }
}
