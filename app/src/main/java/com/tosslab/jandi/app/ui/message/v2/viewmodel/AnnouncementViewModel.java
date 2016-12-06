package com.tosslab.jandi.app.ui.message.v2.viewmodel;

import android.app.Activity;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.BotRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
import com.tosslab.jandi.app.utils.image.transform.TransformConfig;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.Date;

import de.greenrobot.event.EventBus;

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
    @ViewById(R.id.tv_announcement_user_name)
    TextView tvAnnouncementUserName;
    @ViewById(R.id.tv_announcement_date)
    TextView tvAnnouncementDate;
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
    private Announcement announcement;

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
    public void setAnnouncement(Announcement announcement) {
        this.announcement = announcement;
        this.isOpened = announcement != null && announcement.isOpened();

        if (!isAfterViews) {
            return;
        }

        initAnnouncement(announcement, isOpened);
    }

    private void initAnnouncement(Announcement announcement, boolean isOpened) {
        if (announcement == null) {
            vgAnnouncement.setVisibility(View.GONE);
            return;
        }

        long writerId = announcement.getWriterId();
        Date writtenAt = announcement.getWrittenAt();
        String content = announcement.getContent();

        Human human = HumanRepository.getInstance().getHuman(writerId);
        Bot bot = BotRepository.getInstance().getBot(writerId);

        boolean isBot = false;
        boolean isJandiBot = false;
        String name;
        String profileUrl;

        if (human != null) {
            name = human.getName();
            profileUrl = human.getPhotoUrl();
            if (TextUtils.equals(human.getStatus(), "disabled")) {
                tvAnnouncementUserName.setPaintFlags(
                        tvAnnouncementUserName.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                ivAnnouncementUser.setColorFilter(0x85ffffff);
            } else {
                if ((tvAnnouncementUserName.getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG) > 0) {
                    tvAnnouncementUserName.setPaintFlags(
                            tvAnnouncementUserName.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                ivAnnouncementUser.clearColorFilter();
            }
        } else if (bot != null) {
            isBot = true;
            name = bot.getName();
            profileUrl = bot.getPhotoUrl();
            if (TextUtils.equals(bot.getType(), "jandi_bot")) {
                isJandiBot = true;
            }
        } else {
            vgAnnouncement.setVisibility(View.GONE);
            return;
        }

        vgAnnouncement.setVisibility(View.VISIBLE);

        if (isBot) {
            if (isJandiBot) {
                ivAnnouncementUser.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ImageLoader.loadFromResources(ivAnnouncementUser, R.drawable.logotype_80);
            } else {
                ImageLoader.newInstance()
                        .placeHolder(R.drawable.profile_img, ImageView.ScaleType.FIT_CENTER)
                        .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                        .transformation(new JandiProfileTransform(ivAnnouncementUser.getContext(),
                                TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                                TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                                Color.TRANSPARENT))
                        .uri(Uri.parse(profileUrl))
                        .into(ivAnnouncementUser);
            }
        } else {
            ImageUtil.loadProfileImage(ivAnnouncementUser, profileUrl, R.drawable.profile_img);
        }

        final boolean finalIsBot = isBot;
        final boolean finalIsJandiBot = isJandiBot;
        ivAnnouncementUser.setOnClickListener(v -> {
            if (finalIsBot && !finalIsJandiBot) {
                // 잔디봇이 아닌 봇은 예외 처리
                return;
            }
            ShowProfileEvent event = new ShowProfileEvent(writerId, ShowProfileEvent.From.Image);
            EventBus.getDefault().post(event);
        });

        String date = DateTransformator.getTimeString(writtenAt);
        tvAnnouncementDate.setText(date);
        tvAnnouncementUserName.setText(name);

        SpannableStringBuilder messageStringBuilder = SpannableLookUp.text(content)
                .hyperLink(false)
                .markdown(false)
                .webLink(false)
                .emailLink(false)
                .telLink(false)
                .lookUp(tvAnnouncementMessage.getContext());

        LinkifyUtil.setOnLinkClick(tvAnnouncementMessage);

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

        if (announcement == null) {
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
