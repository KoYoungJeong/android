package com.tosslab.jandi.app.ui.message.v2.model.announcement;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.text.util.Linkify;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Scroller;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.AnnouncementEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.LinkifyUtil;

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

    public interface OnAnnouncementOpenListener {
        void onOpen();
    }

    public interface OnAnnouncementCloseListener {
        void onClose();
    }

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

    @ViewById(R.id.btn_announcement_delete)
    View btnAnnouncementDelete;

    private OnAnnouncementOpenListener onAnnouncementOpenListener;
    private OnAnnouncementCloseListener onAnnouncementCloseListener;

    @RootContext
    Activity activity;

    @AfterViews
    void init() {
    }

    @UiThread
    public void setAnnouncement(ResAnnouncement announcement, boolean isOpened) {
        if (announcement == null || announcement.isEmpty()) {
            vgAnnouncement.setVisibility(View.GONE);
            return;
        }

        int writerId = announcement.getWriterId();
        String writtenAt = announcement.getWrittenAt();
        String content = announcement.getContent();

        EntityManager entityManager = EntityManager.getInstance(activity);
        FormattedEntity fromEntity = entityManager.getEntityById(writerId);
        if (fromEntity == null) {
            vgAnnouncement.setVisibility(View.GONE);
            return;
        }

        vgAnnouncement.setVisibility(View.VISIBLE);

        String profileUrl = fromEntity.getUserLargeProfileUrl();
        Ion.with(ivAnnouncementUser)
                .placeholder(R.drawable.jandi_profile)
                .error(R.drawable.jandi_profile)
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

        if (onAnnouncementOpenListener != null) {
            btnAnnouncementOpen.setOnClickListener((view) -> onAnnouncementOpenListener.onOpen());
        }
        if (onAnnouncementCloseListener != null) {
            btnAnnouncementClose.setOnClickListener((view) -> onAnnouncementCloseListener.onClose());
        }

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
        if (vgAnnouncementAction.getVisibility() == View.VISIBLE) {
            tvAnnouncementMessage.setSingleLine(false);
            tvAnnouncementMessage.setMaxLines(7);
            tvAnnouncementMessage.setVerticalScrollBarEnabled(true);
            tvAnnouncementMessage.setMovementMethod(new ScrollingMovementMethod());
        } else {
            tvAnnouncementMessage.setSingleLine();
            tvAnnouncementMessage.setVerticalScrollBarEnabled(false);
            tvAnnouncementMessage.setMovementMethod(null);
        }
//        String originText = tvAnnouncementMessage.getTag().toString();
//        String originText = tvAnnouncementMessage.getText().toString();
//        tvAnnouncementMessage.setText(originText);
        tvAnnouncementMessage.invalidate();
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

    public void setOnAnnouncementOpenListener(OnAnnouncementOpenListener onAnnouncementOpenListener) {
        this.onAnnouncementOpenListener = onAnnouncementOpenListener;
    }

    public void setOnAnnouncementCloseListener(OnAnnouncementCloseListener onAnnouncementCloseListener) {
        this.onAnnouncementCloseListener = onAnnouncementCloseListener;
    }
}
