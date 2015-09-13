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

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestUserInfoEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.ResMessages;
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
public class FileViewHolder implements BodyViewHolder {

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

    private FileViewHolder() {
    }

    public static FileViewHolder createFileViewHolder() {
        return new FileViewHolder();
    }


    @Override
    public void initView(View rootView) {
        contentView = rootView.findViewById(R.id.vg_message_item);
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_common_file);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_common_file_name);
        fileTypeTextView = (TextView) rootView.findViewById(R.id.txt_common_file_type);
        tvUploader = (TextView) rootView.findViewById(R.id.txt_img_file_uploader);

        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.iv_entity_listitem_line_through);

        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);
        context = rootView.getContext();
        lastReadView = rootView.findViewById(R.id.vg_message_last_read);
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId, int entityId) {

        int fromEntityId = link.fromEntity;

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity entity = entityManager.getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        Ion.with(profileImageView)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        if (TextUtils.equals(fromEntity.status, "enabled")) {
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

            if (TextUtils.equals(link.message.status, "archived")) {
                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileImageView.setImageResource(R.drawable.jandi_fl_icon_deleted);
                fileTypeTextView.setVisibility(View.GONE);
                fileNameTextView.setTextColor(fileNameTextView.getResources().getColor(R.color
                        .jandi_text_light));
            } else {
                fileNameTextView.setTextColor(fileNameTextView.getResources().getColor(R.color.jandi_messages_file_name));
                fileNameTextView.setText(fileMessage.content.title);
                MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileMessage.content.serverUrl);
                switch (sourceType) {
                    case S3:
                        fileTypeTextView.setText(FileSizeUtil.fileSizeCalculation(fileMessage.content.size)
                                + ", " + fileMessage.content.ext);
                        break;
                    case Google:
                    case Dropbox:
                        fileTypeTextView.setText(fileMessage.content.ext);
                        break;
                }

                int mimeTypeIconImage =
                        MimeTypeUtil.getMimeTypeIconImage(
                                fileMessage.content.serverUrl, fileMessage.content.icon);
                fileImageView.setImageResource(mimeTypeIconImage);
                fileTypeTextView.setVisibility(View.VISIBLE);
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
        return R.layout.item_message_file_v2;

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
