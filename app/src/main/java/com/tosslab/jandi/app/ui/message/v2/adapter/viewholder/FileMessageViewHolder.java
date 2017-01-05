package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.res.Resources;
import android.graphics.Typeface;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import java.util.ArrayList;
import java.util.Collection;

import rx.android.schedulers.AndroidSchedulers;

public class FileMessageViewHolder extends BaseMessageViewHolder {

    private ImageView ivProfile;
    private TextView tvName;
    private View vDisableLineThrough;

    private ImageView ivFileImage;
    private TextView tvFileName;
    private TextView tvFileUploaderName;
    private TextView tvCommonFileSize;
    private TextView tvFileInfoDivider;
    private View vgFileContent;
    private View vProfileCover;
    private View vFileIconBorder;

    private FileMessageViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        if (hasProfile) {
            ivProfile = (ImageView) rootView.findViewById(R.id.iv_message_user_profile);
            vProfileCover = rootView.findViewById(R.id.v_message_user_profile_cover);
            tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
            vDisableLineThrough = rootView.findViewById(R.id.iv_name_line_through);
        }

        ivFileImage = (ImageView) rootView.findViewById(R.id.iv_message_common_file);
        vFileIconBorder = rootView.findViewById(R.id.v_message_common_file_border);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_common_file_name);
        tvFileUploaderName = (TextView) rootView.findViewById(R.id.tv_uploader_name);
        tvFileInfoDivider = (TextView) rootView.findViewById(R.id.tv_file_info_divider);
        tvCommonFileSize = (TextView) rootView.findViewById(R.id.tv_common_file_size);

        vgFileContent = rootView.findViewById(R.id.vg_message_common_file);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        setMarginVisible();
        setTimeVisible();
        if (hasProfile) {
            ProfileUtil.setProfile(link.fromEntity, ivProfile, vProfileCover, tvName, vDisableLineThrough);
        }

        setFileInfo(link, teamId, roomId);
        setFileBackground(link);
    }

    private void setFileBackground(ResMessages.Link link) {
        long writerId = link.fromEntity;
        if (TeamInfoLoader.getInstance().getMyId() == writerId) {
            vgFileContent.setBackgroundResource(R.drawable.bg_message_item_selector_mine);
        } else {
            vgFileContent.setBackgroundResource(R.drawable.bg_message_item_selector);
        }
    }

    @Override
    public int getLayoutId() {
        if (hasProfile) {
            return R.layout.item_message_file_v3;
        } else {
            return R.layout.item_message_file_collapse_v3;
        }
    }

    private void setFileInfo(ResMessages.Link link, long teamId, long roomId) {
        long fromEntityId = link.fromEntity;

        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();

        Room room = teamInfoLoader.getRoom(roomId);

        boolean isPublicTopic = room.isPublicTopic();

        UnreadCountUtil.getUnreadCount(
                roomId, link.id, fromEntityId, teamInfoLoader.getMyId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(unreadCount -> {

                    tvMessageBadge.setText(String.valueOf(unreadCount));

                    if (unreadCount <= 0) {
                        tvMessageBadge.setVisibility(View.GONE);
                    } else {
                        tvMessageBadge.setVisibility(View.VISIBLE);
                    }
                });


        tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.time));

        Resources resources = tvFileName.getResources();

        tvFileUploaderName.setTypeface(Typeface.DEFAULT_BOLD);
        tvFileUploaderName.setTextColor(resources.getColor(R.color.jandi_text));

        User entity = teamInfoLoader.getUser(fromEntityId);

        if (link.message instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;

            if (fromEntityId != fileMessage.writerId) {
                String name = TeamInfoLoader.getInstance().getMemberName(fileMessage.writerId);
                tvFileUploaderName.setText(name);
            }

            boolean isSharedFile = false;

            boolean isAssociate = TeamInfoLoader.getInstance().getMyLevel() == Level.Guest;

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
            tvFileName.setGravity(Gravity.NO_GRAVITY);

            boolean loadIcon = true;
            if (TextUtils.equals(link.message.status, "archived")) {
                tvFileName.setText(R.string.jandi_deleted_file);
                ivFileImage.setImageResource(R.drawable.file_icon_deleted_135);

                tvFileName.setTextColor(resources.getColor(R.color.jandi_text_light));
                tvFileUploaderName.setTextColor(resources.getColor(R.color.jandi_text_light));
                tvFileUploaderName.setVisibility(View.GONE);
                tvFileInfoDivider.setVisibility(View.GONE);
                tvCommonFileSize.setVisibility(View.GONE);
                loadIcon = false;
                vFileIconBorder.setVisibility(View.GONE);
            } else if (!isSharedFile) {
                tvFileName.setText(fileMessage.content.title);
                if (isAssociate || !isPublicTopic) {
                    ivFileImage.setImageResource(R.drawable.file_icon_unshared);
                    loadIcon = false;
                    vFileIconBorder.setVisibility(View.GONE);
                    vgFileContent.setOnClickListener(v -> {
                        Toast.makeText(JandiApplication.getContext()
                                , R.string.jandi_unshared_message, Toast.LENGTH_SHORT).show();
                    });
                } else {
                    vFileIconBorder.setVisibility(View.VISIBLE);
                }
                ivFileImage.setClickable(false);
                tvFileUploaderName.setText(R.string.jandi_unshared_file);
                tvFileName.setTextColor(resources.getColor(R.color.jandi_text_light));
                tvFileUploaderName.setTextColor(resources.getColor(R.color.jandi_text_light));
                tvCommonFileSize.setVisibility(View.GONE);
                tvFileInfoDivider.setVisibility(View.GONE);
                tvFileUploaderName.setVisibility(View.VISIBLE);
                tvFileUploaderName.setTypeface(Typeface.DEFAULT);
            } else {
                tvFileName.setTextColor(resources.getColor(R.color.dark_gray));
                tvFileName.setText(fileMessage.content.title);
                String name = TeamInfoLoader.getInstance().getMemberName(fileMessage.writerId);
                tvFileUploaderName.setVisibility(View.VISIBLE);
                tvFileUploaderName.setText(name);
                ResMessages.FileContent fileContent = ((ResMessages.FileMessage) link.message).content;
                String fileSize = FileUtil.formatFileSize(fileContent.size);
                tvCommonFileSize.setText(fileSize);
                vFileIconBorder.setVisibility(View.VISIBLE);

                int mimeTypeIconImage =
                        MimeTypeUtil.getMimeTypeIconImage(
                                fileMessage.content.serverUrl, fileMessage.content.icon, SourceTypeUtil.TYPE_C);
                ivFileImage.setImageResource(mimeTypeIconImage);
                tvCommonFileSize.setVisibility(View.VISIBLE);
                if (fileContent.size > 0) {
                    tvFileInfoDivider.setVisibility(View.VISIBLE);
                    tvCommonFileSize.setVisibility(View.VISIBLE);
                } else {
                    tvFileInfoDivider.setVisibility(View.GONE);
                    tvCommonFileSize.setVisibility(View.GONE);
                }

                tvFileUploaderName.setTextColor(resources.getColor(R.color.jandi_text));
            }

            if (loadIcon) {
                ResMessages.FileContent content = fileMessage.content;
                String serverUrl = content.serverUrl;
                String fileType = content.icon;
                String fileUrl = content.fileUrl;
                String thumbnailUrl =
                        ImageUtil.getThumbnailUrl(content);
                ImageUtil.setResourceIconOrLoadImageForComment(
                        ivFileImage, vFileIconBorder,
                        fileUrl, thumbnailUrl,
                        serverUrl, fileType);
            }
        }


    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        if (!vgFileContent.hasOnClickListeners()) {
            vgFileContent.setOnClickListener(itemClickListener);
        }
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
