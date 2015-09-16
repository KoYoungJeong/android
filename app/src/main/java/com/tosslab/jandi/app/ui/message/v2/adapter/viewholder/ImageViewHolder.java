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

import com.bumptech.glide.Glide;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FileSizeUtil;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class ImageViewHolder implements BodyViewHolder {

    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView dateTextView;
    private ImageView fileImageView;
    private TextView fileNameTextView;
    private TextView fileTypeTextView;
    private TextView tvUploader;
    private View disableCoverView;
    private View disableLineThroughView;
    private TextView unreadTextView;
    private Context context;
    private View lastReadView;
    private View contentView;

    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileImageView = (ImageView) rootView.findViewById(R.id.iv_message_photo);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_image_file_name);
        fileTypeTextView = (TextView) rootView.findViewById(R.id.txt_img_file_type);
        tvUploader = (TextView) rootView.findViewById(R.id.txt_img_file_uploader);
        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);

        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);
        context = rootView.getContext();
        lastReadView = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity =
                EntityManager.getInstance().getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        Ion.with(profileImageView)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != EntityManager.UNKNOWN_USER_ENTITY ? entityById.getUser() : null;
        if (user != null && TextUtils.equals(user.status, "enabled")) {
            nameTextView.setTextColor(context.getResources().getColor(R.color.jandi_messages_name));
            disableCoverView.setVisibility(View.GONE);
            disableLineThroughView.setVisibility(View.GONE);
        } else {
            nameTextView.setTextColor(
                    context.getResources().getColor(R.color.deactivate_text_color));
            disableCoverView.setVisibility(View.VISIBLE);
            disableLineThroughView.setVisibility(View.VISIBLE);
        }

        int unreadCount = UnreadCountUtil.getUnreadCount(
                teamId, roomId, link.id, fromEntityId, entityManager.getMe().getId());

        unreadTextView.setText(String.valueOf(unreadCount));
        if (unreadCount <= 0) {
            unreadTextView.setVisibility(View.GONE);
        } else {
            unreadTextView.setVisibility(View.VISIBLE);
        }

        nameTextView.setText(fromEntity.name);
        dateTextView.setText(DateTransformator.getTimeStringForSimple(link.time));

        if (link.message instanceof ResMessages.FileMessage) {
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) link.message;

            ResMessages.FileContent fileContent = fileMessage.content;
            MimeTypeUtil.SourceType sourceType =
                    SourceTypeUtil.getSourceType(fileContent.serverUrl);

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

            if (TextUtils.equals(fileMessage.status, "archived")) {

                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileImageView.setImageResource(R.drawable.jandi_fview_icon_deleted);
                fileImageView.setClickable(false);
                fileTypeTextView.setText("");
            } else {
                if (BitmapUtil.hasImageUrl(fileContent)) {
                    // Google, Dropbox 파일이 인 경우
                    if (sourceType == MimeTypeUtil.SourceType.Google
                            || sourceType == MimeTypeUtil.SourceType.Dropbox) {
                        int mimeTypeIconImage =
                                MimeTypeUtil.getMimeTypeIconImage(
                                        fileContent.serverUrl, fileContent.icon);
                        fileImageView.setImageResource(mimeTypeIconImage);
                        fileTypeTextView.setText(fileContent.ext);
                    } else {

                        // small 은 80 x 80 사이즈가 로딩됨 -> medium 으로 로딩

                        String localFilePath = BitmapUtil.getLocalFilePath(fileMessage.id);


                        String thumbPath;
                        if (!TextUtils.isEmpty(localFilePath)) {
                            thumbPath = localFilePath;
                        } else {
                            thumbPath = BitmapUtil.getThumbnailUrlOrOriginal(
                                    fileContent, BitmapUtil.Thumbnails.LARGE);
                        }

                        Glide.with(fileImageView.getContext())
                                .load(thumbPath)
                                .placeholder(R.drawable.file_icon_img)
                                .error(R.drawable.file_icon_img)
                                .crossFade()
                                .centerCrop()
                                .into(fileImageView);
                        fileTypeTextView.setText(FileSizeUtil.fileSizeCalculation(fileContent.size) + ", "
                                + fileContent.ext);
                    }
                } else {
                    fileImageView.setImageResource(R.drawable.file_icon_img);
                }

                fileNameTextView.setText(fileContent.title);
            }

        }
        profileImageView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
        nameTextView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
    }

    @Override
    public void setLastReadViewVisible(int currentLinkId, int lastReadLinkId) {
        if (currentLinkId == lastReadLinkId) {
            lastReadView.setVisibility(View.VISIBLE);
        } else {
            lastReadView.setVisibility(View.GONE);
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
