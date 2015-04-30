package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Intent;
import android.net.Uri;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.ui.photo.PhotoViewActivity_;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class FileCommentViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView dateTextView;
    private TextView fileOwnerTextView;
    private TextView fileNameTextView;
    private TextView commentTextView;
    private TextView fileOwnerPostfixTextView;
    private ImageView fileImageView;
    private View disableCoverView;
    private View disableLineThroughView;
    private TextView unreadTextView;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileOwnerTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_owner);
        fileOwnerPostfixTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_postfix);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_file_name);
        commentTextView = (TextView) rootView.findViewById(R.id.txt_message_commented_content);

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_commented_photo);

        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);

        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);

    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity = EntityManager.getInstance(nameTextView.getContext()).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = ((fromEntity.u_photoThumbnailUrl != null) && TextUtils.isEmpty(fromEntity.u_photoThumbnailUrl.largeThumbnailUrl)) ? fromEntity.u_photoThumbnailUrl.largeThumbnailUrl : fromEntity.u_photoUrl;

        EntityManager entityManager = EntityManager.getInstance(profileImageView.getContext());
        if (TextUtils.equals(entityManager.getEntityById(fromEntity.id).getUser().status, "enabled")) {
            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.jandi_messages_name));

            disableCoverView.setVisibility(View.GONE);
            disableLineThroughView.setVisibility(View.GONE);
        } else {
            nameTextView.setTextColor(nameTextView.getResources().getColor(R.color.deactivate_text_color));

            disableCoverView.setVisibility(View.VISIBLE);
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        int unreadCount = UnreadCountUtil.getUnreadCount(unreadTextView.getContext(), teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        unreadTextView.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            unreadTextView.setVisibility(View.GONE);
        } else {
            unreadTextView.setVisibility(View.VISIBLE);
        }


        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(JandiConstantsForFlavors.SERVICE_ROOT_URL + profileUrl);
        nameTextView.setText(fromEntity.name);

        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (link.feedback instanceof ResMessages.FileMessage) {

            ResMessages.FileMessage feedbackFileMessage = (ResMessages.FileMessage) link.feedback;
            if (TextUtils.equals(link.feedback.status, "archived")) {
                fileOwnerTextView.setVisibility(View.INVISIBLE);
                fileOwnerPostfixTextView.setVisibility(View.INVISIBLE);

                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileImageView.setVisibility(View.VISIBLE);
            } else {
                fileOwnerTextView.setText(feedbackFileMessage.writer.name);
                fileNameTextView.setText(feedbackFileMessage.content.title);

                fileOwnerTextView.setVisibility(View.VISIBLE);
                fileOwnerPostfixTextView.setVisibility(View.VISIBLE);

                String fileType = feedbackFileMessage.content.type;
                if (fileType.startsWith("image/")) {
                    String imageUrl = null;
                    if (feedbackFileMessage.content.extraInfo != null &&
                            !TextUtils.isEmpty(feedbackFileMessage.content.extraInfo.smallThumbnailUrl)) {

                        imageUrl = BitmapUtil.getFileeUrl(feedbackFileMessage.content.extraInfo.smallThumbnailUrl);

                    } else if (!TextUtils.isEmpty(feedbackFileMessage.content.fileUrl)) {
                        imageUrl = BitmapUtil.getFileeUrl(feedbackFileMessage.content.fileUrl);
                    }
                    if (!TextUtils.isEmpty(imageUrl)) {

                        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(feedbackFileMessage.content.serverUrl);

                        switch (sourceType) {
                            case Google:
                            case Dropbox:
                                fileImageView.setImageResource(MimeTypeUtil.getMimeTypeIconImage(feedbackFileMessage.content.serverUrl, feedbackFileMessage.content.icon));
                                fileImageView.setOnClickListener(view -> fileImageView.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(BitmapUtil.getFileeUrl(feedbackFileMessage.content.fileUrl)))));
                                break;
                            default:
                                Ion.with(fileImageView)
                                        .placeholder(R.drawable.jandi_fl_icon_img)
                                        .error(R.drawable.jandi_fl_icon_img)
                                        .crossfade(true)
                                        .fitCenter()
                                        .load(imageUrl);
                                fileImageView.setOnClickListener(view -> PhotoViewActivity_
                                        .intent(fileImageView.getContext())
                                        .imageUrl(BitmapUtil.getFileeUrl(feedbackFileMessage.content.fileUrl))
                                        .imageName(feedbackFileMessage.content.name)
                                        .imageType(feedbackFileMessage.content.type)
                                        .start());
                                break;
                        }

                    } else {
                        fileImageView.setImageResource(MimeTypeUtil.getMimeTypeIconImage(feedbackFileMessage.content.serverUrl, feedbackFileMessage.content.icon));
                    }
                } else {
                    fileImageView.setImageResource(MimeTypeUtil.getMimeTypeIconImage(feedbackFileMessage.content.serverUrl, feedbackFileMessage.content.icon));
                }
            }

        }

        if (link.message instanceof ResMessages.CommentMessage) {
            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) link.message;

            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            spannableStringBuilder.append(commentMessage.content.body);

            boolean hasLink = LinkifyUtil.addLinks(commentTextView.getContext(), spannableStringBuilder);

            if (hasLink) {
                commentTextView.setText(Spannable.Factory.getInstance().newSpannable(spannableStringBuilder));
                LinkifyUtil.setOnLinkClick(commentTextView);
            } else {
                commentTextView.setText(spannableStringBuilder);
            }
        }

        profileImageView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
        nameTextView.setOnClickListener(v -> EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_cmt_with_file_v2;
    }

}
