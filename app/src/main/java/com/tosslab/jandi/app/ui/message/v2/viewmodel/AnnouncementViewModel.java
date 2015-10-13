package com.tosslab.jandi.app.ui.message.v2.viewmodel;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.transform.ion.IonCircleTransform;

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
    ImageView ivAnnouncementUser;
    @ViewById(R.id.tv_announcement_info)
    TextView tvAnnouncementInfo;
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
    private TextLineDetermineRunnable textLineDetermineRunnable;

    @AfterViews
    void init() {
        isAfterViews = true;

        textLineDetermineRunnable = new TextLineDetermineRunnable(tvAnnouncementMessage);

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
        Ion.with(ivAnnouncementUser)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new IonCircleTransform())
                .crossfade(true)
                .load(profileUrl);

        ResLeftSideMenu.User user = fromEntity.getUser();
        String date = DateTransformator.getTimeStringFromISO(
                writtenAt, DateTransformator.FORMAT_YYYYMMDD_HHMM_A);
        String announcementInfo = String.format("%s %s", user.name, date);
        tvAnnouncementInfo.setText(announcementInfo);

        SpannableStringBuilder messageStringBuilder = new SpannableStringBuilder();
        messageStringBuilder.append(!TextUtils.isEmpty(content) ? content : "");
        boolean hasLink = LinkifyUtil.addLinks(activity, messageStringBuilder);
        if (hasLink) {
            Spannable linkSpannable =
                    Spannable.Factory.getInstance().newSpannable(messageStringBuilder);
            messageStringBuilder.setSpan(linkSpannable,
                    0, content.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            LinkifyUtil.setOnLinkClick(tvAnnouncementMessage);
        }
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
        new AlertDialog.Builder(activity)
                .setMessage(activity.getString(R.string.jandi_announcement_delete_question))
                .setPositiveButton(activity.getString(R.string.jandi_confirm), (dialog, which) -> {
                    EventBus.getDefault().post(new AnnouncementEvent(AnnouncementEvent.Action.DELETE));
                })
                .setNegativeButton(activity.getString(R.string.jandi_cancel), null)
                .create()
                .show();
    }

    @Click(R.id.vg_announcement_info)
    void showAndHideAnnouncementAction() {
        int visibility = vgAnnouncementAction.getVisibility();
        vgAnnouncementAction.setVisibility(visibility == View.VISIBLE ? View.GONE : View.VISIBLE);

        boolean isFullShowing = vgAnnouncementAction.getVisibility() == View.VISIBLE;

        scaleAnnouncementTextArea(isFullShowing);

        AnalyticsValue.Action action = isFullShowing
                ? AnalyticsValue.Action.Announcement_Expand : AnalyticsValue.Action.Accouncement_Restore;

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicChat, action);
    }

    private void scaleAnnouncementTextArea(boolean isFullShowing) {
        if (isFullShowing) {
            tvAnnouncementMessage.setSingleLine(false);
            tvAnnouncementMessage.post(textLineDetermineRunnable);
        } else {
            tvAnnouncementMessage.setSingleLine();
            tvAnnouncementMessage.setVerticalScrollBarEnabled(false);
            tvAnnouncementMessage.setMovementMethod(null);
            tvAnnouncementMessage.postInvalidate();
        }
    }

    @UiThread
    public void showCreateAlertDialog(DialogInterface.OnClickListener confirmListener) {
        new AlertDialog.Builder(activity)
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

    private static class TextLineDetermineRunnable implements Runnable {
        private TextView tvAnnouncementMessage;

        public TextLineDetermineRunnable(TextView textView) {
            tvAnnouncementMessage = textView;
        }

        @Override
        public void run() {
            int lineCount = tvAnnouncementMessage.getLineCount();

            // 7줄 이상인 경우 maxLine 선언
            boolean exceedMaxLine = lineCount >= 7;
            if (exceedMaxLine) {
                tvAnnouncementMessage.setMaxLines(7);
            }

            tvAnnouncementMessage.setVerticalScrollBarEnabled(exceedMaxLine);
            tvAnnouncementMessage.setMovementMethod(exceedMaxLine ? new ScrollingMovementMethod() : null);

            tvAnnouncementMessage.invalidate();

            // 스크롤 되어 있는 상태에서 7줄 이하의 글이 공지로 등록될 경우 대비
            tvAnnouncementMessage.scrollTo(0, 0);

            // 가끔 상위 레이아웃이 제대로 펼쳐지지 못해 강제로 호출
            ((ViewGroup) tvAnnouncementMessage.getParent()).postInvalidate();
        }
    }
}
