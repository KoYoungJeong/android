package com.tosslab.jandi.app.ui.message.v2.viewmodel;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.spannable.SpannableLookUp;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.member.WebhookBot;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.LinkifyUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.image.transform.JandiProfileTransform;
import com.tosslab.jandi.app.utils.image.transform.TransformConfig;

import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;


public class AnnouncementViewModel {

    @Bind(R.id.vg_announcement)
    ViewGroup vgAnnouncement;
    @Bind(R.id.vg_announcement_info)
    ViewGroup vgAnnouncementInfo;
    @Bind(R.id.vg_announcement_action)
    ViewGroup vgAnnouncementAction;
    @Bind(R.id.iv_announcement_user)
    ImageView ivAnnouncementUser;
    @Bind(R.id.tv_announcement_user_name)
    TextView tvAnnouncementUserName;
    @Bind(R.id.tv_announcement_date)
    TextView tvAnnouncementDate;
    @Bind(R.id.sv_announcement_message)
    ScrollView svAnnouncementMessage;
    @Bind(R.id.tv_announcement_message)
    TextView tvAnnouncementMessage;
    @Bind(R.id.btn_announcement_open)
    View btnAnnouncementOpen;
    @Bind(R.id.btn_announcement_close)
    View btnAnnouncementClose;

    Context context;

    private Announcement announcement;

    private boolean isOpened;
    private boolean isAfterViews;

    private OnAnnouncementOpenListener onAnnouncementOpenListener;
    private OnAnnouncementCloseListener onAnnouncementCloseListener;

    public AnnouncementViewModel(Context context, View view) {
        this.context = context;
        ButterKnife.bind(this, view);
        isAfterViews = true;

        if (announcement != null) {
            initAnnouncement(announcement, isOpened);
        }
    }

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


        boolean isBot = false;
        boolean isJandiBot = false;
        String name;
        String profileUrl;

        if (TeamInfoLoader.getInstance().isUser(writerId)) {
            User user = TeamInfoLoader.getInstance().getUser(writerId);
            name = user.getName();
            profileUrl = user.getPhotoUrl();
            if (!user.isEnabled()) {
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

            isJandiBot = user.isBot();

        } else if (TeamInfoLoader.getInstance().isBot(writerId)) {
            isBot = true;
            WebhookBot bot = TeamInfoLoader.getInstance().getBot(writerId);
            name = bot.getName();
            profileUrl = bot.getPhotoUrl();
        } else {
            vgAnnouncement.setVisibility(View.GONE);
            return;
        }

        vgAnnouncement.setVisibility(View.VISIBLE);

        if (isBot) {
            ImageLoader.newInstance()
                    .placeHolder(R.drawable.profile_img, ImageView.ScaleType.FIT_CENTER)
                    .actualImageScaleType(ImageView.ScaleType.CENTER_CROP)
                    .transformation(new JandiProfileTransform(ivAnnouncementUser.getContext(),
                            TransformConfig.DEFAULT_CIRCLE_BORDER_WIDTH,
                            TransformConfig.DEFAULT_CIRCLE_BORDER_COLOR,
                            Color.TRANSPARENT))
                    .uri(Uri.parse(profileUrl))
                    .into(ivAnnouncementUser);
        } else {
            if (isJandiBot) {
                ivAnnouncementUser.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                ImageLoader.loadFromResources(ivAnnouncementUser, R.drawable.logotype_80);
            } else {
                ImageUtil.loadProfileImage(ivAnnouncementUser, profileUrl, R.drawable.profile_img);
            }
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

    public void openAnnouncement(boolean isOpened) {
        btnAnnouncementOpen.setVisibility(isOpened ? View.GONE : View.VISIBLE);
        vgAnnouncementInfo.setVisibility(isOpened ? View.VISIBLE : View.GONE);
    }

    @OnClick(R.id.btn_announcement_delete)
    void showDeleteAlertDialog() {
        new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(context.getString(R.string.jandi_announcement_delete_question))
                .setPositiveButton(context.getString(R.string.jandi_confirm), (dialog, which) -> {
                    EventBus.getDefault().post(new AnnouncementEvent(AnnouncementEvent.Action.DELETE));
                })
                .setNegativeButton(context.getString(R.string.jandi_cancel), null)
                .create()
                .show();
    }

    @OnClick({R.id.vg_announcement_info, R.id.vg_announcement_message})
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

    public void showCreateAlertDialog(DialogInterface.OnClickListener confirmListener) {
        new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(context.getString(R.string.jandi_announcement_create_question))
                .setPositiveButton(context.getString(R.string.jandi_confirm), confirmListener)
                .setNegativeButton(context.getString(R.string.jandi_cancel), null)
                .create()
                .show();
    }

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
