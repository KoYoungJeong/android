package com.tosslab.jandi.app.ui.message.detail.model;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.UnjoinedUserListAdapter;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.entity.JandiEntityDatabaseManager;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.maintab.topic.model.EntityComparator;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.Collections;
import java.util.List;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;

/**
 * Copied By com.tosslab.jandi.app.ui.message.model.menus.InviteCommand
 */
@EBean
public class InvitationViewModel {

    @Bean
    InvitationDialogExecutor invitationDialogExecutor;
    @Bean
    EntityClientManager mEntityClientManager;

    private Context context;
    private int entityId;

    public void initData(Context context, int entityId) {
        this.context = context;
        this.entityId = entityId;
    }

    public void invite() {
        inviteMembersToEntity();
    }

    /**
     * Channel, PrivateGroup Invite
     */
    @UiThread
    public void inviteMembersToEntity() {

        int teamMemberCountWithoutMe = EntityManager.getInstance(context)
                .getFormattedUsersWithoutMe()
                .size();

        if (teamMemberCountWithoutMe <= 0) {
            invitationDialogExecutor.execute();
            return;
        }

        /**
         * 사용자 초대를 위한 Dialog 를 보여준 뒤, 체크된 사용자를 초대한다.
         */
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);

        // 현재 채널에 가입된 사용자를 제외한 초대 대상 사용자 리스트를 획득한다.
        List<FormattedEntity> unjoinedMembers = getUnjoinedEntities(context);

        if (unjoinedMembers.size() <= 0) {
            ColoredToast.showWarning(context, context.getString(R.string.warn_all_users_are_already_invited));
            return;
        }
        Collections.sort(unjoinedMembers, new EntityComparator());

        final UnjoinedUserListAdapter adapter = new UnjoinedUserListAdapter(context, unjoinedMembers);
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

        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        dialog.setTitle(R.string.title_cdp_invite);
        dialog.setView(view);
        dialog.setPositiveButton(R.string.menu_entity_invite, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                List<Integer> selectedCdp = adapter.getSelectedUserIds();
                if (selectedCdp != null && !selectedCdp.isEmpty()) {
                    inviteInBackground(context, selectedCdp);
                } else {
                    inviteFailed(context.getString(R.string.title_cdp_invite));
                }
            }
        });
        dialog.show();
    }

    private List<FormattedEntity> getUnjoinedEntities(Context context) {
        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity entity = entityManager.getEntityById(entityId);
        int entityType = entity.isPublicTopic() ? JandiConstants.TYPE_PUBLIC_TOPIC : entity
                .isPrivateGroup() ? JandiConstants.TYPE_PRIVATE_TOPIC : JandiConstants.TYPE_DIRECT_MESSAGE;
        List<FormattedEntity> unjoinedMembersOfEntity = entityManager.getUnjoinedMembersOfEntity(entityId, entityType);

        for (int idx = unjoinedMembersOfEntity.size() - 1; idx >= 0; idx--) {
            FormattedEntity formattedEntity = unjoinedMembersOfEntity.get(idx);
            if (!TextUtils.equals(formattedEntity.getUser().status, "enabled")) {
                unjoinedMembersOfEntity.remove(idx);
            }
        }

        return unjoinedMembersOfEntity;
    }

    @Background
    public void inviteInBackground(Context context, List<Integer> invitedUsers) {
        try {

            FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);

            if (entity.isPublicTopic()) {
                mEntityClientManager.inviteChannel(entityId, invitedUsers);
            } else if (entity.isPrivateGroup()) {
                mEntityClientManager.invitePrivateGroup(entityId, invitedUsers);
            }

            ResLeftSideMenu resLeftSideMenu = mEntityClientManager.getTotalEntitiesInfo();
            JandiEntityDatabaseManager.getInstance(this.context).upsertLeftSideMenu(resLeftSideMenu);
            EntityManager.getInstance(this.context).refreshEntity(resLeftSideMenu);
            EventBus.getDefault().post(new InvitationSuccessEvent());


            inviteSucceed(invitedUsers.size());
        } catch (RetrofitError e) {
            LogUtil.e("fail to invite entity");
            inviteFailed(this.context.getString(R.string.err_entity_invite));
        } catch (Exception e) {
            inviteFailed(this.context.getString(R.string.err_entity_invite));
        }
    }

    @UiThread
    public void inviteSucceed(int memberSize) {
        String rawString = context.getString(R.string.jandi_message_invite_entity);
        String formatString = String.format(rawString, memberSize);
        ColoredToast.show(context, formatString);
    }

    @UiThread
    public void inviteFailed(String errMessage) {
        ColoredToast.showError(context, errMessage);
    }

    public boolean isTopicOwner(Context context, int entityId) {
        return EntityManager.getInstance(context).isMyTopic(entityId);
    }
}
