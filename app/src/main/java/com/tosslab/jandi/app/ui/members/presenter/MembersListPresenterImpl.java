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
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;
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
    private Subscription subscribe;

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
                        members = memberModel.getTeamMembers();
                        if (type == MembersListActivity.TYPE_MEMBERS_LIST_TEAM) {
                            members = memberModel.getTeamMembers();
                        } else if (type == MembersListActivity.TYPE_MEMBERS_LIST_TOPIC) {
                            members = memberModel.getTopicMembers(entityId);
                        } else if (type == MembersListActivity.TYPE_MEMBERS_JOINABLE_TOPIC) {
                            members = memberModel.getUnjoinedTopicMembers(entityId);
                        }

                        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();
                        Observable.from(members)
                                .filter(chatChooseItem -> {
                                    if (TextUtils.isEmpty(s)) {
                                        return true;
                                    } else
                                        return chatChooseItem.getName().toLowerCase().contains(s.toLowerCase());
                                })
                                .toSortedList((chatChooseItem, chatChooseItem2) ->
                                        chatChooseItem.getName().toLowerCase()
                                                .compareTo(chatChooseItem2.getName().toLowerCase()))
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
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        throwable.printStackTrace();
                    }
                });
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
        subscribe.unsubscribe();
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
                .start();
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

    public void onEvent(RetrieveTopicListEvent event) {
        initMemberList();
    }

    public void setView(View view) {
        this.view = view;
    }
}