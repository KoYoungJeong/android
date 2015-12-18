package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;
import com.tosslab.jandi.app.views.spannable.EntitySpannable;
import com.tosslab.jandi.app.views.spannable.MessageSpannable;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 4. 29..
 */
@EBean
public class FileHeadManager {

    @RootContext
    AppCompatActivity activity;

    // in File Detail Header
    private SimpleDraweeView imageViewUserProfile;
    private TextView textViewUserName;
    private TextView textViewFileCreateDate;
    private TextView textViewFileContentInfo;
    private TextView textViewFileSharedCdp;
    private View disableLineThroughView;
    private View disableCoverView;

    private ImageView btnFileDetailStarred;
    private SimpleDraweeView imageViewPhotoFile;
    private ViewGroup vgDetailPhoto;
    private ImageView iconFileType;
    private LinearLayout fileInfoLayout;
    private int roomId;
    private View vgDeleted;
    private TextView tvDeletedDate;

    public View getHeaderView() {
        View header = LayoutInflater.from(activity).inflate(R.layout.activity_file_detail_header, null, false);
        imageViewUserProfile = (SimpleDraweeView) header.findViewById(R.id.img_file_detail_user_profile);
        textViewUserName = (TextView) header.findViewById(R.id.txt_file_detail_user_name);
        textViewFileCreateDate = (TextView) header.findViewById(R.id.txt_file_detail_create_date);
        textViewFileContentInfo = (TextView) header.findViewById(R.id.txt_file_detail_file_info);
        textViewFileSharedCdp = (TextView) header.findViewById(R.id.txt_file_detail_shared_cdp);
        imageViewPhotoFile = (SimpleDraweeView) header.findViewById(R.id.img_file_detail_photo);
        vgDetailPhoto = (ViewGroup) header.findViewById(R.id.vg_file_detail_photo);
        fileInfoLayout = (LinearLayout) header.findViewById(R.id.ly_file_detail_info);
        iconFileType = (ImageView) header.findViewById(R.id.icon_file_detail_content_type);
        disableLineThroughView = header.findViewById(R.id.iv_entity_listitem_line_through);
        disableCoverView = header.findViewById(R.id.v_entity_listitem_warning);
        btnFileDetailStarred = (ImageView) header.findViewById(R.id.bt_file_detail_starred);

        vgDeleted = header.findViewById(R.id.vg_file_detail_deleted);
        tvDeletedDate = ((TextView) header.findViewById(R.id.tv_file_detail_deleted_date));
        return header;
    }

    public void drawFileWriterState(boolean isEnabled) {
        if (isEnabled) {
            disableCoverView.setVisibility(View.GONE);
            disableLineThroughView.setVisibility(View.GONE);
        } else {
            disableCoverView.setVisibility(View.VISIBLE);
            disableLineThroughView.setVisibility(View.VISIBLE);
            textViewUserName.setTextColor(activity.getResources().getColor(R.color.deactivate_text_color));
        }
    }

