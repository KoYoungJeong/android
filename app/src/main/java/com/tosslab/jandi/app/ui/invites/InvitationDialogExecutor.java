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
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.List;

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


    @AfterViews
    private void init(){
        initProgressWheel();
        entityManager = EntityManager.getInstance(context);
    }

    public void execute(){
        showProgressWheel();
        ResTeamDetailInfo.InviteTeam inviteTeam = getInviteTeam();
        dismissProgressWheel();
        if(inviteTeam != null && availableCheck(inviteTeam)){
            InvitationDialogFragment invitationDialog = new InvitationDialogFragment();
            invitationDialog.setInit(inviteTeam);
            invitationDialog.show(context.getSupportFragmentManager(), "invitationsDialog");
        }
    }

    private boolean availableCheck(ResTeamDetailInfo.InviteTeam inviteTeam){
        String invitationUrl = inviteTeam.getInvitationUrl();
        String invitationStatus = inviteTeam.getInvitationStatus();
        if (!TextUtils.isEmpty(invitationUrl) && invitationUrl.contains("undefined")) {
            showErrorToast(context.getResources().getString(R.string.err_entity_invite));
            return false;
        }
        if (TextUtils.isEmpty(invitationStatus) || TextUtils.equals(invitationStatus, "disabled")) {
            showTextDialog(context.getResources().getString(R.string.jandi_invite_disabled, getOwnerName()));
            return false;
        }
        return true;
    }

    private ResTeamDetailInfo.InviteTeam getInviteTeam() {
        try {
            ResTeamDetailInfo.InviteTeam inviteTeam = teamDomainInfoModel.getTeamInfo(entityManager.getTeamId());
            return inviteTeam;
        }catch(JandiNetworkException e){
            e.printStackTrace();
            showErrorToast(context.getResources().getString(R.string.err_network));
            return null;
        }catch(Exception e){
            e.printStackTrace();
            showErrorToast(context.getResources().getString(R.string.err_entity_invite));
            return null;
        }
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
    void initProgressWheel() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(context);
        mProgressWheel.init();
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

}
