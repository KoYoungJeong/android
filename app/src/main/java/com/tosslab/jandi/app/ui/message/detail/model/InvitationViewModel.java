package com.tosslab.jandi.app.ui.message.detail.model;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.UnjoinedUserListAdapter;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Copied By com.tosslab.jandi.app.ui.message.model.menus.InviteCommand
 */
@EBean
public class InvitationViewModel {

    @Bean
    EntityClientManager mEntityClientManager;

    /**
     * Channel, PrivateGroup Invite
     */
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void inviteMembersToEntity(Activity activity, final int entityId) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        final Context context = activity.getApplicationContext();

        int teamMemberCountWithoutMe = EntityManager.getInstance()
                .getFormattedUsersWithoutMe()
                .size();

        if (teamMemberCountWithoutMe <= 0) {
            ColoredToast.showWarning(context, context.getString(R.string.warn_all_users_are_already_invited));
            return;
        }

        final UnjoinedUserListAdapter adapter = new UnjoinedUserListAdapter(activity.getBaseContext());

        PublishSubject<String> publishSubject = PublishSubject.create();
        Subscription subscribe = publishSubject.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                .flatMap(s -> Observable.from(getUnjoinedEntities(entityId))
                                .filter(formattedEntity -> {
                                    String searchTarget = s.toLowerCase();
                                    return formattedEntity.getName().toLowerCase()
                                            .contains(searchTarget);
                                })
                                .toSortedList((formattedEntity, formattedEntity2) -> formattedEntity
                                        .getName().compareToIgnoreCase(formattedEntity2.getName()))
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter::setUnjoinedEntities);

        /**
         * 사용자 초대를 위한 Dialog 를 보여준 뒤, 체크된 사용자를 초대한다.
         */
        View view = LayoutInflater.from(activity.getBaseContext()).inflate(R.layout.dialog_invite_to_topic, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        EditText et = (EditText) view.findViewById(R.id.et_cdp_search);

        // 현재 채널에 가입된 사용자를 제외한 초대 대상 사용자 리스트를 획득한다.
        List<FormattedEntity> unjoinedMembers = getUnjoinedEntities(entityId);

        if (unjoinedMembers.size() <= 0) {
            ColoredToast.showWarning(context, context.getString(R.string.warn_all_users_are_already_invited));
            return;
        }

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

        publishSubject.onNext("");

        et.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                publishSubject.onNext(s.toString());
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
                    inviteInBackground(context, selectedCdp, entityId);
                } else {
                    inviteFailed(context, context.getString(R.string.title_cdp_invite));
                }
            }
        });

        dialog.setOnDismissListener(dialog1 -> subscribe.unsubscribe());

        dialog.show();
    }

    private List<FormattedEntity> getUnjoinedEntities(int entityId) {
        EntityManager entityManager = EntityManager.getInstance();
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
    public void inviteInBackground(Context context, List<Integer> invitedUsers, int entityId) {
        try {

            FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

            if (entity.isPublicTopic()) {
                mEntityClientManager.inviteChannel(entityId, invitedUsers);
            } else if (entity.isPrivateGroup()) {
                mEntityClientManager.invitePrivateGroup(entityId, invitedUsers);
            }

            ResLeftSideMenu resLeftSideMenu = mEntityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(resLeftSideMenu);
            EntityManager.getInstance().refreshEntity();
            EventBus.getDefault().post(new InvitationSuccessEvent());

            trackTopicMemberInviteSuccess(invitedUsers.size(), entityId);
            inviteSucceed(context, invitedUsers.size());
        } catch (RetrofitError e) {
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            trackTopicMemberInviteFail(errorCode);
            LogUtil.e("fail to invite entity");
            inviteFailed(context, context.getString(R.string.err_entity_invite));
        } catch (Exception e) {
            trackTopicMemberInviteFail(-1);
            inviteFailed(context, context.getString(R.string.err_entity_invite));
        }
    }

    private void trackTopicMemberInviteSuccess(int memberCount, int entityId) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicMemberInvite)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, true)
                        .property(PropertyKey.TopicId, entityId)
                        .property(PropertyKey.MemberCount, memberCount)
                        .build());

        GoogleAnalyticsUtil.sendEvent(Event.TopicMemberInvite.name(), "ResponseSuccess");
    }

    private void trackTopicMemberInviteFail(int errorCode) {
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.TopicMemberInvite)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                        .property(PropertyKey.ResponseSuccess, false)
                        .property(PropertyKey.ErrorCode, errorCode)
                        .build());

        GoogleAnalyticsUtil.sendEvent(Event.TopicMemberInvite.name(), "ResponseFail");
    }

    @UiThread
    public void inviteSucceed(Context context, int memberSize) {
        String rawString = context.getString(R.string.jandi_message_invite_entity);
        String formatString = String.format(rawString, memberSize);
        ColoredToast.show(context, formatString);
    }

    @UiThread
    public void inviteFailed(Context context, String errMessage) {
        ColoredToast.showError(context, errMessage);
    }

}
