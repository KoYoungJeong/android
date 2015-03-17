package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
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
    private View disableCoverView;
    private View disableLineThroughView;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_photo);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_image_file_name);
        fileTypeTextView = (TextView) rootView.findViewById(R.id.txt_img_file_type);
        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);

    }

    @Override
    public void bindData(ResMessages.Link link) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance(nameTextView.getContext()).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = ((fromEntity.u_photoThumbnailUrl != null) && TextUtils.isEmpty(fromEntity.u_photoThumbnailUrl.largeThumbnailUrl)) ? fromEntity.u_photoThumbnailUrl.largeThumbnailUrl : fromEntity.u_photoUrl;
        EntityManager entityManager = EntityManager.getInstance(profileImageView.getContext());
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        if (entityById != null && entityById.getUser() != null && TextUtils.equals(entityById.getUser().status, "enabled")) {

            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.jandi_messages_name));

            disableCoverView.setVisibility(View.GONE);
            disableLineThroughView.setVisibility(View.GONE);
        } else {

            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.deactivate_text_color));

            disableCoverView.setVisibility(View.VISIBLE);
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + profileUrl);

        nameTextView.setText(fromEntity.name);

        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.time));

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
        profileImageView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_img_v2;

    }
}
