package com.tosslab.jandi.app.ui.message.model.menus;

import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.UnjoinedUserListAdapter;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.topic.model.EntityComparator;
import com.tosslab.jandi.app.ui.message.to.ChattingInfomations;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;

import java.util.Collections;
import java.util.List;

<<<<<<< HEAD
import retrofit.RetrofitError;

=======
>>>>>>> origin/entitymanager_thread_safe
/**
 * Created by Steve SeongUg Jung on 14. 12. 10..
 */
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
        entityManager = EntityManager.getInstance(activity);

        progressWheel = new ProgressWheel(activity);
        progressWheel.init();
    }

    @Override
    public void execute(MenuItem menuItem) {
        inviteMembersToEntity();
    }

    /**
     * Channel, PrivateGroup Invite
     */
    @UiThread
    public void inviteMembersToEntity() {

        int teamMemberCountWithoutMe = entityManager.getFormattedUsersWithoutMe().size();

        if (teamMemberCountWithoutMe <= 0) {
            invitationDialogExecutor.execute();
            return;
        }

        /**
         * 사용자 초대를 위한 Dialog 를 보여준 뒤, 체크된 사용자를 초대한다.
         */
        View view = LayoutInflater.from(activity).inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);

        // 현재 채널에 가입된 사용자를 제외한 초대 대상 사용자 리스트를 획득한다.
        List<FormattedEntity> unjoinedMembers = getUnjoinedEntities();

        if (unjoinedMembers.size() <= 0) {
            ColoredToast.showWarning(activity, activity.getString(R.string.warn_all_users_are_already_invited));
            return;
        }
        Collections.sort(unjoinedMembers, new EntityComparator());

        final UnjoinedUserListAdapter adapter = new UnjoinedUserListAdapter(activity, unjoinedMembers);
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                UnjoinedUserListAdapter userListAdapter = (UnjoinedUserListAdapter) parent.getAdapter();
                FormattedEntity item = userListAdapter.getItem(position);
                item.isSelectedToBeJoined = !item.isSelectedToBeJoined;

                userListAdapter.notifyDataSetChanged();
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
        dialog.setTitle(R.string.title_cdp_invite);
        dialog.setView(view);
        dialog.setPositiveButton(R.string.menu_entity_invite, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<Integer> selectedCdp = adapter.getSelectedUserIds();
                if (selectedCdp != null && !selectedCdp.isEmpty()) {
                    inviteInBackground(selectedCdp);
                } else {
                    inviteFailed(activity.getString(R.string.title_cdp_invite));
                }
            }
        });
        dialog.show();
    }

    private List<FormattedEntity> getUnjoinedEntities() {
        List<FormattedEntity> unjoinedMembersOfEntity = entityManager.getUnjoinedMembersOfEntity(
                chattingInfomations.entityId,
                chattingInfomations.entityType);

        for (int idx = unjoinedMembersOfEntity.size() - 1; idx >= 0; idx--) {
            FormattedEntity formattedEntity = unjoinedMembersOfEntity.get(idx);
            if (!TextUtils.equals(formattedEntity.getUser().status, "enabled")) {
                unjoinedMembersOfEntity.remove(idx);
            }
        }

        return unjoinedMembersOfEntity;
    }

    @Background
    public void inviteInBackground(List<Integer> invitedUsers) {
        try {
            if (chattingInfomations.isPublicTopic()) {
                mEntityClientManager.inviteChannel(
                        chattingInfomations.entityId, invitedUsers);
            } else if (chattingInfomations.isPrivateTopic()) {
                mEntityClientManager.invitePrivateGroup(
                        chattingInfomations.entityId, invitedUsers);
            }

            ResLeftSideMenu resLeftSideMenu = mEntityClientManager.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(activity).upsertLeftSideMenu(resLeftSideMenu);
            EntityManager.getInstance(activity).refreshEntity(resLeftSideMenu);


            inviteSucceed(invitedUsers.size());
        } catch (RetrofitError e) {
            LogUtil.e("fail to invite entity");
            inviteFailed(activity.getString(R.string.err_entity_invite));
        } catch (Exception e) {
            inviteFailed(activity.getString(R.string.err_entity_invite));
        }
    }

    @UiThread
    public void inviteSucceed(int memberSize) {
        String rawString = activity.getString(R.string.jandi_message_invite_entity);
        String formatString = String.format(rawString, memberSize);
        ColoredToast.show(activity, formatString);
    }

    @UiThread
    public void inviteFailed(String errMessage) {
        ColoredToast.showError(activity, errMessage);
    }

}
