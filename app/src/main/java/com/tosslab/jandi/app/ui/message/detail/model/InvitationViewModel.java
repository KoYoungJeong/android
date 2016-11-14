package com.tosslab.jandi.app.ui.message.detail.model;

import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.lists.entities.UnjoinedUserListAdapter;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicMemberInvite;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
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
    public void inviteMembersToEntity(Activity activity, final long entityId) {
        if (activity == null || activity.isFinishing()) {
            return;
        }

        final Context context = activity.getApplicationContext();

        int teamMemberCountWithoutMe = TeamInfoLoader.getInstance().getUserList().size() - 1;

        if (teamMemberCountWithoutMe <= 0) {
            ColoredToast.showWarning(context.getString(R.string.warn_all_users_are_already_invited));
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
                        .toSortedList((formattedEntity, formattedEntity2) -> StringCompareUtil.compare(formattedEntity.getName(), formattedEntity2.getName()))
                )
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter::setUnjoinedEntities, Throwable::printStackTrace);

        /**
         * 사용자 초대  위한 Dialog    여준 뒤, 체크된 사용자  초대한다.
         */
        View view = LayoutInflater.from(activity.getBaseContext()).inflate(R.layout.dialog_invite_to_topic, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        EditText et = (EditText) view.findViewById(R.id.et_cdp_search);

        // 현재 채널에 가입된 사용자를 제외한 초대 대상 사용자 리스트를 획득한다.
        List<User> unjoinedMembers = getUnjoinedEntities(entityId);

        if (unjoinedMembers.size() <= 0) {
            ColoredToast.showWarning(context.getString(R.string.warn_all_users_are_already_invited));
            return;
        }

        lv.setAdapter(adapter);
        lv.setOnItemClickListener((parent, view1, position, id) -> {
            UnjoinedUserListAdapter userListAdapter = (UnjoinedUserListAdapter) parent.getAdapter();
            User item = userListAdapter.getItem(position);
            userListAdapter.toggleChecked(item);

            userListAdapter.notifyDataSetChanged();
        });

        publishSubject.onNext("");

        et.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                publishSubject.onNext(s.toString());
            }
        });

        AlertDialog.Builder dialog = new AlertDialog.Builder(activity,
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        dialog.setTitle(R.string.title_cdp_invite);
        dialog.setView(view);
        dialog.setPositiveButton(R.string.menu_entity_invite, (dialogInterface, i) -> {
            List<Long> selectedCdp = adapter.getSelectedUserIds();
            if (selectedCdp != null && !selectedCdp.isEmpty()) {
                inviteInBackground(context, selectedCdp, entityId);
            } else {
                inviteFailed(context, context.getString(R.string.title_cdp_invite));
            }
        });

        dialog.setOnDismissListener(dialog1 -> subscribe.unsubscribe());

        dialog.show();
    }

    private List<User> getUnjoinedEntities(long entityId) {

        if (TeamInfoLoader.getInstance().isTopic(entityId)) {
            TopicRoom topic = TeamInfoLoader.getInstance().getTopic(entityId);

            Collection<Long> members = topic.getMembers();

            List<User> userList = TeamInfoLoader.getInstance().getUserList();

            return Observable.from(userList)
                    .filter(user -> !members.contains(user.getId()))
                    .filter(User::isEnabled)
                    .collect((Func0<ArrayList<User>>) ArrayList::new, ArrayList::add)
                    .toBlocking()
                    .firstOrDefault(new ArrayList<>());

        } else {
            return new ArrayList<>();
        }
    }

    @Background
    public void inviteInBackground(Context context, List<Long> invitedUsers, long entityId) {
        try {

            TopicRoom entity = TeamInfoLoader.getInstance().getTopic(entityId);

            if (entity.isPublicTopic()) {
                mEntityClientManager.inviteChannel(entityId, invitedUsers);
            } else {
                mEntityClientManager.invitePrivateGroup(entityId, invitedUsers);
            }

            TopicRepository.getInstance().addMember(entityId, invitedUsers);
            TeamInfoLoader.getInstance().refresh();

            EventBus.getDefault().post(new InvitationSuccessEvent());

            SprinklrTopicMemberInvite.sendLog(entityId, invitedUsers.size());
            inviteSucceed(context, invitedUsers.size());
        } catch (RetrofitException e) {
            int errorCode = e.getStatusCode();
            SprinklrTopicMemberInvite.sendFailLog(errorCode);
            LogUtil.e("fail to invite entity");
            inviteFailed(context, context.getString(R.string.err_entity_invite));
        } catch (Exception e) {
            SprinklrTopicMemberInvite.sendFailLog(-1);
            inviteFailed(context, context.getString(R.string.err_entity_invite));
        }
    }

    @UiThread
    public void inviteSucceed(Context context, int memberSize) {
        String rawString = context.getString(R.string.jandi_message_invite_entity);
        String formatString = String.format(rawString, memberSize);
        ColoredToast.show(formatString);
    }

    @UiThread
    public void inviteFailed(Context context, String errMessage) {
        ColoredToast.show(errMessage);
    }

}
