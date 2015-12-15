package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.AutoScaleImageView;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class ImageViewHolder implements BodyViewHolder {

    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvDate;
    private AutoScaleImageView ivFileImage;
    private TextView tvFileName;
    private TextView tvFileType;
    private TextView tvUploader;
    private View vDisableCover;
    private View vDisableLineThrough;
    private TextView tvUnread;
    private Context context;
    private View vLastRead;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvDate = (TextView) rootView.findViewById(R.id.tv_message_create_date);

        ivFileImage = (AutoScaleImageView) rootView.findViewById(R.id.iv_message_photo);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_image_file_name);
        tvFileType = (TextView) rootView.findViewById(R.id.tv_img_file_type);
        tvUploader = (TextView) rootView.findViewById(R.id.tv_img_file_uploader);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        tvUnread = (TextView) rootView.findViewById(R.id.tv_entity_listitem_unread);
        context = rootView.getContext();
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {
        int fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity fromEntity = entityManager.getEntityById(fromEntityId);

        boolean isUnknownUser = fromEntity == EntityManager.UNKNOWN_USER_ENTITY;
        ResLeftSideMenu.User user = isUnknownUser ? null : fromEntity.getUser();

        bindUser(user, fromEntity.getUserLargeProfileUrl());

        bindUnreadCount(link.id, teamId, roomId, fromEntityId, entityManager.getMe().getId());

        tvDate.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (!(link.message instanceof ResMessages.FileMessage)) {
            return;
        }

        ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;
        ResMessages.FileContent fileContent = fileMessage.content;
        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileContent.serverUrl);

        bindUploader(user, fileMessage.writerId);

        bindFileImage(fileMessage, fileContent, sourceType);
    }

    private void bindUser(ResLeftSideMenu.User user, String userProfileUrl) {
        BitmapUtil.loadCropCircleImageByGlideBitmap(ivProfile,
                userProfileUrl,
                R.drawable.profile_img,
                R.drawable.profile_img
        );

        tvName.setText(user != null ? user.name : "");

        if (user != null && TextUtils.equals(user.status, "enabled")) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            tvName.setText(user.name);
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);

            int userId = user.id;
            ShowProfileEvent eventFromImage = new ShowProfileEvent(userId, ShowProfileEvent.From.Image);
            ivProfile.setOnClickListener(v -> EventBus.getDefault().post(eventFromImage));

            ShowProfileEvent eventFromName = new ShowProfileEvent(userId, ShowProfileEvent.From.Name);
            tvName.setOnClickListener(v -> EventBus.getDefault().post(eventFromName));
        } else {
            tvName.setTextColor(
                    context.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);

            ivProfile.setOnClickListener(null);
            tvName.setOnClickListener(null);
        }
    }

    private void bindUnreadCount(int linkId, int teamId, int roomId, int fromEntityId, int myId) {
        int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId, linkId, fromEntityId, myId);
        tvUnread.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
        }
    }

    private void bindUploader(ResLeftSideMenu.User user, int writerId) {
        if (isWriter(user, writerId)) {
            tvUploader.setVisibility(View.GONE);
        } else {
            tvUploader.setVisibility(View.VISIBLE);
            String shared = tvUploader.getContext().getString(R.string.jandi_shared);
            String name = EntityManager.getInstance().getEntityById(writerId).getName();
            String ofFile = tvUploader.getContext().getString(R.string.jandi_who_of_file);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(shared).append(" ");
            int startIdx = builder.length();
            builder.append(name);
            int lastIdx = builder.length();
            builder.append(ofFile);

            int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, tvUploader
                    .getResources().getDisplayMetrics());

            builder.setSpan(new NameSpannable(textSize, Color.BLACK),
                    startIdx,
                    lastIdx,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            tvUploader.setText(builder);
        }
    }

    private boolean isWriter(ResLeftSideMenu.User user, int writerId) {
        return user != null && user.id == writerId;
    }

    void bindFileImage(ResMessages.FileMessage fileMessage,
                       ResMessages.FileContent fileContent, MimeTypeUtil.SourceType sourceType) {

        if (TextUtils.equals(fileMessage.status, "archived")) {
            tvFileName.setText(R.string.jandi_deleted_file);
            ivFileImage.setImageResource(R.drawable.jandi_fview_icon_deleted);
            tvFileType.setText("");
            return;
        }

        tvFileName.setText(fileContent.title);

        if (!BitmapUtil.hasImageUrl(fileContent)) {
            ivFileImage.setImageResource(R.drawable.file_icon_img);
            return;
        }

        // Google, Dropbox 파일이 인 경우
        if (isFileFromGoogleOrDropbox(sourceType)) {
            String serverUrl = fileContent.serverUrl;
            String icon = fileContent.icon;
            int mimeTypeIconImage = MimeTypeUtil.getMimeTypeIconImage(serverUrl, icon);

            ivFileImage.setImageResource(mimeTypeIconImage);
            tvFileType.setText(fileContent.ext);
        } else {
            String localFilePath = BitmapUtil.getLocalFilePath(fileMessage.id);
            String remoteFilePth =
                    BitmapUtil.getThumbnailUrlOrOriginal(fileContent, BitmapUtil.Thumbnails.LARGE);
            String thumbPath = !TextUtils.isEmpty(localFilePath) ? localFilePath : remoteFilePth;

            ResMessages.ThumbnailUrls extraInfo = fileContent.extraInfo;
            if (extraInfo != null && extraInfo.width > 0 && extraInfo.height > 0) {
//                LogUtil.i(AutoScaleImageView.TAG, String.format("load From spec %s, %d, %d, %d", thumbPath, extraInfo.width, extraInfo.height, extraInfo.orientation));
                ivFileImage.load(thumbPath, extraInfo.width, extraInfo.height, extraInfo.orientation);
            } else {
//                LogUtil.i(AutoScaleImageView.TAG, String.format("load From undefined spec %s", thumbPath));
                ivFileImage.load(thumbPath);
            }

            String fileSize = FileUtil.fileSizeCalculation(fileContent.size);
            tvFileType.setText(String.format("%s, %s", fileSize, fileContent.ext));
        }
    }

    private boolean isFileFromGoogleOrDropbox(MimeTypeUtil.SourceType sourceType) {
        return sourceType == MimeTypeUtil.SourceType.Google
                || sourceType == MimeTypeUtil.SourceType.Dropbox;
    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            vLastRead.setVisibility(View.VISIBLE);
        } else {
            vLastRead.setVisibility(View.GONE);
        }
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_img_v2;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (contentView != null && itemClickListener != null) {
            contentView.setOnClickListener(itemClickListener);
        }
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        if (contentView != null && itemLongClickListener != null) {
            contentView.setOnLongClickListener(itemLongClickListener);
        }
    }

}
