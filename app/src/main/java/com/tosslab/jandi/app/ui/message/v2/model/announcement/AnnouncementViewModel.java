package com.tosslab.jandi.app.ui.message.v2.model.announcement;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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

    public interface OnAnnouncementDeleteListener {
        void onDelete();
    }

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

        tvAnnouncementMessage.setText(content);

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
                .setMessage("삭제할래용?")
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
    }

    public void setOnAnnouncementOpenListener(OnAnnouncementOpenListener onAnnouncementOpenListener) {
        this.onAnnouncementOpenListener = onAnnouncementOpenListener;
    }

    public void setOnAnnouncementCloseListener(OnAnnouncementCloseListener onAnnouncementCloseListener) {
        this.onAnnouncementCloseListener = onAnnouncementCloseListener;
    }
}
