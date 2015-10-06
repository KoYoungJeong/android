package com.tosslab.jandi.app.ui.members.presenter;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.profile.ShowProfileEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

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
import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscription;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
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
                .map(new Func1<String, List<ChatChooseItem>>() {
                    @Override
                    public List<ChatChooseItem> call(String s) {
                        int entityId = view.getEntityId();
                        int type = view.getType();

                        List<ChatChooseItem> members;
                        if (type == MembersListActivity.TYPE_MEMBERS_LIST_TEAM) {
                            members = memberModel.getTeamMembers();
                        } else if (type == MembersListActivity.TYPE_MEMBERS_LIST_TOPIC) {
                            members = memberModel.getTopicMembers(entityId);
                        } else if (type == MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC) {
                            members = memberModel.getUnjoinedTopicMembers(entityId);
                        } else {
                            members = memberModel.getTeamMembers();
                        }

                        List<ChatChooseItem> chatChooseItems = new ArrayList<>();
                        Observable.from(members)
                                .filter(new Func1<ChatChooseItem, Boolean>() {
                                    @Override
                                    public Boolean call(ChatChooseItem chatChooseItem) {
                                        if (TextUtils.isEmpty(s)) {
                                            return true;
                                        } else
                                            return chatChooseItem.getName().toLowerCase().contains(s.toLowerCase());
                                    }
                                })
                                .toSortedList(new Func2<ChatChooseItem, ChatChooseItem, Integer>() {
                                    @Override
                                    public Integer call(ChatChooseItem chatChooseItem, ChatChooseItem chatChooseItem2) {

                                        int myId = EntityManager.getInstance().getMe().getId();
                                        if (chatChooseItem.getEntityId() == myId) {
                                            return -1;
                                        } else if (chatChooseItem2.getEntityId() == myId) {
                                            return 1;
                                        } else {
                                            return chatChooseItem.getName().toLowerCase()
                                                    .compareTo(chatChooseItem2.getName().toLowerCase());
                                        }
                                    }
                                })
                                .subscribe(new Action1<List<ChatChooseItem>>() {
                                    @Override
                                    public void call(List<ChatChooseItem> collection) {
                                        chatChooseItems.addAll(collection);
                                    }
                                });
                        return chatChooseItems;
                    }
                })
                .subscribe(new Action1<List<ChatChooseItem>>() {
                    @Override
                    public void call(List<ChatChooseItem> topicMembers) {
                        view.showListMembers(topicMembers);
                    }
                }, Throwable::printStackTrace);

        entityRefreshPublishSubject = PublishSubject.create();
        entityRefreshSubscriber = entityRefreshPublishSubject.throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .subscribe(integer -> initObject(), Throwable::printStackTrace);
    }

    @AfterViews
    void initViews() {
        initMemberList();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void initMemberList() {
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
    public void onDestory() {
        if (!subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        if (!entityRefreshSubscriber.isUnsubscribed()) {
            entityRefreshSubscriber.unsubscribe();
        }
    }

    @Override
    public void inviteMemberToTopic(int entityId) {
        invitationViewModel.inviteMembersToEntity(activity, entityId);
    }

    public void onEventMainThread(final RequestMoveDirectMessageEvent event) {
        EntityManager entityManager = EntityManager.getInstance();
        view.moveDirectMessageActivity(entityManager.getTeamId(), event.userId, entityManager.getEntityById(event.userId).isStarred);
    }

    public void onEventMainThread(ShowProfileEvent event) {
        MemberProfileActivity_.intent(activity)
                .memberId(event.userId)
                .from(view.getType() == MembersListActivity.TYPE_MEMBERS_LIST_TOPIC ?
                        MemberProfileActivity.EXTRA_FROM_PARTICIPANT : MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                .start();

        AnalyticsUtil.sendEvent(getScreen(), AnalyticsUtil.getProfileAction(event.userId, event.from));
    }

    @Background
    @Override
    public void inviteInBackground(List<Integer> invitedUsers, int entityId) {
        try {

            FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);

            if (entity.isPublicTopic()) {
                entityClientManager.inviteChannel(entityId, invitedUsers);
            } else if (entity.isPrivateGroup()) {
                entityClientManager.invitePrivateGroup(entityId, invitedUsers);
            }

            ResLeftSideMenu resLeftSideMenu = entityClientManager.getTotalEntitiesInfo();
            LeftSideMenuRepository.getRepository().upsertLeftSideMenu(resLeftSideMenu);
            EntityManager.getInstance().refreshEntity();
            EventBus.getDefault().post(new InvitationSuccessEvent());
            trackTopicMemberInviteSuccess(invitedUsers.size(), entityId);
            view.showInviteSucceed(invitedUsers.size());
        } catch (RetrofitError e) {
            int errorCode = e.getResponse() != null ? e.getResponse().getStatus() : -1;
            trackTopicMemberInviteFail(errorCode);
            LogUtil.e("fail to invite entity");
            view.showInviteFailed(JandiApplication.getContext().getString(R.string.err_entity_invite));
        } catch (Exception e) {
            trackTopicMemberInviteFail(-1);
            view.showInviteFailed(JandiApplication.getContext().getString(R.string.err_entity_invite));
        }
    }

    @Override
    public void initKickableMode(int entityId) {
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        int myId = EntityManager.getInstance().getMe().getId();
        view.setKickMode(entity.isMine(myId));
    }

    @Background
    @Override
    public void onKickUser(int topicId, int userEntityId) {
        int teamId = EntityManager.getInstance().getTeamId();
        view.showProgressWheel();
        try {
            memberModel.kickUser(teamId, topicId, userEntityId);
            // UI 갱신은 요청 전 성공
            // 나머지 정보는 소켓에 의해 자동 갱신 될 것으로 예상
            view.removeUser(userEntityId);
        } catch (RetrofitError retrofitError) {
            retrofitError.printStackTrace();
            // 실패시 화면 다시 갱신토록 변경
            view.refreshMemberList();
        }

        view.dismissProgressWheel();
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

    }

    public void onEvent(RetrieveTopicListEvent event) {
        if (!entityRefreshSubscriber.isUnsubscribed()) {
            entityRefreshPublishSubject.onNext(1);
        }
    }

    public void setView(View view) {
        this.view = view;
    }

    private AnalyticsValue.Screen getScreen() {
        return view.getType() == MembersListActivity.TYPE_MEMBERS_LIST_TOPIC ? AnalyticsValue.Screen.Participants : AnalyticsValue.Screen.TeamMembers;
    }
}