    public void drawFileSharedEntities(ResMessages.FileMessage resFileDetail) {
        if (resFileDetail == null) {
            return;
        }
        EntityManager mEntityManager = EntityManager.getInstance();
        if (mEntityManager == null) {
            return;
        }

        int teamId = mEntityManager.getTeamId();

        if (resFileDetail.shareEntities != null && !resFileDetail.shareEntities.isEmpty()) {
            SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
            int shareTextSize = (int) activity.getResources().getDimension(R.dimen.jandi_text_size_small);
            int shareTextColor = activity.getResources().getColor(R.color.file_type);
            MessageSpannable messageSpannable = new MessageSpannable(shareTextSize, shareTextColor);
            spannableStringBuilder.append(activity.getString(R.string.jandi_shared_in_room))
                    .setSpan(messageSpannable, 0, spannableStringBuilder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

            spannableStringBuilder.append(" ");
            int firstLength = spannableStringBuilder.length();

            Observable.from(resFileDetail.shareEntities)
                    .distinct(ResMessages.OriginalMessage.IntegerWrapper::getShareEntity)
                    .map(integerWrapper -> mEntityManager.getEntityById(integerWrapper.getShareEntity()))
                    .filter(formattedEntity -> formattedEntity != EntityManager.UNKNOWN_USER_ENTITY)
                    .doOnNext(formattedEntity1 -> {
                        if (spannableStringBuilder.length() > firstLength) {
                            spannableStringBuilder.append(", ");
                        }
                    })
                    .subscribe(formattedEntity2 -> {
                        int entityType;
                        if (formattedEntity2.isPrivateGroup()) {
                            entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
                        } else if (formattedEntity2.isPublicTopic()) {
                            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
                        } else {
                            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;

                        }

                        EntitySpannable entitySpannable = new EntitySpannable(activity, teamId, formattedEntity2.getId(), entityType, formattedEntity2.isStarred);

                        int length = spannableStringBuilder.length();
                        spannableStringBuilder.append(formattedEntity2.getName());

                        spannableStringBuilder.setSpan(entitySpannable, length,
                                length + formattedEntity2.getName().length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    }, Throwable::printStackTrace);

            textViewFileSharedCdp.setMovementMethod(LinkMovementMethod.getInstance());
            textViewFileSharedCdp.setText(spannableStringBuilder, TextView.BufferType.SPANNABLE);
        } else {
            textViewFileSharedCdp.setText(R.string.jandi_nowhere_shared_file);
        }

    }

    public void setFileInfo(ResMessages.FileMessage fileMessage) {
        // 사용자
        FormattedEntity writer = EntityManager.getInstance().getEntityById(fileMessage.writerId);

        String profileUrl = writer.getUserSmallProfileUrl();

        ImageUtil.loadCircleImageByFresco(imageViewUserProfile, profileUrl, R.drawable.profile_img);

        String userName = writer.getName();
        textViewUserName.setText(userName);

        imageViewUserProfile.setOnClickListener(v -> {
            EventBus.getDefault().post(new ShowProfileEvent(writer.getId(), ShowProfileEvent.From.Image));
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewProfile);
        });
        textViewUserName.setOnClickListener(v -> {
            EventBus.getDefault().post(new ShowProfileEvent(writer.getId(), ShowProfileEvent.From.Name));
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewProfile);
        });

        btnFileDetailStarred.setSelected(fileMessage.isStarred);

        // 파일
        String createTime = DateTransformator.getTimeString(fileMessage.createTime);
        textViewFileCreateDate.setText(createTime);
        // if Deleted File
        if (TextUtils.equals(fileMessage.status, "archived")) {
            imageViewPhotoFile.setOnClickListener(null);
            imageViewPhotoFile.setVisibility(View.GONE);
            fileInfoLayout.setVisibility(View.GONE);

            vgDeleted.setVisibility(View.VISIBLE);
            String timeString = DateTransformator.getTimeString(fileMessage.updateTime, DateTransformator.FORMAT_YYYYMMDD_HHMM_A);
            String deletedDate = tvDeletedDate.getResources().getString(R.string.jandi_file_deleted_with_date, timeString);
            tvDeletedDate.setText(deletedDate);

        } else {
            vgDeleted.setVisibility(View.GONE);

            ActionBar actionBar = activity.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(fileMessage.content.title);
            }

            MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileMessage.content.serverUrl);

            String fileSizeString = FormatConverter.formatFileSize(fileMessage.content.size);
            switch (sourceType) {

                case S3:
                    textViewFileContentInfo.setText(fileSizeString + " " + fileMessage.content.ext);
                    break;
                case Google:
                case Dropbox:
                    textViewFileContentInfo.setText(fileMessage.content.ext);
                    break;
            }

            // 공유 CDP 이름
            drawFileSharedEntities(fileMessage);

            if (!TextUtils.isEmpty(fileMessage.content.type)) {

                FileThumbLoader thumbLoader;
                if (fileMessage.content.type.startsWith("image")) {
                    thumbLoader = new ImageThumbLoader(iconFileType, vgDetailPhoto, imageViewPhotoFile, roomId);
                } else {
                    thumbLoader = new NormalThumbLoader(iconFileType, imageViewPhotoFile);
                }

                thumbLoader.loadThumb(fileMessage);
            }
        }

        boolean enabledUser = isEnabledUser(fileMessage.writerId);
        drawFileWriterState(enabledUser);
    }

    private boolean isEnabledUser(int writerId) {
        EntityManager entityManager = EntityManager.getInstance();
        String userStatus = entityManager.getEntityById(writerId).getUser().status;
        return TextUtils.equals(userStatus, "enabled");
    }

    public ImageView getStarredButton() {
        return btnFileDetailStarred;
    }

    public void setRoomId(int roomId) {

        this.roomId = roomId;
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void updateStarred(boolean starred) {
        btnFileDetailStarred.setSelected(starred);
    }
}
