package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class FileCommentViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView dateTextView;
    private TextView fileOwnerTextView;
    private TextView fileNameTextView;
    private TextView commentTextView;
    private TextView fileOwnerPostfixTextView;
    private ImageView fileImageView;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileOwnerTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_owner);
        fileOwnerPostfixTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_postfix);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_file_name);
        commentTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_content);

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_commented_photo);
    }

    @Override
    public void bindData(ResMessages.Link link) {

        String profileUrl = ((link.message.writer.u_photoThumbnailUrl != null) && TextUtils.isEmpty(link.message.writer.u_photoThumbnailUrl.largeThumbnailUrl)) ? link.message.writer.u_photoThumbnailUrl.largeThumbnailUrl : link.message.writer.u_photoUrl;
        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + profileUrl);

        nameTextView.setText(link.message.writer.name);
        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));

        if (link.feedback instanceof ResMessages.FileMessage) {

            ResMessages.FileMessage feedbackFileMessage = (ResMessages.FileMessage) link.feedback;
            if (TextUtils.equals(link.feedback.status, "archived")) {
                fileOwnerTextView.setVisibility(View.INVISIBLE);
                fileOwnerPostfixTextView.setVisibility(View.INVISIBLE);

                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileImageView.setVisibility(View.VISIBLE);
            } else {
                fileOwnerTextView.setText(feedbackFileMessage.writer.name);
                fileNameTextView.setText(feedbackFileMessage.content.title);

                fileOwnerTextView.setVisibility(View.VISIBLE);
                fileOwnerPostfixTextView.setVisibility(View.VISIBLE);

                String fileType = feedbackFileMessage.content.type;
                if (fileType.startsWith("image/")) {
                    String imageUrl = JandiConstantsForFlavors.SERVICE_ROOT_URL + feedbackFileMessage.content.extraInfo.smallThumbnailUrl.replaceAll(" ", "%20");
                    Ion.with(fileImageView)
                            .placeholder(R.drawable.jandi_fl_icon_img)
                            .error(R.drawable.jandi_fl_icon_img)
                            .crossfade(true)
                            .load(imageUrl);
                } else if (fileType.startsWith("audio")) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_audio);
                } else if (fileType.startsWith("video")) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_video);
                } else if (fileType.startsWith("application/pdf")) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_pdf);
                } else if (fileType.startsWith("text")) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_txt);
                } else if (TextUtils.equals(fileType, "application/x-hwp")) {
                    fileImageView.setImageResource(R.drawable.jandi_fl_icon_hwp);
                } else if (FormatConverter.isSpreadSheetMimeType(fileType)) {
                    fileImageView.setImageResource(R.drawable.jandi_fl_icon_exel);
                } else if (FormatConverter.isPresentationMimeType(fileType)) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_ppt);
                } else if (FormatConverter.isDocmentMimeType(fileType)) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_txt);
                } else {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_etc);
                }
            }

        }

        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;
            commentTextView.setText(commentMessage.content.body);
        }

        profileImageView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(link.message.writerId)));

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_cmt_with_file_v2;
    }
}
