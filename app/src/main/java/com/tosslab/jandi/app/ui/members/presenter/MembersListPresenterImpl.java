package com.tosslab.jandi.app.ui.members.presenter;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.events.team.TeamJoinEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicMemberInvite;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Tee on 15. x. x..
 */

@EBean
public class MembersListPresenterImpl implements MembersListPresenter {

    @RootContext
    AppCompatActivity activity;

    @Bean
    MembersModel memberModel;

    @Bean
    EntityClientManager entityClientManager;

    @Bean
    InvitationViewModel invitationViewModel;

    private View view;
    private PublishSubject<String> objectPublishSubject;
    private PublishSubject<Integer> entityRefreshPublishSubject;
    private Subscription subscribe;
    private Subscription entityRefreshSubscriber;

    @AfterInject
    void initObject() {
        objectPublishSubject = PublishSubject.create();
        subscribe = objectPublishSubject
                .throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(s -> {
                    List<ChatChooseItem> members = getChatChooseItems();
                    return getFilteredChatChooseItems(s, members);
                })
                .observeOn(AndroidSchedulers.mainThread())
                .doOnNext(topicMembers -> {
                    view.dismissProgressWheel();
                    view.showListMembers(topicMembers);
                })
                .subscribe();

        entityRefreshPublishSubject = PublishSubject.create();
        entityRefreshSubscriber = entityRefreshPublishSubject.throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .subscribe(integer -> initObject(), Throwable::printStackTrace);
    }

