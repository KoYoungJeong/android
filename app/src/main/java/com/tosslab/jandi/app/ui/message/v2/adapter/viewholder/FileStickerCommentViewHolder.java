package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerManager;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import java.util.ArrayList;
import java.util.Collection;

public class FileStickerCommentViewHolder extends BaseCommentViewHolder implements HighlightView {

    private ViewGroup vgMessageCommonFile;
    private ImageView ivMessageCommonFile;

    private ImageView ivProfileNestedUserProfileForSticker;
    private TextView tvProfileNestedUserNameForSticker;
    private ImageView ivProfileNestedLineThroughForSticker;

    private ImageView ivProfileNestedCommentSticker;
    private TextView tvProfileNestedCommentStickerCreateDate;
    private TextView tvProfileNestedCommentStickerUnread;
    private TextView tvMessageCommonFileName;
    private Context context;
    private boolean hasNestedProfile = false;
    private boolean hasOnlyBadge;
    private TextView tvFileUploaderName;
    private TextView tvCommonFileSize;
    private boolean hasFlatTop = false;
    private View tvFileInfoDivider;
    private View vProfileCover;
    private View vFileIconBorder;
    private ViewGroup vgProfileNestedCommentSticker;

    private FileStickerCommentViewHolder() {
    }

    @Override
    public int getLayoutId() {
        if (hasNestedProfile) {
            return R.layout.item_comment_sticker_v3;
        } else {
            return R.layout.item_comment_sticker_collapse_v3;
        }
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        super.setOptionView();

        vgProfileNestedCommentSticker =
                (ViewGroup) rootView.findViewById(R.id.vg_profile_nested_comment_sticker);


        // 파일 정보
        if (hasContentInfo()) {
            vgMessageCommonFile = (ViewGroup) rootView.findViewById(R.id.vg_message_common_file);
            ivMessageCommonFile = (ImageView) rootView.findViewById(R.id.iv_message_common_file);
            tvMessageCommonFileName = (TextView) rootView.findViewById(R.id.tv_message_common_file_name);
            vFileIconBorder = rootView.findViewById(R.id.v_message_common_file_border);
            tvFileUploaderName = (TextView) rootView.findViewById(R.id.tv_uploader_name);
            tvFileInfoDivider = rootView.findViewById(R.id.tv_file_info_divider);
            tvCommonFileSize = (TextView) rootView.findViewById(R.id.tv_common_file_size);
        }

        // 커멘트 스티커 프로필
        if (hasNestedProfile) {
            ivProfileNestedUserProfileForSticker = (ImageView) rootView.findViewById(R.id.iv_profile_nested_user_profile_for_sticker);
            vProfileCover = rootView.findViewById(R.id.v_profile_nested_user_profile_for_sticker_cover);
            tvProfileNestedUserNameForSticker = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_user_name_for_sticker);
            ivProfileNestedLineThroughForSticker = (ImageView) rootView.findViewById(R.id.iv_profile_nested_name_line_through_for_sticker);
        }

