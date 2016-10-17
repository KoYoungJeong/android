package com.tosslab.jandi.app.ui.invites;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.invites.email.InviteByEmailActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
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

    @Inject
    Lazy<TeamApi> teamApi;
    private int from;
    private ProgressWheel progressWheel;

    public static boolean canBeInviation(String invitationStatus, String invitationUrl) {
        return availableState(invitationStatus, invitationUrl) == AvailableState.AVAIL;
    }

    private static AvailableState availableState(String invitationStatus, String invitationUrl) {
        if (!TextUtils.isEmpty(invitationUrl) && invitationUrl.contains("undefined")) {
            return AvailableState.UNDEFINE;
        }
        if (TextUtils.isEmpty(invitationStatus) || TextUtils.equals(invitationStatus, "disabled")) {
            return AvailableState.DISABLE;
        }
        return AvailableState.AVAIL;
    }

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public void execute() {

        try {
            TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();
            boolean teamOwner = teamInfoLoader.getUser(teamInfoLoader.getMyId()).isTeamOwner();
            String teamName = teamInfoLoader.getTeamName();
            String invitationStatus = teamInfoLoader.getInvitationStatus();
            String invitationUrl = teamInfoLoader.getInvitationUrl();

            AvailableState availableState = availableState(invitationStatus, invitationUrl);
            switch (availableState) {
                case AVAIL:
                    InvitationDialogFragment invitationDialog =
                            InvitationDialogFragment.newInstance(teamName, invitationUrl, from);
                    invitationDialog.show(activity.getSupportFragmentManager(), "invitationsDialog");
                    break;
                case UNDEFINE:
                    if (!teamOwner) {
                        showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_entity_invite));
                    }
                    break;
                case DISABLE:
                    if (!teamOwner) {
                        showTextDialog(JandiApplication.getContext().getResources().getString(R.string.jandi_invite_disabled, getOwnerName()));
                    }
                    break;
            }

            if (teamOwner && availableState != AvailableState.AVAIL) {
                ColoredToast.showGray(R.string.jandi_invitation_for_admin);

                Intent intent = new Intent(activity, InviteByEmailActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                activity.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorToast(JandiApplication.getContext().getResources().getString(R.string.err_entity_invite));
        }

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
                        (dialog, id) -> {
                            if (from == FROM_MAIN_TEAM) {
                                AnalyticsUtil.sendEvent(
                                        AnalyticsValue.Screen.TeamTab,
                                        AnalyticsValue.Action.InviteMember_InviteDisabled);
                            }
                            dialog.dismiss();
                        })
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
        List<User> users = TeamInfoLoader.getInstance().getUserList();
        return Observable.from(users)
                .filter(User::isTeamOwner)
                .map(User::getName)
                .toBlocking()
                .firstOrDefault("");
    }

    public void setFrom(int from) {
        this.from = from;
    }

    private enum AvailableState {
        AVAIL, UNDEFINE, DISABLE
    }

}