    public List<ChatChooseItem> getFilteredChatChooseItems(String s, List<ChatChooseItem> members) {
        List<ChatChooseItem> chatChooseItems = new ArrayList<>();
        Observable.from(members)
                .filter(chatChooseItem -> {
                    if (TextUtils.isEmpty(s)) {
                        return true;
                    } else
                        return chatChooseItem.getName().toLowerCase().contains(s.toLowerCase());
                })
                .toSortedList((lhs, rhs) -> {
                    if (lhs.isBot()) {
                        return -1;
                    } else if (rhs.isBot()) {
                        return 1;
                    } else {

                        String lhsName, rhsName;
                        if (!lhs.isInactive()) {
                            lhsName = lhs.getName();
                        } else {
                            lhsName = lhs.getEmail();
                        }

                        if (!rhs.isInactive()) {
                            rhsName = rhs.getName();
                        } else {
                            rhsName = rhs.getEmail();
                        }

                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());

                    }
                })
                .subscribe(chatChooseItems::addAll, Throwable::printStackTrace);
        return chatChooseItems;
    }

    public List<ChatChooseItem> getChatChooseItems() {
        long entityId = view.getEntityId();
        int type = view.getType();
        List<ChatChooseItem> members;
        if (type == MembersListActivity.TYPE_MEMBERS_LIST_TEAM) {
            members = memberModel.getTeamMembers();
        } else if (type == MembersListActivity.TYPE_MEMBERS_LIST_TOPIC
                || type == MembersListActivity.TYPE_ASSIGN_TOPIC_OWNER) {
            members = memberModel.getTopicMembers(entityId);
        } else if (type == MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC) {
            members = memberModel.getUnjoinedTopicMembers(entityId);
        } else {
            members = memberModel.getTeamMembers();
        }
        return members;
    }

    @AfterViews
    void initViews() {
        initMemberList();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void initMemberList() {
        view.showProgressWheel();
        objectPublishSubject.onNext(view.getSearchText());
    }

    @Override
    public void onEventBusRegister() {
        EventBus.getDefault().register(this);
    }

    @Override
    public void onEventBusUnregister() {
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSearch(CharSequence text) {
        if (!subscribe.isUnsubscribed()) {
            objectPublishSubject.onNext(text.toString());
        }
    }

    @Override
    public void onDestroy() {
        if (!subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        if (!entityRefreshSubscriber.isUnsubscribed()) {
            entityRefreshSubscriber.unsubscribe();
        }
    }

    @Override
    public void inviteMemberToTopic(long entityId) {
        invitationViewModel.inviteMembersToEntity(activity, entityId);
    }

    public void onEventMainThread(final RequestMoveDirectMessageEvent event) {

        view.moveDirectMessageActivity(TeamInfoLoader.getInstance().getTeamId(), event.userId);
    }

    public void onEventMainThread(ShowProfileEvent event) {
        MemberProfileActivity_.intent(activity)
                .memberId(event.userId)
                .from(view.getType() == MembersListActivity.TYPE_MEMBERS_LIST_TOPIC ?
                        MemberProfileActivity.EXTRA_FROM_PARTICIPANT : MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                .start();

        AnalyticsUtil.sendEvent(getScreen(), AnalyticsValue.Action.ViewProfile);
    }

    public void onEvent(TeamJoinEvent event) {
        objectPublishSubject.onNext(view.getSearchText());
    }

    public void onEvent(TeamLeaveEvent event) {
        objectPublishSubject.onNext(view.getSearchText());
    }

    @Background
    @Override
    public void inviteInBackground(List<Long> invitedUsers, long entityId) {
        try {


            if (TeamInfoLoader.getInstance().isPublicTopic(entityId)) {
                entityClientManager.inviteChannel(entityId, invitedUsers);
            } else {
                entityClientManager.invitePrivateGroup(entityId, invitedUsers);
            }

            TopicRepository.getInstance().addMember(entityId, invitedUsers);

            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new InvitationSuccessEvent());
            SprinklrTopicMemberInvite.sendLog(entityId, invitedUsers.size());
            view.showInviteSucceed(invitedUsers.size());
        } catch (RetrofitException e) {
            int errorCode = e.getStatusCode();
            SprinklrTopicMemberInvite.sendFailLog(errorCode);
            LogUtil.e("fail to invite entity");
            view.showInviteFailed(JandiApplication.getContext().getString(R.string.err_entity_invite));
        } catch (Exception e) {
            SprinklrTopicMemberInvite.sendFailLog(-1);
            view.showInviteFailed(JandiApplication.getContext().getString(R.string.err_entity_invite));
        }
    }

    @Override
    public void initKickableMode(long entityId) {
        boolean topicOwner = memberModel.isMyTopic(entityId);
        boolean teamOwner = memberModel.isTeamOwner();
        boolean isDefaultTopic = TeamInfoLoader.getInstance().getDefaultTopicId() == entityId;
        // 내 토픽이되 기본 토픽이 아니어야 함
        view.setKickMode((topicOwner || teamOwner) && !isDefaultTopic);
    }

    @Override
    public void onKickMemberClick(long topicId, ChatChooseItem item) {
        boolean isTopicOwner = memberModel.isTopicOwner(topicId, item.getEntityId());
        if (isTopicOwner) {
            view.showNeedToAssignTopicOwnerDialog();
            return;
        }

        view.showKickDialog(item.getName(), item.getPhotoUrl(), item.getEntityId());
    }

    @Background
    @Override
    public void onKickUser(long topicId, long userEntityId) {
        if (!NetworkCheckUtil.isConnected()) {
            view.showKickFailToast();
            return;
        }

        long teamId = TeamInfoLoader.getInstance().getTeamId();
        view.showProgressWheel();
        try {
            memberModel.kickUser(teamId, topicId, userEntityId);
            // UI 갱신은 요청 전 성공
            // 나머지 정보는 소켓에 의해 자동 갱신 될 것으로 예상되나 메모리상 정보도 갱신하도록 함
            memberModel.removeMember(topicId, userEntityId);
            EventBus.getDefault().post(new RetrieveTopicListEvent());
            view.removeUser(userEntityId);
            view.showKickSuccessToast();
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
            // 실패시 화면 다시 갱신토록 변경
            view.refreshMemberList();
            view.showKickFailToast();
        }

        view.dismissProgressWheel();
    }

    @Override
    public void onMemberClickForAssignOwner(long topicId, final ChatChooseItem item) {
        if (TeamInfoLoader.getInstance().isBot(item.getEntityId())) {
            return;
        }

        if (memberModel.isTopicOwner(topicId, item.getEntityId())) {
            view.showAlreadyTopicOwnerToast();
            return;
        }

        view.showConfirmAssignTopicOwnerDialog(item.getName(), item.getPhotoUrl(), item.getEntityId());
    }

    @Background
    @Override
    public void onAssignToTopicOwner(long topicId, long memberId) {
        if (!NetworkCheckUtil.isConnected()) {
            view.showAssignTopicOwnerFailToast();
            return;
        }

        view.showProgressWheel();

        long teamId = TeamInfoLoader.getInstance().getTeamId();
        try {
            memberModel.assignToTopicOwner(teamId, topicId, memberId);
            view.dismissProgressWheel();

            view.showAssignTopicOwnerSuccessToast();

            view.setResultAndFinish(memberId);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
            view.dismissProgressWheel();

            view.showAssignTopicOwnerFailToast();

            view.setResultAndFinish(-1);
        }
    }

    public void setView(View view) {
        this.view = view;
    }

    private AnalyticsValue.Screen getScreen() {
        return view.getType() == MembersListActivity.TYPE_MEMBERS_LIST_TOPIC ? AnalyticsValue.Screen.Participants : AnalyticsValue.Screen.TeamMembers;
    }
}