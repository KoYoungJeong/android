package com.tosslab.jandi.app.ui.message.model.menus;

import android.content.ClipboardManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel_;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;

/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
@Deprecated
@EBean
class InviteCommand implements MenuCommand {

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;
    @SystemService
    ClipboardManager clipboardManager;
    @Bean
    InvitationDialogExecutor invitationDialogExecutor;
    private AppCompatActivity activity;
    private EntityClientManager mEntityClientManager;
    private ChattingInfomations chattingInfomations;
    private EntityManager entityManager;
    private ProgressWheel progressWheel;

    void initData(AppCompatActivity activity, EntityClientManager mEntityClientManager, ChattingInfomations chattingInfomations) {
        this.activity = activity;
        this.mEntityClientManager = mEntityClientManager;
        this.chattingInfomations = chattingInfomations;
        entityManager = EntityManager.getInstance();

        progressWheel = new ProgressWheel(activity);
    }

    @Override
    public void execute(MenuItem menuItem) {
        inviteMembersToEntity();
    }

    /**
     * Channel, PrivateGroup Invite
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void inviteMembersToEntity() {

        int teamMemberCountWithoutMe = entityManager.getFormattedUsersWithoutMe().size();

        if (teamMemberCountWithoutMe <= 0) {
            invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_TOPIC_CHAT);
            invitationDialogExecutor.execute();
        } else {
            InvitationViewModel invitationViewModel = InvitationViewModel_.getInstance_(activity);
            invitationViewModel.inviteMembersToEntity(activity, chattingInfomations.entityId);
        }

    }
}
