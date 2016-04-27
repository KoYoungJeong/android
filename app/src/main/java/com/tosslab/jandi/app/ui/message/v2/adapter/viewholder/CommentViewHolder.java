package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.spannable.analysis.mention.MentionAnalysisInfo;
import com.tosslab.jandi.app.ui.message.v2.adapter.viewholder.builder.BaseViewHolderBuilder;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.DateViewSpannable;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import java.util.ArrayList;
import java.util.Collection;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 4. 7..
 */
public class CommentViewHolder extends BaseCommentViewHolder {

    private SimpleDraweeView ivMessageCommonFile;
    private TextView tvFileUploaderName;
    private TextView tvCommonFileSize;
    private TextView tvMessageBadge;
    private TextView tvMessageTime;
    private SimpleDraweeView ivProfileNestedCommentUserProfile;
    private ViewGroup vgProfileNestedCommentUserName;
    private TextView tvProfileNestedCommentUserName;
    private ImageView ivProfileNestedNameLineThrough;
    private TextView tvProfileNestedCommentContent;
    private TextView tvMessageCommonFileName;
    private ViewGroup vgMessageCommonFile;
    private Context context;

    private boolean hasNestedProfile = false;
    private boolean hasOnlyBadge;


    private CommentViewHolder() {
    }

    @Override
    public void initView(View rootView) {
        super.initView(rootView);
        // 파일 정보
        vgMessageCommonFile = (ViewGroup) rootView.findViewById(R.id.vg_message_common_file);
        ivMessageCommonFile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_common_file);
        tvMessageCommonFileName = (TextView) rootView.findViewById(R.id.tv_message_common_file_name);
        tvFileUploaderName = (TextView) rootView.findViewById(R.id.tv_uploader_name);
        tvCommonFileSize = (TextView) rootView.findViewById(R.id.tv_common_file_size);
        tvMessageBadge = (TextView) rootView.findViewById(R.id.tv_message_badge);
        tvMessageTime = (TextView) rootView.findViewById(R.id.tv_message_time);
        tvMessageBadge.setVisibility(View.GONE);
        tvMessageTime.setVisibility(View.GONE);

