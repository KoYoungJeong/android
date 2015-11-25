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
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.file.FileSizeUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import java.util.ArrayList;
import java.util.Collection;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class FileViewHolder implements BodyViewHolder {

    private ImageView ivProfile;
    private TextView tvName;
    private TextView tvDate;
    private ImageView ivFileImage;
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

        ivFileImage = (ImageView) rootView.findViewById(R.id.iv_message_common_file);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_common_file_name);
        tvFileType = (TextView) rootView.findViewById(R.id.tv_common_file_type);
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
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        FormattedEntity room = entityManager.getEntityById(roomId);

        boolean isPublicTopic = room.isPublicTopic();

        String profileUrl = entity.getUserLargeProfileUrl();

        BitmapUtil.loadImageByIon(ivProfile,
                profileUrl,
                R.drawable.profile_img,
                R.drawable.profile_img
        );

        if (TextUtils.equals(fromEntity.status, "enabled")) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    context.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        int unreadCount = UnreadCountUtil.getUnreadCount(
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        tvUnread.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            tvUnread.setVisibility(View.GONE);
        } else {
            tvUnread.setVisibility(View.VISIBLE);
        }

        tvName.setText(fromEntity.name);
        tvDate.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (link.message instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;

            if (fromEntity.id != fileMessage.writerId) {
                tvUploader.setVisibility(View.VISIBLE);
                String shared = tvUploader.getContext().getString(R.string.jandi_shared);
                String name = EntityManager.getInstance()
                        .getEntityById(fileMessage.writerId).getName();
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
            } else {
                tvUploader.setVisibility(View.GONE);
            }

            boolean isSharedFile = false;

            Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities = ((ResMessages.FileMessage) link.message).shareEntities;

            // ArrayList로 나오는 경우 아직 DB에 기록되지 않은 경우 - object가 자동갱신되지 않는 문제 해결
            if (shareEntities instanceof ArrayList) {
                ResMessages.FileMessage file = MessageRepository.getRepository().getFileMessage(link.message.id);
                shareEntities = file.shareEntities;
            }

            for (ResMessages.OriginalMessage.IntegerWrapper e : shareEntities) {
                if (e.getShareEntity() == roomId) {
                    isSharedFile = true;
                }
            }

            int fileNameTextSizePX;
            if (TextUtils.equals(link.message.status, "archived")) {

                tvFileName.setText(R.string.jandi_deleted_file);
                fileNameTextSizePX = tvFileName.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_medium);
                ivFileImage.setImageResource(R.drawable.jandi_fl_icon_deleted);
                tvFileType.setVisibility(View.GONE);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_text_light));

            } else if (!isSharedFile) {
                fileNameTextSizePX = tvFileName.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_medium);
                tvFileName.setText(fileMessage.content.title);

                if (isPublicTopic) {
                    int mimeTypeIconImage =
                            MimeTypeUtil.getMimeTypeIconImage(
                                    fileMessage.content.serverUrl, fileMessage.content.icon);
                    ivFileImage.setImageResource(mimeTypeIconImage);
                } else {
                    ivFileImage.setImageResource(R.drawable.file_icon_unshared_141);
                }

                ivFileImage.setClickable(false);
                tvFileType.setText(R.string.jandi_unshared_file);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_text_light));
            } else {
                fileNameTextSizePX = tvFileName.getResources().getDimensionPixelSize(R.dimen.jandi_entity_item_title_font);
                tvFileName.setTextSize(tvFileName.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_medium));
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color.jandi_messages_file_name));
                tvFileName.setText(fileMessage.content.title);
                MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileMessage.content.serverUrl);
                switch (sourceType) {
                    case S3:
                        String fileSize = FileSizeUtil.fileSizeCalculation(fileMessage.content.size);
                        String fileType = String.format("%s, %s", fileSize, fileMessage.content.ext);
                        tvFileType.setText(fileType);
                        break;
                    case Google:
                    case Dropbox:
                        tvFileType.setText(fileMessage.content.ext);
                        break;
                }
                int mimeTypeIconImage =
                        MimeTypeUtil.getMimeTypeIconImage(
                                fileMessage.content.serverUrl, fileMessage.content.icon);
                ivFileImage.setImageResource(mimeTypeIconImage);
                tvFileType.setVisibility(View.VISIBLE);
            }
            tvFileName.setTextSize(TypedValue.COMPLEX_UNIT_PX, fileNameTextSizePX);
        }

        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
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
        return R.layout.item_message_file_v2;
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
