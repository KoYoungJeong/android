package com.tosslab.jandi.app.ui.message.v2.viewmodel;

import android.app.Activity;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;
import android.widget.TextView;

import com.facebook.drawee.drawable.ScalingUtils;
import com.facebook.drawee.generic.RoundingParams;
import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.markdown.MarkdownLookUp;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.ui.commonviewmodels.markdown.viewmodel.MarkdownViewModel;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.transform.TransformConfig;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 15. 6. 24..
 */
@EBean
public class AnnouncementViewModel {

    @ViewById(R.id.vg_announcement)
    ViewGroup vgAnnouncement;
    @ViewById(R.id.vg_announcement_info)
    ViewGroup vgAnnouncementInfo;
    @ViewById(R.id.vg_announcement_action)
    ViewGroup vgAnnouncementAction;
    @ViewById(R.id.iv_announcement_user)
    SimpleDraweeView ivAnnouncementUser;
    @ViewById(R.id.tv_announcement_info)
    TextView tvAnnouncementInfo;
    @ViewById(R.id.sv_announcement_message)
    ScrollView svAnnouncementMessage;
    @ViewById(R.id.tv_announcement_message)
    TextView tvAnnouncementMessage;
    @ViewById(R.id.btn_announcement_open)
    View btnAnnouncementOpen;
    @ViewById(R.id.btn_announcement_close)
    View btnAnnouncementClose;

    @RootContext
    Activity activity;
    private ResAnnouncement announcement;

    private boolean isOpened;
    private boolean isAfterViews;

    private OnAnnouncementOpenListener onAnnouncementOpenListener;
    private OnAnnouncementCloseListener onAnnouncementCloseListener;

