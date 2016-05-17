package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.util.ProfileUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by tee on 16. 4. 7..
 */
public class CommentViewHolder extends BaseCommentViewHolder {

    private SimpleDraweeView ivMessageCommonFile;
    private TextView tvFileUploaderName;
    private TextView tvCommonFileSize;
    private SimpleDraweeView ivProfileNestedCommentUserProfile;
    private TextView tvProfileNestedCommentUserName;
    private ImageView ivProfileNestedNameLineThrough;
    private TextView tvProfileNestedCommentContent;
    private TextView tvMessageCommonFileName;
    private ViewGroup vgMessageCommonFile;
    private Context context;

    private boolean hasNestedProfile = false;
    private boolean hasOnlyBadge = false;
    private boolean hasFlatTop = false;
    private View tvFileInfoDivider;
    private View vProfileCover;
    private View vFileIconBorder;
    private ViewGroup vgProfileNestedComment;


    private CommentViewHolder() {
    }

    @Override
    public int getLayoutId() {
        if (hasNestedProfile) {
            return R.layout.item_comment_msg_v3;
        } else {
            return R.layout.item_comment_msg_collapse_v3;
        }
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);

        vgProfileNestedComment =
                (ViewGroup) rootView.findViewById(R.id.vg_profile_nested_comment);