        // 프로필이 있는 커멘트
        ivProfileNestedCommentUserProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_profile_nested_comment_user_profile);
        vgProfileNestedCommentUserName = (ViewGroup) rootView.findViewById(R.id.vg_profile_nested_comment_user_name);
        tvProfileNestedCommentUserName = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_user_name);
        ivProfileNestedNameLineThrough = (ImageView) rootView.findViewById(R.id.iv_profile_nested_name_line_through);

        tvProfileNestedCommentContent = (TextView) rootView.findViewById(R.id.tv_profile_nested_comment_content);

        context = rootView.getContext();

        if (hasNestedProfile) {
            ivProfileNestedCommentUserProfile.setVisibility(View.VISIBLE);
            vgProfileNestedCommentUserName.setVisibility(View.VISIBLE);
        } else {
            ivProfileNestedCommentUserProfile.setVisibility(View.INVISIBLE);
            vgProfileNestedCommentUserName.setVisibility(View.GONE);
        }

    }

    @Override
    protected void initObjects() {
        vgProfileNestedComment.setVisibility(View.VISIBLE);
        vgProfileNestedCommentSticker.setVisibility(View.GONE);
    }

    @Override
    public void bindData(ResMessages.Link link, long teamId, long roomId, long entityId) {
        super.bindData(link, teamId, roomId, entityId);

        if (hasFileInfoView()) {
            settingFileInfo(link, roomId);
        }

        setCommentUserInfo(link);

        setCommentMessage(link, teamId, roomId);
    }

    private void setCommentMessage(ResMessages.Link link, long teamId, long roomId) {
        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");
            builder.append(" ");

            long myId = EntityManager.getInstance().getMe().getId();
            MentionAnalysisInfo mentionAnalysisInfo =
                    MentionAnalysisInfo.newBuilder(myId, commentMessage.mentions)
                            .textSizeFromResource(R.dimen.jandi_mention_message_view_comment_item_font_size)
                            .build();

            SpannableLookUp.text(builder)
                    .hyperLink(false)
                    .markdown(false)
                    .webLink(false)
                    .emailLink(false)
                    .telLink(false)
                    .mention(mentionAnalysisInfo, false)
                    .lookUp(tvProfileNestedCommentContent.getContext());

            LinkifyUtil.addLinks(context, builder);

            if (!hasOnlyBadge) {
                int startIndex = builder.length();
                builder.append(DateTransformator.getTimeStringForSimple(commentMessage.createTime));
                int endIndex = builder.length();

                DateViewSpannable spannable =
                        new DateViewSpannable(tvProfileNestedCommentContent.getContext(),
                                DateTransformator.getTimeStringForSimple(commentMessage.createTime));
                spannable.setTextColor(
                        JandiApplication.getContext().getResources().getColor(R.color.jandi_messages_date));
                builder.setSpan(spannable, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                    link.id, link.fromEntity, myId);

            if (unreadCount > 0) {
                NameSpannable unreadCountSpannable =
                        new NameSpannable(
                                context.getResources().getDimensionPixelSize(R.dimen.jandi_comment_text_size)
                                , context.getResources().getColor(R.color.jandi_accent_color));
                int beforeLength = builder.length();
                builder.append(" ");
                builder.append(String.valueOf(unreadCount))
                        .setSpan(unreadCountSpannable, beforeLength, builder.length(),
                                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }

            tvProfileNestedCommentContent.setText(builder);
        }
    }

    private void setCommentUserInfo(ResMessages.Link link) {
        long fromEntityId = link.fromEntity;
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        ImageUtil.loadProfileImage(
                ivProfileNestedCommentUserProfile, profileUrl, R.drawable.profile_img);

        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != EntityManager.UNKNOWN_USER_ENTITY ? entityById.getUser() : null;

        if (user != null && entityById.isEnabled()) {
            tvProfileNestedCommentUserName.setTextColor(
                    JandiApplication.getContext().getResources().getColor(R.color.jandi_messages_name));
            ivProfileNestedNameLineThrough.setVisibility(View.GONE);
        } else {
            tvProfileNestedCommentUserName.setTextColor(
                    JandiApplication.getContext().getResources().getColor(R.color.deactivate_text_color));
            ivProfileNestedNameLineThrough.setVisibility(View.VISIBLE);
        }

        tvProfileNestedCommentUserName.setText(fromEntity.name);

        ivProfileNestedCommentUserProfile.setOnClickListener(
                v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));

        tvProfileNestedCommentUserName.setOnClickListener(
                v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
    }

    private void settingFileInfo(ResMessages.Link link, long roomId) {
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity room = entityManager.getEntityById(roomId);

        boolean isPublicTopic = room.isPublicTopic();

        FormattedEntity feedbackEntityById =
                entityManager.getEntityById(link.feedback.writerId);
        ResLeftSideMenu.User feedbackUser =
                feedbackEntityById != EntityManager.UNKNOWN_USER_ENTITY ? feedbackEntityById.getUser() : null;

        tvFileUploaderName.setText(feedbackUser.name);

        ResMessages.FileContent fileContent = link.feedback.content;

        String fileSize = FileUtil.fileSizeCalculation(fileContent.size);

        tvCommonFileSize.setText(fileSize);

        tvMessageTime.setText(DateTransformator.getTimeStringForSimple(link.time));

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

            tvMessageCommonFileName.setTypeface(null, Typeface.BOLD);

            final Resources resources = tvMessageCommonFileName.getResources();
            if (TextUtils.equals(link.feedback.status, "archived")) {
                tvMessageCommonFileName.setText(R.string.jandi_deleted_file);
                tvMessageCommonFileName.setTextColor(resources.getColor(R.color.jandi_text_light));

                ImageLoader.newBuilder()
                        .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                        .load(R.drawable.file_icon_deleted)
                        .into(ivMessageCommonFile);

                ivMessageCommonFile.setOnClickListener(null);
            } else {
                final ResMessages.FileContent content = feedbackFileMessage.content;

                if (!isSharedFile) {
                    tvMessageCommonFileName.setTypeface(null, Typeface.NORMAL);
                    int testSizePx = resources.getDimensionPixelSize(R.dimen.jandi_text_size_11sp);
                    tvMessageCommonFileName.setTextSize(TypedValue.COMPLEX_UNIT_PX, testSizePx);
                    SpannableStringBuilder unshareTextBuilder = new SpannableStringBuilder();
                    String title = content.title;
                    if (content.title.length() > 15) {
                        unshareTextBuilder.append(title.substring(0, 14))
                                .append("...");
                    } else {
                        unshareTextBuilder.append(title).append(" ");
                    }

                    unshareTextBuilder.setSpan(
                            new StyleSpan(Typeface.BOLD),
                            0, unshareTextBuilder.length(),
                            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                    unshareTextBuilder.append(resources.getString(R.string.jandi_unshared_file));
                    tvMessageCommonFileName.setText(unshareTextBuilder);
                    tvMessageCommonFileName.setTextSize(TypedValue.COMPLEX_UNIT_PX, testSizePx);
                    tvMessageCommonFileName.setTextColor(resources.getColor(R.color.jandi_text_light));

                    ivMessageCommonFile.setClickable(false);

                    int resId = R.drawable.file_icon_unshared;
                    if (isPublicTopic) {
                        resId = MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon);
                    }

                    ImageLoader.newBuilder()
                            .actualScaleType(ScalingUtils.ScaleType.FIT_CENTER)
                            .load(resId)
                            .into(ivMessageCommonFile);
                } else {
                    tvMessageCommonFileName.setText(content.title);
                    tvMessageCommonFileName.setTextColor(resources.getColor(R.color.jandi_messages_file_name));

                    String serverUrl = content.serverUrl;
                    String fileType = content.icon;
                    String fileUrl = content.fileUrl;
                    String thumbnailUrl =
                            ImageUtil.getThumbnailUrl(content.extraInfo, ImageUtil.Thumbnails.SMALL);
                    ImageUtil.setResourceIconOrLoadImage(
                            ivMessageCommonFile, null,
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
        }
    }

    private void setHasNestedProfile(boolean hasNestedProfile) {
        this.hasNestedProfile = hasNestedProfile;
    }

    @Override
    public void setOnItemClickListener(View.OnClickListener itemClickListener) {
        super.setOnItemClickListener(itemClickListener);
        vgMessageCommonFile.setOnClickListener(itemClickListener);
        vgReadMore.setOnClickListener(itemClickListener);
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
            return viewHolder;
        }
    }

}
