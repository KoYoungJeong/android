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
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class ImageViewHolder implements BodyViewHolder {

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

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_photo);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_image_file_name);
        fileTypeTextView = (TextView) rootView.findViewById(R.id.txt_img_file_type);

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

        if (link.message instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;

            String imageUrl = JandiConstantsForFlavors.SERVICE_ROOT_URL + fileMessage.content.extraInfo.smallThumbnailUrl.replaceAll(" ", "%20");


            if (TextUtils.equals(fileMessage.status, "archived")) {

                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileImageView.setImageResource(R.drawable.jandi_fview_icon_deleted);
            } else {

                Ion.with(fileImageView)
                        .placeholder(R.drawable.jandi_fl_icon_img)
                        .error(R.drawable.jandi_fl_icon_img)
                        .crossfade(true)
                        .load(imageUrl);

                fileNameTextView.setText(fileMessage.content.title);
                fileTypeTextView.setText(fileMessage.content.ext);
            }

        }
        profileImageView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(link.message.writerId)));

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_img_v2;

    }
}
