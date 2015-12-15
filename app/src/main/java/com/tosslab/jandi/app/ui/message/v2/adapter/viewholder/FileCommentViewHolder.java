package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.GenerateMentionMessageUtil;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import java.util.ArrayList;
import java.util.Collection;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class FileCommentViewHolder implements BodyViewHolder {

    private SimpleDraweeView ivProfile;
    private TextView tvName;
    private TextView tvFileOwner;
    private TextView tvFileName;
    private TextView tvComment;
    private SimpleDraweeView ivFileImage;
    private View vDisableCover;
    private View vDisableLineThrough;
    private Context context;
    private View vLastRead;
    private View vFileImageRound;
    private View contentView;
    private TextView tvDate;
    private TextView tvUnread;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        ivProfile = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_user_profile);
        tvName = (TextView) rootView.findViewById(R.id.tv_message_user_name);
        tvDate = (TextView) rootView.findViewById(R.id.tv_message_create_date);
        tvUnread = ((TextView) rootView.findViewById(R.id.tv_entity_listitem_unread));

        tvFileOwner = (TextView) rootView.findViewById(R.id.tv_message_commented_owner);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_commented_file_name);
        tvComment = (TextView) rootView.findViewById(R.id.tv_message_commented_content);

        ivFileImage = (SimpleDraweeView) rootView.findViewById(R.id.iv_message_commented_photo);
        vFileImageRound = rootView.findViewById(R.id.iv_message_commented_photo_round);

        vDisableCover = rootView.findViewById(R.id.v_entity_listitem_warning);
        vDisableLineThrough = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        context = rootView.getContext();
        vLastRead = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        int fromEntityId = link.fromEntity;
        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        FormattedEntity room = entityManager.getEntityById(roomId);

        boolean isPublicTopic = room.isPublicTopic();

        ImageUtil.loadCircleImageByFresco(ivProfile, profileUrl, R.drawable.profile_img);

        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != EntityManager.UNKNOWN_USER_ENTITY ? entityById.getUser() : null;

        FormattedEntity feedbackEntityById = entityManager.getEntityById(link.feedback.writerId);
        ResLeftSideMenu.User feedbackUser = feedbackEntityById != EntityManager.UNKNOWN_USER_ENTITY ? feedbackEntityById.getUser() : null;

        if (user != null && TextUtils.equals(user.status, "enabled")) {
            tvName.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            vDisableCover.setVisibility(View.GONE);
            vDisableLineThrough.setVisibility(View.GONE);
        } else {
            tvName.setTextColor(
                    context.getResources().getColor(R.color.deactivate_text_color));
            vDisableCover.setVisibility(View.VISIBLE);
            vDisableLineThrough.setVisibility(View.VISIBLE);
        }

        tvName.setText(fromEntity.name);

        tvDate.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (link.feedback instanceof ResMessages.FileMessage) {

            ResMessages.FileMessage feedbackFileMessage = link.feedback;

            boolean isSharedFile = false;

            Collection<ResMessages.OriginalMessage.IntegerWrapper> shareEntities = feedbackFileMessage.shareEntities;

            // ArrayList로 나오는 경우 아직 DB에 기록되지 않은 경우 - object가 자동갱신되지 않는 문제 해결
            if (shareEntities instanceof ArrayList) {
                ResMessages.FileMessage file = MessageRepository.getRepository().getFileMessage(feedbackFileMessage.id);
                shareEntities = file.shareEntities;
            }

            for (ResMessages.OriginalMessage.IntegerWrapper e : shareEntities) {
                if (e.getShareEntity() == roomId) {
                    isSharedFile = true;
                }
            }

            ivFileImage.setScaleType(ImageView.ScaleType.FIT_CENTER);
            vFileImageRound.setVisibility(View.GONE);
            tvFileName.setTypeface(null, Typeface.BOLD);

            if (TextUtils.equals(link.feedback.status, "archived")) {
                tvFileOwner.setVisibility(View.GONE);
                tvFileName.setText(R.string.jandi_deleted_file);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_text_light));
                ivFileImage.setImageURI(UriFactory.getResourceUri(R.drawable.jandi_fl_icon_deleted));
                ivFileImage.setOnClickListener(null);
            } else if (!isSharedFile) {
                tvFileName.setTypeface(null, Typeface.NORMAL);
                int TextSizePX = tvFileName.getResources().getDimensionPixelSize(R.dimen.jandi_text_size_11sp);
                tvFileOwner.setVisibility(View.VISIBLE);
                tvFileOwner.setText(Html.fromHtml(tvFileOwner.getResources().getString(R.string.jandi_commented_on, feedbackUser.name)));
                tvFileOwner.setTextSize(TypedValue.COMPLEX_UNIT_PX, TextSizePX);
                ResMessages.FileContent content = feedbackFileMessage.content;
                SpannableStringBuilder unshareTextBuilder = new SpannableStringBuilder();
                String title = content.title;
                if (content.title.length() > 15) {
                    unshareTextBuilder.append(title.substring(0, 14))
                            .append("...");
                } else {
                    unshareTextBuilder.append(title).append(" ");
                }

                unshareTextBuilder.setSpan(new StyleSpan(Typeface.BOLD), 0, unshareTextBuilder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

                unshareTextBuilder.append(context.getResources().getString(R.string.jandi_unshared_file));
                tvFileName.setText(unshareTextBuilder);
                tvFileName.setTextSize(TypedValue.COMPLEX_UNIT_PX, TextSizePX);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color.jandi_text_light));

                if (isPublicTopic) {
                    int mimeTypeIconImage =
                            MimeTypeUtil.getMimeTypeIconImage(
                                    feedbackFileMessage.content.serverUrl, feedbackFileMessage.content.icon);
                    ivFileImage.setImageURI(UriFactory.getResourceUri(mimeTypeIconImage));
                } else {
                    ivFileImage.setImageURI(UriFactory.getResourceUri(R.drawable.file_icon_unshared_141));
                }
                ivFileImage.setClickable(false);
            } else {
                tvFileOwner.setText(Html.fromHtml(tvFileOwner.getResources().getString(R.string.jandi_commented_on, feedbackUser.name)));
                ResMessages.FileContent content = feedbackFileMessage.content;
                tvFileName.setText(content.title);
                tvFileName.setTextColor(tvFileName.getResources().getColor(R.color
                        .jandi_messages_file_name));

                tvFileOwner.setVisibility(View.VISIBLE);

                String fileType = content.icon;
                GenericDraweeHierarchy hierarchy = ivFileImage.getHierarchy();
                if (TextUtils.equals(fileType, "image")) {
                    if (ImageUtil.hasImageUrl(content)) {
                        String thumbnailUrl = ImageUtil.getThumbnailUrlOrOriginal(
                                content, ImageUtil.Thumbnails.SMALL);
                        MimeTypeUtil.SourceType sourceType =
                                SourceTypeUtil.getSourceType(content.serverUrl);
                        switch (sourceType) {
                            case Google:
                            case Dropbox:
                                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
                                ivFileImage.setHierarchy(hierarchy);
                                int mimeTypeIconImage =
                                        MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon);
                                ivFileImage.setImageURI(UriFactory.getResourceUri(mimeTypeIconImage));
                                ivFileImage.setOnClickListener(view -> {
                                    Intent intent = new Intent(Intent.ACTION_VIEW,
                                            Uri.parse(
                                                    ImageUtil.getThumbnailUrlOrOriginal(
                                                            content, ImageUtil.Thumbnails.ORIGINAL)));
                                    context.startActivity(intent);
                                });
                                break;
                            default:
                                vFileImageRound.setVisibility(View.VISIBLE);
                                Resources resources = context.getResources();
                                Drawable placeHolder = resources.getDrawable(R.drawable.file_icon_img);
                                hierarchy.setPlaceholderImage(placeHolder, ScalingUtils.ScaleType.FIT_XY);
                                Drawable failure = resources.getDrawable(R.drawable.image_no_preview);
                                hierarchy.setFailureImage(failure, ScalingUtils.ScaleType.FIT_XY);
                                hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.CENTER_CROP);
                                ivFileImage.setHierarchy(hierarchy);
                                loadImage(thumbnailUrl);
                                break;
                        }
                    } else {
                        hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
                        ivFileImage.setHierarchy(hierarchy);
                        int mimeTypeIconImage =
                                MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon);
                        ivFileImage.setImageURI(UriFactory.getResourceUri(mimeTypeIconImage));
                    }
                } else {
                    hierarchy.setActualImageScaleType(ScalingUtils.ScaleType.FIT_XY);
                    ivFileImage.setHierarchy(hierarchy);
                    int mimeTypeIconImage =
                            MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, content.icon);
                    ivFileImage.setImageURI(UriFactory.getResourceUri(mimeTypeIconImage));
                }
            }

        }

        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;

            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append(!TextUtils.isEmpty(commentMessage.content.body) ? commentMessage.content.body : "");
            builder.append(" ");

            boolean hasLink = LinkifyUtil.addLinks(context, builder);

            int unreadCount = UnreadCountUtil.getUnreadCount(teamId, roomId,
                    link.id, link.fromEntity, EntityManager.getInstance().getMe().getId());

            tvUnread.setText(String.valueOf(unreadCount));

            if (unreadCount > 0) {
                tvUnread.setVisibility(View.VISIBLE);
            } else {
                tvUnread.setVisibility(View.GONE);
            }


            GenerateMentionMessageUtil generateMentionMessageUtil = new GenerateMentionMessageUtil(
                    tvComment, builder, commentMessage.mentions, entityManager.getMe().getId())
                    .setPxSize(R.dimen.jandi_mention_comment_item_font_size);
            builder = generateMentionMessageUtil.generate(true);


            if (hasLink) {
                tvComment.setText(
                        Spannable.Factory.getInstance().newSpannable(builder));

                LinkifyUtil.setOnLinkClick(tvComment);
            } else {
                tvComment.setText(builder);
            }

        }

        ivProfile.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Image)));
        tvName.setOnClickListener(v -> EventBus.getDefault().post(new ShowProfileEvent(fromEntity.id, ShowProfileEvent.From.Name)));
    }

    private void loadImage(String thumbnailUrl) {
        ViewGroup.LayoutParams layoutParams = ivFileImage.getLayoutParams();

        ImageRequest request = ImageRequestBuilder.newBuilderWithSource(Uri.parse(thumbnailUrl))
            .setResizeOptions(new ResizeOptions(layoutParams.width, layoutParams.height))
            .build();

        DraweeController controller = Fresco.newDraweeControllerBuilder()
                .setImageRequest(request)
                .setOldController(ivFileImage.getController())
                .build();

        ivFileImage.setController(controller);
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
        return R.layout.item_message_cmt_with_file_v2;
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
