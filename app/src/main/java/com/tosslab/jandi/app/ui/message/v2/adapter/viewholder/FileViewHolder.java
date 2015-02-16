package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.GlideCircleTransform;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class FileViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView dateTextView;
    private ImageView fileImageView;
    private TextView fileNameTextView;
    private TextView fileTypeTextView;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_common_file);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_common_file_name);
        fileTypeTextView = (TextView) rootView.findViewById(R.id.txt_common_file_type);
    }

    @Override
    public void bindData(ResMessages.Link link) {

        String profileUrl = ((link.message.writer.u_photoThumbnailUrl != null) && TextUtils.isEmpty(link.message.writer.u_photoThumbnailUrl.largeThumbnailUrl)) ? link.message.writer.u_photoThumbnailUrl.largeThumbnailUrl : link.message.writer.u_photoUrl;

        EntityManager entityManager = EntityManager.getInstance(profileImageView.getContext());
        if (TextUtils.equals(entityManager.getEntityById(link.message.writerId).getUser().status, "enabled")) {

            Ion.with(profileImageView)
                    .placeholder(R.drawable.jandi_profile)
                    .error(R.drawable.jandi_profile)
                    .transform(new IonCircleTransform())
                    .crossfade(true)
                    .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + profileUrl);
            nameTextView.setText(link.message.writer.name);
        } else {
            profileImageView.setImageResource(R.drawable.jandi_ic_launcher);
            nameTextView.setText("Disable User");
        }
        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.message.createTime));

        if (link.message instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;

            if (TextUtils.equals(link.message.status, "archived")) {
                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileImageView.setImageResource(R.drawable.jandi_fl_icon_deleted);
                fileTypeTextView.setText("");
            } else {
                fileNameTextView.setText(fileMessage.content.title);
                fileTypeTextView.setText(fileMessage.content.ext);

                if (fileMessage.content.type.startsWith("audio")) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_audio);
                } else if (fileMessage.content.type.startsWith("video")) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_video);
                } else if (fileMessage.content.type.startsWith("application/pdf")) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_pdf);
                } else if (fileMessage.content.type.startsWith("text")) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_txt);
                } else if (TextUtils.equals(fileMessage.content.type, "application/x-hwp")) {
                    fileImageView.setImageResource(R.drawable.jandi_fl_icon_hwp);
                } else if (FormatConverter.isSpreadSheetMimeType(fileMessage.content.type)) {
                    fileImageView.setImageResource(R.drawable.jandi_fl_icon_exel);
                } else if (FormatConverter.isPresentationMimeType(fileMessage.content.type)) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_ppt);
                } else if (FormatConverter.isDocmentMimeType(fileMessage.content.type)) {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_txt);
                } else {
                    fileImageView.setImageResource(R.drawable.jandi_fview_icon_etc);
                }
            }
        }

        profileImageView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(link.message.writerId)));

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_file_v2;

    }
}
