package com.tosslab.jandi.app.ui.invites;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by tee on 15. 6. 9..
 */

@EBean
public class InvitationDialogExecutor {

    public static final int FROM_MAIN_INVITE = 1;
    public static final int FROM_MAIN_MEMBER = 2;
    public static final int FROM_MAIN_TEAM = 7;
    public static final int FROM_TOPIC_CHAT = 3;
    public static final int FROM_TOPIC_MEMBER = 4;
    public static final int FROM_MAIN_POPUP = 5;
    public static final int FROM_CHAT_CHOOSE = 6;

    @RootContext
    AppCompatActivity activity;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    private int from;
    private ProgressWheel progressWheel;
    private EntityManager entityManager;

    @Background
    public void execute() {

        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        entityManager = EntityManager.getInstance();
        showProgressWheel();

        try {
            ResTeamDetailInfo.InviteTeam inviteTeam = getTeamInfo(entityManager.getTeamId());
            AvailableState availableState = availableState(inviteTeam);
            switch (availableState) {
                case AVAIL:
                    InvitationDialogFragment invitationDialog =
                            InvitationDialogFragment.newInstance(inviteTeam.getName(), inviteTeam.getInvitationUrl(), from);
                    invitationDialog.show(activity.getSupportFragmentManager(), "invitationsDialog");
                    break;
                case UNDEFINE:
                    showErrorToast(activity.getResources().getString(R.string.err_entity_invite));
                    break;
                case DISABLE:
                    showTextDialog(activity.getResources().getString(R.string.jandi_invite_disabled, getOwnerName()));
                    break;
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
            showErrorToast(activity.getResources().getString(R.string.err_network));
        } catch (Exception e) {
            e.printStackTrace();
            showErrorToast(activity.getResources().getString(R.string.err_entity_invite));
        }

        dismissProgressWheel();
    }

    private ResTeamDetailInfo.InviteTeam getTeamInfo(long teamId) throws RetrofitError {
        ResTeamDetailInfo.InviteTeam resTeamDetailInfo = RequestApiManager.getInstance().getTeamInfoByTeamApi(teamId);
        return resTeamDetailInfo;
    }


    private AvailableState availableState(ResTeamDetailInfo.InviteTeam inviteTeam) {
        String invitationUrl = inviteTeam.getInvitationUrl();
        String invitationStatus = inviteTeam.getInvitationStatus();
        if (!TextUtils.isEmpty(invitationUrl) && invitationUrl.contains("undefined")) {
            return AvailableState.UNDEFINE;
        }
        if (TextUtils.isEmpty(invitationStatus) || TextUtils.equals(invitationStatus, "disabled")) {
            return AvailableState.DISABLE;
        }
        return AvailableState.AVAIL;
    }

    @UiThread
    void showErrorToast(String message) {
        ColoredToast.showError(message);
    }

    @UiThread
    public void showTextDialog(String alertText) {
        new AlertDialog.Builder(activity, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton(activity.getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> dialog.dismiss())
                .create().show();
    }

    @UiThread
    void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread
    void showProgressWheel() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(activity);
        }

        if (progressWheel != null && !progressWheel.isShowing())
            progressWheel.show();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(activity, null);
    }

    private String getOwnerName() {
        List<FormattedEntity> users = entityManager.getFormattedUsers();
        FormattedEntity tempDefaultEntity = new FormattedEntity();
        FormattedEntity owner = Observable.from(users)
                .filter(formattedEntity ->
                        formattedEntity.isTeamOwner())
                .firstOrDefault(tempDefaultEntity)
                .toBlocking()
                .first();
        return owner.getUser().name;
    }

    public void setFrom(int from) {
        this.from = from;
    }

    private enum AvailableState {
        AVAIL, UNDEFINE, DISABLE
    }

}