        // 스티커
        ivProfileNestedCommentSticker = (ImageView) rootView.findViewById(R.id.iv_profile_nested_comment_sticker);
        tvProfileNestedCommentStickerCreateDate = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_sticker_create_date);
        tvProfileNestedCommentStickerUnread = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_sticker_unread);
        context = rootView.getContext();

        if (hasNestedProfile) {
            ivProfileNestedUserProfileForSticker.setVisibility(View.VISIBLE);
            tvProfileNestedUserNameForSticker.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initObjects() {
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        super.bindData(link, teamId, roomId, entityId);

        boolean hasFileInfoView = hasContentInfo();
        if (hasFileInfoView) {
            settingFileInfo(link, roomId);
            setFileInfoBackground(link);
        }

        if (hasNestedProfile) {
            ProfileUtil.setProfileForCommment(
                    link.fromEntity, ivProfileNestedUserProfileForSticker, vProfileCover,
                    tvProfileNestedUserNameForSticker, ivProfileNestedLineThroughForSticker);
        }

        getStickerComment(link, teamId, roomId);
        setBackground(link);

        if (hasCommentBubbleTail()) {
            // 파일 정보가 없고 내가 쓴 코멘트 인 경우만 comment_bubble_tail_mine resource 사
            vCommentBubbleTail.setBackgroundResource(hasFileInfoView
                    ? R.drawable.bg_comment_bubble_tail :
                    isFromMe(link) ? R.drawable.comment_bubble_tail_mine : R.drawable.bg_comment_bubble_tail);
        }
    }

    private void setFileInfoBackground(ResMessages.Link link) {
        boolean isMe = isFromMe(link);
        if (isMe) {
            vgMessageCommonFile.setBackgroundResource(R.drawable.bg_message_item_selector_mine);
        } else {
            vgMessageCommonFile.setBackgroundResource(R.drawable.bg_message_item_selector);

        }
    }

    private boolean isFromMe(ResMessages.Link link) {
        boolean isMe = false;
        if (link.feedback != null) {
            isMe = TeamInfoLoader.getInstance().getMyId() == link.message.writerId;
        }
        return isMe;
    }

    private void setBackground(ResMessages.Link link) {
        boolean isMe = isFromMe(link);

        int resId;
        if (hasFlatTop) {
            if (hasBottomMargin) {
                if (isMe) {
                    resId = R.drawable.bg_message_item_selector_mine_flat_top;
                } else {
                    resId = R.drawable.bg_message_item_selector_flat_top;
                }
            } else {
                if (isMe) {
                    resId = R.drawable.bg_message_item_selector_mine_flat_all;
                } else {
                    resId = R.drawable.bg_message_item_selector_flat_all;
                }
            }
        } else {
            if (hasBottomMargin) {
                if (isMe) {
                    resId = R.drawable.bg_message_item_selector_mine;
                } else {
                    resId = R.drawable.bg_message_item_selector;

                }
            } else {
                if (isMe) {
                    resId = R.drawable.bg_message_item_selector_mine_flat_bottom;
                } else {
                    resId = R.drawable.bg_message_item_selector_flat_bottom;
                }
            }
        }

        vgProfileNestedCommentSticker.setBackgroundResource(resId);
    }

    private void getStickerComment(ResMessages.Link link, long teamId, long roomId) {
        ResMessages.CommentStickerMessage message = (ResMessages.CommentStickerMessage) link.message;

        StickerManager.getInstance().loadStickerNoOption(ivProfileNestedCommentSticker, message.content.groupId, message.content.stickerId);

        if (hasOnlyBadge) {
            tvProfileNestedCommentStickerCreateDate.setVisibility(View.GONE);
        } else {
            tvProfileNestedCommentStickerCreateDate.setVisibility(View.VISIBLE);
            tvProfileNestedCommentStickerCreateDate.setText(DateTransformator.getTimeStringForSimple(message.createTime));

        }

        if (link.unreadCnt > 0) {
            tvProfileNestedCommentStickerUnread.setText(String.valueOf(link.unreadCnt));
            tvProfileNestedCommentStickerUnread.setVisibility(View.VISIBLE);
        } else {
            tvProfileNestedCommentStickerUnread.setVisibility(View.GONE);
        }

    }

    private void settingFileInfo(ResMessages.Link link, long roomId) {
        boolean isPublicTopic = TeamInfoLoader.getInstance().isPublicTopic(roomId);

        User feedbackEntityById = TeamInfoLoader.getInstance().getUser(link.feedback.writerId);

        tvFileUploaderName.setText(feedbackEntityById.getName());

        if (link.feedback instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage feedbackFileMessage = ((ResMessages.FileMessage) link.feedback);
            ResMessages.FileContent fileContent = feedbackFileMessage.content;
            String fileSize = FileUtil.formatFileSize(fileContent.size);
            tvCommonFileSize.setText(fileSize);

            boolean isSharedFile = false;

            Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities = feedbackFileMessage.shareEntities;

            // ArrayList로 나오는 경우 아직 DB에 기록되지 않은 경우 - object가 자동갱신되지 않는 문제 해결
            if (shareEntities instanceof ArrayList) {
                ResMessages.FileMessage file = MessageRepository.getRepository().getFileMessage(feedbackFileMessage.id);
                if (file != null && file.shareEntities != null) {
                    shareEntities = file.shareEntities;
                }
            }

            if (shareEntities != null) {
                for (ResMessages.OriginalMessage.IntegerWrapper e : shareEntities) {
                    if (e.getShareEntity() == roomId) {
                        isSharedFile = true;
                    }
                }
            }

            tvMessageCommonFileName.setTypeface(null, Typeface.BOLD);

            final Resources resources = tvMessageCommonFileName.getResources();
            boolean needFileUploader = true;
            boolean needFileUploaderDivider = true;
            boolean needFileSize = true;

            if (TextUtils.equals(link.feedback.status, "archived")) {
                tvMessageCommonFileName.setText(R.string.jandi_deleted_file);
                tvMessageCommonFileName.setTextColor(resources.getColor(R.color.jandi_text_light));
                needFileUploader = false;
                needFileUploaderDivider = false;
                needFileSize = false;

                ivMessageCommonFile.setScaleType(ImageView.ScaleType.FIT_CENTER);
                ivMessageCommonFile.setImageResource(R.drawable.file_icon_deleted);
                vFileIconBorder.setVisibility(View.GONE);
                ivMessageCommonFile.setOnClickListener(null);
            } else {
                final ResMessages.FileContent content = feedbackFileMessage.content;

                if (!isSharedFile) {
                    needFileUploaderDivider = false;
                    needFileSize = false;

                    tvMessageCommonFileName.setText(content.title);
                    tvMessageCommonFileName.setTextColor(resources.getColor(R.color.jandi_text_light));
                    tvFileUploaderName.setText(R.string.jandi_unshared_file);
                    tvFileUploaderName.setTextColor(resources.getColor(R.color.jandi_text_light));

                    ivMessageCommonFile.setClickable(false);

                    boolean image = fileContent.icon.startsWith("image");
                    if (!image && !isPublicTopic) {
                        ivMessageCommonFile.setImageResource(R.drawable.file_icon_unshared);
                        vFileIconBorder.setVisibility(View.GONE);
                    } else {
                        String serverUrl = content.serverUrl;
                        String fileType = content.icon;
                        String fileUrl = content.fileUrl;
                        String thumbnailUrl =
                                ImageUtil.getThumbnailUrl(content);
                        ImageUtil.setResourceIconOrLoadImageForComment(
                                ivMessageCommonFile, vFileIconBorder,
                                fileUrl, thumbnailUrl,
                                serverUrl, fileType);
                    }
                } else {
                    if (content.size <= 0) {
                        needFileSize = false;
                        needFileUploaderDivider = false;
                    }
                    tvMessageCommonFileName.setText(content.title);
                    tvMessageCommonFileName.setTextColor(resources.getColor(R.color.dark_gray));
                    tvFileUploaderName.setTextColor(resources.getColor(R.color.dark_gray));

                    String serverUrl = content.serverUrl;
                    String fileType = content.icon;
                    String fileUrl = content.fileUrl;
                    String thumbnailUrl =
                            ImageUtil.getThumbnailUrl(content);
                    ImageUtil.setResourceIconOrLoadImageForComment(
                            ivMessageCommonFile, vFileIconBorder,
                            fileUrl, thumbnailUrl,
                            serverUrl, fileType);

                    MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);
                    if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
                        ivMessageCommonFile.setOnClickListener(v -> {
                            String imageUrl = ImageUtil.getOriginalUrl(content);
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(imageUrl));
                            context.startActivity(intent);
                        });
                    } else {
                        ivMessageCommonFile.setOnClickListener(null);
                    }
                }
            }
            tvFileUploaderName.setVisibility(needFileUploader ? View.VISIBLE : View.GONE);
            tvCommonFileSize.setVisibility(needFileSize ? View.VISIBLE : View.GONE);
            tvFileInfoDivider.setVisibility(needFileUploaderDivider ? View.VISIBLE : View.GONE);
        }
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        super.setOnItemClickListener(itemClickListener);
        if (vgMessageCommonFile != null) {
            vgMessageCommonFile.setOnClickListener(itemClickListener);
        }
        if (vgReadMore != null) {
            vgReadMore.setOnClickListener(itemClickListener);
        }
        vgProfileNestedCommentSticker.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        super.setOnItemLongClickListener(itemLongClickListener);
        vgProfileNestedCommentSticker.setOnLongClickListener(itemLongClickListener);
    }

    private void setHasNestedProfile(boolean hasNestedProfile) {
        this.hasNestedProfile = hasNestedProfile;
    }

    public void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    public void setHasFlatTop(boolean hasFlatTop) {
        this.hasFlatTop = hasFlatTop;
    }

    @Override
    public View getHighlightView() {
        return vgProfileNestedCommentSticker;
    }

    public static class Builder extends BaseViewHolderBuilder {

        public FileStickerCommentViewHolder build() {
            FileStickerCommentViewHolder viewHolder = new FileStickerCommentViewHolder();
            viewHolder.setHasBottomMargin(hasBottomMargin);
            viewHolder.setHasSemiDivider(hasSemiDivider);
            viewHolder.setHasContentInfo(hasFileInfoView);
            viewHolder.setHasCommentBubbleTail(hasCommentBubbleTail);
            viewHolder.setHasNestedProfile(hasNestedProfile);
            viewHolder.setHasViewAllComment(hasViewAllComment);
            viewHolder.setHasOnlyBadge(hasOnlyBadge);
            viewHolder.setHasFlatTop(hasFlatTop);
            return viewHolder;
        }
    }

}
