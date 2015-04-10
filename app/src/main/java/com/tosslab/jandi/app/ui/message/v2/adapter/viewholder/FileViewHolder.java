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
import com.tosslab.jandi.app.utils.FormatConverter;
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
    private View disableCoverView;
    private View disableLineThroughView;
    private TextView unreadTextView;

    private FileViewHolder() {
    }

    public static FileViewHolder createFileViewHolder() {
        return new FileViewHolder();
    }


    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_common_file);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_common_file_name);
        fileTypeTextView = (TextView) rootView.findViewById(R.id.txt_common_file_type);

        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);

        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);
    }

    @Override
    public void bindData(ResMessages.Link link) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance(nameTextView.getContext()).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = ((fromEntity.u_photoThumbnailUrl != null) && TextUtils.isEmpty(fromEntity.u_photoThumbnailUrl.largeThumbnailUrl)) ? fromEntity.u_photoThumbnailUrl.largeThumbnailUrl : fromEntity.u_photoUrl;

        EntityManager entityManager = EntityManager.getInstance(profileImageView.getContext());
        if (TextUtils.equals(entityManager.getEntityById(fromEntity.id).getUser().status, "enabled")) {

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

        profileImageView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));

    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_file_v2;

    }
}