        if (hasFileInfoView()) {
            // 파일 정보
            vgMessageCommonFile = (ViewGroup) rootView.findViewById(R.id.vg_message_common_file);
            ivMessageCommonFile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_common_file);
            tvMessageCommonFileName = (TextView) rootView.findViewById(R.id.tv_message_common_file_name);
            vFileIconBorder = rootView.findViewById(R.id.v_message_common_file_border);
            tvFileUploaderName = (TextView) rootView.findViewById(R.id.tv_uploader_name);
            tvFileInfoDivider = rootView.findViewById(R.id.tv_file_info_divider);
            tvCommonFileSize = (TextView) rootView.findViewById(R.id.tv_common_file_size);
        }

        // 프로필이 있는 커멘트
        if (hasNestedProfile) {
            ivProfileNestedCommentUserProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_profile_nested_comment_user_profile);
            vProfileCover = rootView.findViewById(R.id.v_profile_nested_comment_user_profile_cover);
            tvProfileNestedCommentUserName = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_user_name);
            ivProfileNestedNameLineThrough = (ImageView) rootView.findViewById(R.id.iv_profile_nested_name_line_through);
        }

        tvProfileNestedCommentContent = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_content);

        context = rootView.getContext();

        if (hasNestedProfile) {
            ivProfileNestedCommentUserProfile.setVisibility(View.VISIBLE);
            tvProfileNestedCommentUserName.setVisibility(View.VISIBLE);
            ivProfileNestedNameLineThrough.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void initObjects() {
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        super.bindData(link, teamId, roomId, entityId);

        boolean hasFileInfoView = hasFileInfoView();
        if (hasFileInfoView) {
            settingFileInfo(link, roomId);
            setFileInfoBackground(link);
        }

        if (hasNestedProfile) {
            ProfileUtil.setProfile(link.fromEntity, ivProfileNestedCommentUserProfile, vProfileCover,
                    tvProfileNestedCommentUserName, ivProfileNestedNameLineThrough);
        }

        setCommentMessage(link, teamId, roomId);
        setBackground(link);

        if (hasCommentBubbleTail()) {
            // 파일 정보가 없고 내가 쓴 코멘트 인 경우만 comment_bubble_tail_mine resource 사
            ivCommentBubbleTail.setBackgroundResource(hasFileInfoView
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
            isMe = EntityManager.getInstance().isMe(link.feedback.writerId);
        }
        return isMe;
    }

    private void setBackground(ResMessages.Link link) {

        boolean isMe = EntityManager.getInstance().isMe(link.message.writerId);

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
        vgProfileNestedComment.setBackgroundResource(resId);
    }

    private void setCommentMessage(ResMessages.Link link, long teamId, long roomId) {
        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;

            long myId = EntityManager.getInstance().getMe().getId();
            if (commentMessage.content.contentBuilder == null) {

                SpannableStringBuilder messageBuilder = new SpannableStringBuilder();
                messageBuilder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");
                messageBuilder.append(" ");

                MentionAnalysisInfo mentionAnalysisInfo =
                        MentionAnalysisInfo.newBuilder(myId, commentMessage.mentions)
                                .textSize(tvProfileNestedCommentContent.getTextSize())
                                .clickable(true)
                                .build();

                SpannableLookUp.text(messageBuilder)
                        .hyperLink(false)
                        .markdown(false)
                        .webLink(false)
                        .emailLink(false)
                        .telLink(false)
                        .mention(mentionAnalysisInfo, false)
                        .lookUp(tvProfileNestedCommentContent.getContext());

                commentMessage.content.contentBuilder = messageBuilder;
            }

            SpannableStringBuilder builderWithBadge = new SpannableStringBuilder(commentMessage.content.contentBuilder);
            if (!hasOnlyBadge) {
                int startIndex = builderWithBadge.length();
                builderWithBadge.append(DateTransformator.getTimeStringForSimple(commentMessage.createTime));
                int endIndex = builderWithBadge.length();

                DateViewSpannable spannable =
                        new DateViewSpannable(tvProfileNestedCommentContent.getContext(),
                                DateTransformator.getTimeStringForSimple(commentMessage.createTime),
                                (int) UiUtils.getPixelFromSp(10f));
                spannable.setTextColor(
                        JandiApplication.getContext().getResources().getColor(R.color.jandi_messages_date));
                builderWithBadge.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                    link.id, link.fromEntity, myId);

            if (unreadCount > 0) {
                NameSpannable unreadCountSpannable =
                        new NameSpannable(
                                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 9f, context.getResources().getDisplayMetrics())
                                , context.getResources().getColor(R.color.jandi_accent_color));
                int beforeLength = builderWithBadge.length();
                builderWithBadge.append(" ");
                builderWithBadge.append(String.valueOf(unreadCount))
                        .setSpan(unreadCountSpannable, beforeLength, builderWithBadge.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            tvProfileNestedCommentContent.setText(builderWithBadge, TextView.BufferType.SPANNABLE);
        }
    }

    private void settingFileInfo(ResMessages.Link link, long roomId) {
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity room = entityManager.getEntityById(roomId);

        boolean isPublicTopic = room.isPublicTopic();

        FormattedEntity feedbackEntityById =
                entityManager.getEntityById(link.feedback.writerId);

        tvFileUploaderName.setText(feedbackEntityById.getName());

        ResMessages.FileContent fileContent = link.feedback.content;

        String fileSize = FileUtil.fileSizeCalculation(fileContent.size);

        tvCommonFileSize.setText(fileSize);

        if (link.feedback instanceof ResMessages.FileMessage) {

            ResMessages.FileMessage feedbackFileMessage = link.feedback;

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

                ImageLoader.newBuilder()
                        .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                        .load(R.drawable.file_icon_deleted)
                        .into(ivMessageCommonFile);

                vFileIconBorder.setVisibility(View.GONE);
                ivMessageCommonFile.setOnClickListener(null);
            } else {
                final ResMessages.FileContent content = feedbackFileMessage.content;


                if (!isSharedFile) {
                    needFileUploaderDivider = false;
                    needFileSize = false;

                    tvMessageCommonFileName.setText(content.title);
                    tvFileUploaderName.setText(R.string.jandi_unshared_file);
                    tvMessageCommonFileName.setTextColor(resources.getColor(R.color.jandi_text_light));
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
                                ImageUtil.getThumbnailUrl(content.extraInfo, ImageUtil.Thumbnails.SMALL);
                        ImageUtil.setResourceIconOrLoadImageForComment(
                                ivMessageCommonFile, vFileIconBorder,
                                fileUrl, thumbnailUrl,
                                serverUrl, fileType);
                    }

                } else {
                    tvMessageCommonFileName.setText(content.title);
                    tvMessageCommonFileName.setTextColor(resources.getColor(R.color.dark_gray));
                    tvFileUploaderName.setTextColor(resources.getColor(R.color.dark_gray));

                    String serverUrl = content.serverUrl;
                    String fileType = content.icon;
                    String fileUrl = content.fileUrl;
                    String thumbnailUrl =
                            ImageUtil.getThumbnailUrl(content.extraInfo, ImageUtil.Thumbnails.SMALL);
                    ImageUtil.setResourceIconOrLoadImageForComment(
                            ivMessageCommonFile, vFileIconBorder,
                            fileUrl, thumbnailUrl,
                            serverUrl, fileType);

                    MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);
                    if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
                        ivMessageCommonFile.setOnClickListener(v -> {
                            String imageUrl =
                                    ImageUtil.getThumbnailUrlOrOriginal(
                                            content, ImageUtil.Thumbnails.ORIGINAL);
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

    private void setHasNestedProfile(boolean hasNestedProfile) {
        this.hasNestedProfile = hasNestedProfile;
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
        vgProfileNestedComment.setOnClickListener(itemClickListener);
    }

    @Override
    public void setOnItemLongClickListener(View.OnLongClickListener itemLongClickListener) {
        super.setOnItemLongClickListener(itemLongClickListener);
        vgProfileNestedComment.setOnLongClickListener(itemLongClickListener);
    }

    public void setHasOnlyBadge(boolean hasOnlyBadge) {
        this.hasOnlyBadge = hasOnlyBadge;
    }

    protected void setHasFlatTop(boolean hasFlatTop) {
        this.hasFlatTop = hasFlatTop;
    }

    public static class Builder extends BaseViewHolderBuilder {

        public CommentViewHolder build() {
            CommentViewHolder viewHolder = new CommentViewHolder();
            viewHolder.setHasBottomMargin(hasBottomMargin);
            viewHolder.setHasSemiDivider(hasSemiDivider);
            viewHolder.setHasFileInfoView(hasFileInfoView);
            viewHolder.setHasCommentBubbleTail(hasCommentBubbleTail);
            viewHolder.setHasNestedProfile(hasNestedProfile);
            viewHolder.setHasViewAllComment(hasViewAllComment);
            viewHolder.setHasOnlyBadge(hasOnlyBadge);
            viewHolder.setHasFlatTop(hasFlatTop);
            return viewHolder;
        }
    }

}
