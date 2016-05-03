package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.text.TextUtils;
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

import java.util.ArrayList;
import java.util.Collection;

import de.greenrobot.event.EventBus;

public class FileMessageViewHolder extends BaseMessageViewHolder {

    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private View vDisableLineThrough;

    private ImageView ivFileImage;
    private TextView tvFileName;
    private TextView tvFileUploaderName;
    private TextView tvCommonFileSize;
    private TextView tvFileInfoDivider;
    private View vgFileContent;

    private FileMessageViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        ivFileImage = (ImageView) rootView.findViewById(R.id.iv_message_common_file);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_common_file_name);
        tvFileUploaderName = (TextView) rootView.findViewById(R.id.tv_uploader_name);
        tvFileInfoDivider = (TextView) rootView.findViewById(R.id.tv_file_info_divider);
        tvCommonFileSize = (TextView) rootView.findViewById(R.id.tv_common_file_size);

        vgFileContent = rootView.findViewById(R.id.vg_message_common_file);

    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        setProfileInfos(link);
        setFileInfo(link, teamId, roomId);
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_file_v3;
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


        FormattedEntity entity = entityManager.getEntityById(fromEntityId);

        ResLeftSideMenu.User fromEntity = entity.getUser();

        if (link.message instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;

            if (fromEntity.id != fileMessage.writerId) {
                String name = EntityManager.getInstance()
                        .getEntityById(fileMessage.writerId).getName();
                tvFileUploaderName.setText(name);
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

                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_text_light));
                tvFileUploaderName.setVisibility(View.GONE);
                tvFileInfoDivider.setVisibility(View.GONE);
                tvCommonFileSize.setVisibility(View.GONE);

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
                tvFileUploaderName.setText(R.string.jandi_unshared_file);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color.jandi_text_light));
                tvCommonFileSize.setVisibility(View.GONE);
                tvFileInfoDivider.setVisibility(View.GONE);
                tvFileUploaderName.setVisibility(View.VISIBLE);
            } else {
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color.dark_gray));
                tvFileName.setText(fileMessage.content.title);
                String name = EntityManager.getInstance()
                        .getEntityById(fileMessage.writerId).getName();
                tvFileUploaderName.setText(name);
                ResMessages.FileContent fileContent = ((ResMessages.FileMessage) link.message).content;
                String fileSize = FileUtil.fileSizeCalculation(fileContent.size);
                tvCommonFileSize.setText(fileSize);

                int mimeTypeIconImage =
                        MimeTypeUtil.getMimeTypeIconImage(
                                fileMessage.content.serverUrl, fileMessage.content.icon);
                ivFileImage.setImageResource(mimeTypeIconImage);
                tvCommonFileSize.setVisibility(View.VISIBLE);
                tvFileInfoDivider.setVisibility(View.VISIBLE);
                tvFileUploaderName.setVisibility(View.VISIBLE);

            }
        }

        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
    }

    public void setProfileInfos(ResMessages.Link link) {
        long fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        if (entity.getUser() != null && entity.isEnabled()) {
            tvName.setTextColor(JandiApplication.getContext().getResources().getColor(R.color.jandi_messages_name));
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    tvName.getResources().getColor(R.color.deactivate_text_color));
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        tvName.setText(entity.getName());
        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntityId, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntityId, ShowProfileEvent.From.Name)));
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        vgFileContent.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        vgFileContent.setOnLongClickListener(itemLongClickListener);
    }

    public void setHasBottomMargin(boolean hasBottomMargin) {
        this.hasBottomMargin = hasBottomMargin;
    }

    public static class Builder extends BaseViewHolderBuilder {

        public FileMessageViewHolder build() {
            FileMessageViewHolder fileViewHolder = new FileMessageViewHolder();
            fileViewHolder.setHasBottomMargin(hasBottomMargin);
            return fileViewHolder;
        }
    }

}
