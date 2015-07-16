package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FileSizeUtil;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

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
    private TextView unreadTextView;
    private Context context;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileImageView = (ImageView) rootView.findViewById(R.id.iv_message_photo);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_image_file_name);
        fileTypeTextView = (TextView) rootView.findViewById(R.id.txt_img_file_type);
        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);

        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);
        context = rootView.getContext();
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity =
                EntityManager.getInstance(context).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != null ? entityById.getUser() : null;
        if (user != null && TextUtils.equals(user.status, "enabled")) {
            nameTextView.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            disableCoverView.setVisibility(View.GONE);
            disableLineThroughView.setVisibility(View.GONE);
        } else {
            nameTextView.setTextColor(
                    context.getResources().getColor(R.color.deactivate_text_color));
            disableCoverView.setVisibility(View.VISIBLE);
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        int unreadCount = UnreadCountUtil.getUnreadCount(context,
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        unreadTextView.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            unreadTextView.setVisibility(View.GONE);
        } else {
            unreadTextView.setVisibility(View.VISIBLE);
        }

        nameTextView.setText(fromEntity.name);
        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (link.message instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;

            ResMessages.FileContent fileContent = fileMessage.content;
            MimeTypeUtil.SourceType sourceType =
                    SourceTypeUtil.getSourceType(fileContent.serverUrl);

            if (TextUtils.equals(fileMessage.status, "archived")) {

                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileImageView.setImageResource(R.drawable.jandi_fview_icon_deleted);
                fileImageView.setClickable(false);
            } else {
                if (BitmapUtil.hasImageUrl(fileContent)) {
                    // Google, Dropbox 파일이 인 경우
                    if (sourceType == MimeTypeUtil.SourceType.Google
                            || sourceType == MimeTypeUtil.SourceType.Dropbox) {
                        int mimeTypeIconImage =
                                MimeTypeUtil.getMimeTypeIconImage(
                                        fileContent.serverUrl, fileContent.icon);
                        fileImageView.setImageResource(mimeTypeIconImage);
                        fileImageView.setOnClickListener(view -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(
                                            BitmapUtil.getThumbnailUrlOrOriginal(
                                                    fileContent, BitmapUtil.Thumbnails.ORIGINAL)));
                            context.startActivity(intent);
                        });
                    } else {

                        fileImageView.setClickable(false);

                        // small 은 80 x 80 사이즈가 로딩됨 -> medium 으로 로딩
                        String mediumThumb =
                                BitmapUtil.getThumbnailUrlOrOriginal(
                                        fileContent, BitmapUtil.Thumbnails.MEDIUM);

                        Glide.with(fileImageView.getContext())
                                .load(mediumThumb)
                                .placeholder(R.drawable.jandi_fl_icon_img)
                                .error(R.drawable.jandi_fl_icon_img)
                                .crossFade()
                                .into(fileImageView);
                    }
                } else {
                    fileImageView.setClickable(false);
                    fileImageView.setImageResource(R.drawable.jandi_fl_icon_img);
                }

                fileNameTextView.setText(fileContent.title);

                fileTypeTextView.setText(FileSizeUtil.fileSizeCalculation(fileContent.size) + ", "
                        + fileContent.ext);
            }

        }
        profileImageView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
        nameTextView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_img_v2;
    }

}