    @AfterViews
    void init() {
        isAfterViews = true;

        if (announcement != null) {
            initAnnouncement(announcement, isOpened);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setAnnouncement(ResAnnouncement announcement, boolean isOpened) {
        this.announcement = announcement;
        this.isOpened = isOpened;

        if (!isAfterViews) {
            return;
        }

        initAnnouncement(announcement, isOpened);
    }

    private void initAnnouncement(ResAnnouncement announcement, boolean isOpened) {
        if (announcement == null || announcement.isEmpty()) {
            vgAnnouncement.setVisibility(View.GONE);
            return;
        }

        int writerId = announcement.getWriterId();
        String writtenAt = announcement.getWrittenAt();
        String content = announcement.getContent();

        EntityManager entityManager = EntityManager.getInstance();
        FormattedEntity fromEntity = entityManager.getEntityById(writerId);
        if (fromEntity == EntityManager.UNKNOWN_USER_ENTITY) {
            vgAnnouncement.setVisibility(View.GONE);
            return;
        }

        vgAnnouncement.setVisibility(View.VISIBLE);

        String profileUrl = fromEntity.getUserLargeProfileUrl();


        if (entityManager.isBot(writerId)) {
            if (entityManager.isJandiBot(writerId)) {
                ImageLoader.newBuilder()
                        .actualScaleType(ScalingUtils.ScaleType.CENTER_INSIDE)
                        .load(UriFactory.getResourceUri(R.drawable.bot_80x100))
                        .into(ivAnnouncementUser);
            } else {
                RoundingParams circleRoundingParams = ImageUtil.getCircleRoundingParams(
                        TransformConfig.DEFAULT_CIRCLE_LINE_COLOR, TransformConfig.DEFAULT_CIRCLE_LINE_WIDTH);

                ImageLoader.newBuilder()
                        .placeHolder(R.drawable.profile_img, ScalingUtils.ScaleType.FIT_CENTER)
                        .actualScaleType(ScalingUtils.ScaleType.CENTER_CROP)
                        .roundingParams(circleRoundingParams)
                        .load(Uri.parse(fromEntity.getUserLargeProfileUrl()))
                        .into(ivAnnouncementUser);
            }
        } else {
            ImageUtil.loadProfileImage(ivAnnouncementUser, profileUrl, R.drawable.profile_img);
        }

        ivAnnouncementUser.setOnClickListener(v -> {
            if (EntityManager.getInstance().isBot(writerId)
                    && !EntityManager.getInstance().isJandiBot(writerId)) {
                // 잔디봇이 아닌 봇은 예외 처리
                return;
            }
            ShowProfileEvent event = new ShowProfileEvent(writerId, ShowProfileEvent.From.Image);
            EventBus.getDefault().post(event);
        });

        String date = DateTransformator.getTimeStringFromISO(
                writtenAt, DateTransformator.FORMAT_YYYYMMDD_HHMM_A);
        String announcementInfo = String.format("%s %s", fromEntity.getName(), date);
        tvAnnouncementInfo.setText(announcementInfo);

        SpannableStringBuilder messageStringBuilder = MarkdownLookUp
                .text(content)
                .lookUp(tvAnnouncementMessage.getContext());
        LinkifyUtil.addLinks(activity, messageStringBuilder);
        LinkifyUtil.setOnLinkClick(tvAnnouncementMessage);

        MarkdownViewModel markdownViewModel = new MarkdownViewModel(tvAnnouncementMessage,
                messageStringBuilder, true);
        markdownViewModel.execute();

        tvAnnouncementMessage.setText(messageStringBuilder);

        boolean isFullShowing = vgAnnouncementAction.getVisibility() == View.VISIBLE;
        scaleAnnouncementTextArea(isFullShowing);

        if (onAnnouncementOpenListener != null) {
            btnAnnouncementOpen.setOnClickListener((view) -> onAnnouncementOpenListener.onOpen());
        }
        if (onAnnouncementCloseListener != null) {
            btnAnnouncementClose.setOnClickListener((view) -> onAnnouncementCloseListener.onClose());
        }

        openAnnouncement(isOpened);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void openAnnouncement(boolean isOpened) {
        btnAnnouncementOpen.setVisibility(isOpened ? View.GONE : View.VISIBLE);
        vgAnnouncementInfo.setVisibility(isOpened ? View.VISIBLE : View.GONE);
    }

    @Click(R.id.btn_announcement_delete)
    void showDeleteAlertDialog() {
        new AlertDialog.Builder(activity, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(activity.getString(R.string.jandi_announcement_delete_question))
                .setPositiveButton(activity.getString(R.string.jandi_confirm), (dialog, which) -> {
                    EventBus.getDefault().post(new AnnouncementEvent(AnnouncementEvent.Action.DELETE));
                })
                .setNegativeButton(activity.getString(R.string.jandi_cancel), null)
                .create()
                .show();
    }

    @Click({R.id.vg_announcement_info, R.id.vg_announcement_message})
    void showAndHideAnnouncementAction() {
        int visibility = vgAnnouncementAction.getVisibility();
        vgAnnouncementAction.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);

        boolean isFullShowing = vgAnnouncementAction.getVisibility() == View.VISIBLE;

        scaleAnnouncementTextArea(isFullShowing);

        AnalyticsValue.Action action = isFullShowing
                ? AnalyticsValue.Action.Announcement_Expand : AnalyticsValue.Action.Accouncement_Restore;

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, action);
    }

    private void scaleAnnouncementTextArea(final boolean isFullShowing) {
        if (isFullShowing) {
            tvAnnouncementMessage.setSingleLine(false);
        } else {
            tvAnnouncementMessage.setSingleLine();
        }

        tvAnnouncementMessage.post(() -> {
//            // 7줄 이상인 경우 7줄까지만 보여주는게 디자인 의도임
            int lineCount = tvAnnouncementMessage.getLineCount();
            if (lineCount >= 7) {
                lineCount = 7;
            } else if (lineCount <= 0) {
                lineCount = 1;
            }

            int newHeight = tvAnnouncementMessage.getLineHeight() * lineCount;
            // 폰트가 잘리거나 밑줄이 안보이는 경우가 있어 2dp 정도 여백을 남김
            float space = JandiApplication.getContext().getResources().getDisplayMetrics().density * 2;
            ViewGroup.LayoutParams layoutParams = svAnnouncementMessage.getLayoutParams();
            layoutParams.height = newHeight + (int) (space * 2);
            svAnnouncementMessage.setLayoutParams(layoutParams);

            svAnnouncementMessage.scrollTo(0, 0);
        });
    }

    @UiThread
    public void showCreateAlertDialog(DialogInterface.OnClickListener confirmListener) {
        new AlertDialog.Builder(activity, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(activity.getString(R.string.jandi_announcement_create_question))
                .setPositiveButton(activity.getString(R.string.jandi_confirm), confirmListener)
                .setNegativeButton(activity.getString(R.string.jandi_cancel), null)
                .create()
                .show();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setAnnouncementViewVisibility(boolean visibility) {
        if (vgAnnouncement == null) {
            return;
        }

        if (announcement == null || announcement.isEmpty()) {
            return;
        }

        vgAnnouncement.setVisibility(visibility ? View.VISIBLE : View.GONE);
    }

    public void setOnAnnouncementOpenListener(OnAnnouncementOpenListener onAnnouncementOpenListener) {
        this.onAnnouncementOpenListener = onAnnouncementOpenListener;
    }

    public void setOnAnnouncementCloseListener(OnAnnouncementCloseListener onAnnouncementCloseListener) {
        this.onAnnouncementCloseListener = onAnnouncementCloseListener;
    }

    public interface OnAnnouncementOpenListener {
        void onOpen();
    }

    public interface OnAnnouncementCloseListener {
        void onClose();
    }
}
