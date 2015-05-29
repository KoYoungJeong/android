package com.tosslab.jandi.app.ui.message.v2.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
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
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

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
    private View disableCoverView;
    private View disableLineThroughView;
    private TextView unreadTextView;
    private Context context;

    @Override
    public void initView(View rootView) {
        profileImageView = (ImageView) rootView.findViewById(R.id.img_message_user_profile);
        nameTextView = (TextView) rootView.findViewById(R.id.txt_message_user_name);
        dateTextView = (TextView) rootView.findViewById(R.id.txt_message_create_date);

        fileImageView = (ImageView) rootView.findViewById(R.id.img_message_photo);
        fileNameTextView = (TextView) rootView.findViewById(R.id.txt_message_image_file_name);
        fileTypeTextView = (TextView) rootView.findViewById(R.id.txt_img_file_type);
        disableCoverView = rootView.findViewById(R.id.view_entity_listitem_warning);
        disableLineThroughView = rootView.findViewById(R.id.img_entity_listitem_line_through);

        unreadTextView = (TextView) rootView.findViewById(R.id.txt_entity_listitem_unread);
        context = rootView.getContext();
    }

    @Override
    public void bindData(ResMessages.Link link, int teamId, int roomId) {

        int fromEntityId = link.fromEntity;

        FormattedEntity entity =
                EntityManager.getInstance(context).getEntityById(fromEntityId);
        ResLeftSideMenu.User fromEntity = entity.getUser();

        String profileUrl = entity.getUserLargeProfileUrl();

        LogUtil.e("profileUrl - " + profileUrl);

        Ion.with(profileImageView)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity entityById = entityManager.getEntityById(fromEntity.id);
        ResLeftSideMenu.User user = entityById != null ? entityById.getUser() : null;
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

        int unreadCount = UnreadCountUtil.getUnreadCount(context,
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

            if (TextUtils.equals(fileMessage.status, "archived")) {

                fileNameTextView.setText(R.string.jandi_deleted_file);
                fileImageView.setImageResource(R.drawable.jandi_fview_icon_deleted);
                fileImageView.setOnClickListener(null);
            } else {
                ResMessages.ThumbnailUrls extraInfo = fileContent.extraInfo;
                String smallThumbnailUrl = extraInfo != null ? extraInfo.smallThumbnailUrl : null;
                String mediumThumbnailUrl = extraInfo != null ? extraInfo.mediumThumbnailUrl : null;
                String largeThumbnailUrl = extraInfo != null ? extraInfo.largeThumbnailUrl : null;
                String originalFileUrl = fileContent.fileUrl;

                if (hasImageUrl(smallThumbnailUrl, largeThumbnailUrl, originalFileUrl)) {
                    // Google, Dropbox 파일이 인 경우
                    if (sourceType == MimeTypeUtil.SourceType.Google
                            || sourceType == MimeTypeUtil.SourceType.Dropbox) {
                        int mimeTypeIconImage =
                                MimeTypeUtil.getMimeTypeIconImage(
                                        fileContent.serverUrl, fileContent.icon);
                        fileImageView.setImageResource(mimeTypeIconImage);
                        fileImageView.setOnClickListener(view -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW,
                                    Uri.parse(BitmapUtil.getFileUrl(originalFileUrl)));
                            fileImageView.getContext().startActivity(intent);
                        });
                    } else {
                        String optimizedImageUrl =
                                getOptimizedImageUrl(context, smallThumbnailUrl, mediumThumbnailUrl,
                                        largeThumbnailUrl, originalFileUrl);

                        String imageUrl = BitmapUtil.getFileUrl(optimizedImageUrl);
                        Log.d("JANDI", "imageUrl - " + imageUrl);

                        fileImageView.setOnClickListener(view -> PhotoViewActivity_
                                .intent(fileImageView.getContext())
                                .imageUrl(imageUrl)
                                .imageName(fileContent.name)
                                .imageType(fileContent.type)
                                .start());

                        // small 은 80 x 80 사이즈가 로딩됨 -> medium 으로 로딩
                        String mediumThumb = !TextUtils.isEmpty(mediumThumbnailUrl)
                                ? BitmapUtil.getFileUrl(mediumThumbnailUrl) : imageUrl;

                        Log.d("JANDI", "small thumb - " + mediumThumb);
                        Ion.with(fileImageView)
                                .placeholder(R.drawable.jandi_fl_icon_img)
                                .error(R.drawable.jandi_fl_icon_img)
                                .crossfade(true)
                                .fitCenter()
                                .load(mediumThumb);
                    }
                } else {
                    fileImageView.setImageResource(R.drawable.jandi_fl_icon_img);
                }

                fileNameTextView.setText(fileContent.title);
                fileTypeTextView.setText(fileContent.ext);
            }

        }
        profileImageView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
        nameTextView.setOnClickListener(v ->
                EventBus.getDefault().post(new RequestUserInfoEvent(fromEntity.id)));
    }

    private boolean hasImageUrl(String small, String large, String original) {
        return !TextUtils.isEmpty(small)
                || !TextUtils.isEmpty(large)
                || !TextUtils.isEmpty(original);
    }

    private String getOptimizedImageUrl(Context context,
                                        String small, String medium,
                                        String large, String original) {
        // XXHDPI 이상인 기기에서만 오리지널 파일을 로드
        int dpi = context.getResources().getDisplayMetrics().densityDpi;
        if (dpi > DisplayMetrics.DENSITY_XHIGH) {
            String url = original;
            return !TextUtils.isEmpty(url) ? url : getImageUrl(small, medium, large, original);
        }

        return getImageUrl(small, medium, large, original);
    }

    private String getImageUrl(String small, String medium, String large, String original) {
        // 라지 사이즈부터 조회(640 x 640)
        if (!TextUtils.isEmpty(large)) {
            return large;
        }

        // 중간 사이즈 (360 x 360)
        if (!TextUtils.isEmpty(medium)) {
            return medium;
        }

        // 원본 파일
        if (!TextUtils.isEmpty(original)) {
            return original;
        }

        return small;
    }

    @Override
    public int getLayoutId() {
        return R.layout.item_message_img_v2;

    }

}
