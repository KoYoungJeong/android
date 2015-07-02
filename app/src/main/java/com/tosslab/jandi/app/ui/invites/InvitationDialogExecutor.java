package com.tosslab.jandi.app.ui.invites;

import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

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

    @RootContext
    AppCompatActivity context;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    private ProgressWheel mProgressWheel;
    private EntityManager entityManager;

    @Background
    public void execute() {
        entityManager = EntityManager.getInstance(context);
        showProgressWheel();

        try {
            ResTeamDetailInfo.InviteTeam inviteTeam = teamDomainInfoModel.getTeamInfo(entityManager.getTeamId());
            AvailableState availableState = availableState(inviteTeam);
            switch (availableState) {
                case AVAIL:
                    InvitationDialogFragment invitationDialog =
                            InvitationDialogFragment.newInstance(inviteTeam.getName(), inviteTeam.getInvitationUrl());
                    invitationDialog.show(context.getSupportFragmentManager(), "invitationsDialog");
                    break;
                case UNDEFINE:
                    showErrorToast(context.getResources().getString(R.string.err_entity_invite));
                    break;
                case DISABLE:
                    showTextDialog(context.getResources().getString(R.string.jandi_invite_disabled, getOwnerName()));
                    break;
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
            showErrorToast(context.getResources().getString(R.string.err_network));
        } catch (Exception e) {
            e.printStackTrace();
            showErrorToast(context.getResources().getString(R.string.err_entity_invite));
        }

        dismissProgressWheel();
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
        ColoredToast.showError(context, message);
    }

    @UiThread
    public void showTextDialog(String alertText) {
        new AlertDialog.Builder(context)
                .setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton(context.getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> dialog.dismiss())
                .create().show();
    }

    @UiThread
    void dismissProgressWheel() {
        if (mProgressWheel != null && mProgressWheel.isShowing()) {
            mProgressWheel.dismiss();
        }
    }

    @UiThread
    void showProgressWheel() {
        if (mProgressWheel == null) {
            mProgressWheel = new ProgressWheel(context);
            mProgressWheel.init();
        }

        if (mProgressWheel != null && !mProgressWheel.isShowing())
            mProgressWheel.show();
    }

    private String getOwnerName() {
        List<FormattedEntity> users = entityManager.getFormattedUsers();
        FormattedEntity tempDefaultEntity = new FormattedEntity();
        FormattedEntity owner = Observable.from(users)
                .filter(formattedEntity ->
                        TextUtils.equals(formattedEntity.getUser().u_authority, "owner"))
                .firstOrDefault(tempDefaultEntity)
                .toBlocking()
                .first();
        return owner.getUser().name;
    }

    private enum AvailableState {
        AVAIL, UNDEFINE, DISABLE
    }

}
