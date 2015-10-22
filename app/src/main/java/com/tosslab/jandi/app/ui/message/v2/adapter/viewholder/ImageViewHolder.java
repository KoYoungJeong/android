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
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FileSizeUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.NameSpannable;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 21..
 */
public class ImageViewHolder implements BodyViewHolder {

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

        ivFileImage = (ImageView) rootView.findViewById(R.id.iv_message_photo);
        tvFileName = (TextView) rootView.findViewById(R.id.tv_message_image_file_name);
        tvFileType = (TextView) rootView.findViewById(R.id.tv_img_file_type);
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

        FormattedEntity entity =
                EntityManager.getInstance().getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        BitmapUtil.loadCropCircleImageByGlideBitmap(ivProfile,
                profileUrl,
                R.drawable.profile_img,
                R.drawable.profile_img);

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != EntityManager.UNKNOWN_USER_ENTITY ? entityById.getUser() : null;
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

                tvFileName.setText(R.string.jandi_deleted_file);
                ivFileImage.setImageResource(R.drawable.jandi_fview_icon_deleted);
                ivFileImage.setClickable(false);
                tvFileType.setText("");
            } else {
                if (BitmapUtil.hasImageUrl(fileContent)) {
                    // Google, Dropbox 파일이 인 경우
                    if (sourceType == MimeTypeUtil.SourceType.Google
                            || sourceType == MimeTypeUtil.SourceType.Dropbox) {
                        int mimeTypeIconImage =
                                MimeTypeUtil.getMimeTypeIconImage(
                                        fileContent.serverUrl, fileContent.icon);
                        ivFileImage.setImageResource(mimeTypeIconImage);
                        tvFileType.setText(fileContent.ext);
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

                        BitmapUtil.loadCropBitmapByGlide(ivFileImage,
                                thumbPath,
                                R.drawable.file_icon_img,
                                R.drawable.file_icon_img);

                        String fileSize = FileSizeUtil.fileSizeCalculation(fileContent.size);
                        tvFileType.setText(String.format("%s, %s", fileSize, fileContent.ext));
                    }
                } else {
                    ivFileImage.setImageResource(R.drawable.file_icon_img);
                }

                tvFileName.setText(fileContent.title);
            }

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
