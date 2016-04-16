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

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import java.util.ArrayList;
import java.util.Collection;

import de.greenrobot.event.EventBus;

public class FileMessageViewHolder extends BaseMessageViewHolder {

    private Context context;

    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private View vDisableCover;
    private View vDisableLineThrough;

    private ImageView ivFileImage;
    private TextView tvFileName;
    private TextView tvFileExtraInfo;

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        ivFileImage = (ImageView) rootView.findViewById(R.id.iv_message_common_file);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_common_file_name);
        tvFileExtraInfo = (TextView) rootView.findViewById(R.id.tv_common_file_type);

        context = rootView.getContext();
    }

    @Override
    protected void initObjects() {
        vgMessageContent.setVisibility(View.GONE);
        vgStickerMessageContent.setVisibility(View.GONE);
        vgFileMessageContent.setVisibility(View.VISIBLE);
        vgImageMessageContent.setVisibility(View.GONE);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        setProfileInfos(link);
        setFileInfo(link, teamId, roomId);
    }

    private void setFileInfo(ResMessages.Link link, long teamId, long roomId) {
        long fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();

        FormattedEntity room = entityManager.getEntityById(roomId);

        boolean isPublicTopic = room.isPublicTopic();

        int unreadCount = UnreadCountUtil.getUnreadCount(
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        tvMessageBadge.setText(String.valueOf(unreadCount));

        if (unreadCount <= 0) {
            tvMessageBadge.setVisibility(View.GONE);
        } else {
            tvMessageBadge.setVisibility(View.VISIBLE);
        }

        tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.time));

        boolean isFileFromMe = true;

        FormattedEntity entity = entityManager.getEntityById(fromEntityId);

        ResLeftSideMenu.User fromEntity = entity.getUser();

        if (link.message instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;

            if (fromEntity.id != fileMessage.writerId) {
                isFileFromMe = false;
                String shared = JandiApplication.getContext().getString(R.string.jandi_shared);
                String name = EntityManager.getInstance()
                        .getEntityById(fileMessage.writerId).getName();
                String ofFile = JandiApplication.getContext().getString(R.string.jandi_who_of_file);

                SpannableStringBuilder builder = new SpannableStringBuilder();
                builder.append(shared).append(" ");
                int startIdx = builder.length();
                builder.append(name);
                int lastIdx = builder.length();
                builder.append(ofFile);

                int textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, tvFileExtraInfo
                        .getResources().getDisplayMetrics());

                builder.setSpan(new NameSpannable(textSize, Color.BLACK),
                        startIdx,
                        lastIdx,
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                tvFileExtraInfo.setText(builder);
            }

            boolean isSharedFile = false;

            Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities = ((ResMessages.FileMessage) link.message).shareEntities;

            // ArrayList로 나오는 경우 아직 DB에 기록되지 않은 경우 - object가 자동갱신되지 않는 문제 해결
            if (shareEntities instanceof ArrayList) {
                ResMessages.FileMessage file = MessageRepository.getRepository().getFileMessage(link.message.id);
                shareEntities = file != null ? file.shareEntities : shareEntities;
            }

            if (shareEntities != null) {
                for (ResMessages.OriginalMessage.IntegerWrapper e : shareEntities) {
                    if (e.getShareEntity() == roomId) {
                        isSharedFile = true;
                    }
                }
            }

            ivFileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            if (TextUtils.equals(link.message.status, "archived")) {
                tvFileName.setText(R.string.jandi_deleted_file);
                ivFileImage.setImageResource(R.drawable.file_icon_deleted);
                tvFileExtraInfo.setVisibility(View.GONE);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_text_light));

            } else if (!isSharedFile) {
                tvFileName.setText(fileMessage.content.title);

                if (isPublicTopic) {
                    int mimeTypeIconImage =
                            MimeTypeUtil.getMimeTypeIconImage(
                                    fileMessage.content.serverUrl, fileMessage.content.icon);
                    ivFileImage.setImageResource(mimeTypeIconImage);
                } else {
                    ivFileImage.setImageResource(R.drawable.file_icon_unshared);
                }

                ivFileImage.setClickable(false);
                tvFileExtraInfo.setText(R.string.jandi_unshared_file);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_text_light));
            } else {
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color.dark_gray));
                tvFileName.setText(fileMessage.content.title);
                if (isFileFromMe) {
                    MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileMessage.content.serverUrl);
                    switch (sourceType) {
                        case S3:
                            String fileSize = FileUtil.fileSizeCalculation(fileMessage.content.size);
                            String fileType = String.format("%s, %s", fileSize, fileMessage.content.ext);
                            tvFileExtraInfo.setText(fileType);
                            break;
                        case Google:
                        case Dropbox:
                            tvFileExtraInfo.setText(fileMessage.content.ext);
                            break;
                    }
                }
                int mimeTypeIconImage =
                        MimeTypeUtil.getMimeTypeIconImage(
                                fileMessage.content.serverUrl, fileMessage.content.icon);
                ivFileImage.setImageResource(mimeTypeIconImage);
                tvFileExtraInfo.setVisibility(View.VISIBLE);

            }
        }

        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
    }

    public void setProfileInfos(ResMessages.Link link) {
        long fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        if (fromEntity != null && entity.isEnabled()) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    tvName.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        tvName.setText(fromEntity.name);
        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        super.setOnItemClickListener(itemClickListener);
        vgFileMessageContent.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        super.setOnItemLongClickListener(itemLongClickListener);
        vgImageMessageContent.setOnLongClickListener(itemLongClickListener);
    }

    public void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    public static class Builder extends BaseViewHolderBuilder {
//        private boolean hasBottomMargin = false;
//
//        public Builder setHasBottomMargin(boolean hasBottomMargin) {
//            this.hasBottomMargin = hasBottomMargin;
//            return this;
//        }

        public FileMessageViewHolder build() {
            FileMessageViewHolder fileViewHolder = new FileMessageViewHolder();
            fileViewHolder.setHasBottomMargin(hasBottomMargin);
            return fileViewHolder;
        }
    }

}
