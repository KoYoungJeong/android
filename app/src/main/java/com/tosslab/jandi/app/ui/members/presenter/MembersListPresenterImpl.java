package com.tosslab.jandi.app.ui.members.presenter;

import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.tosslab.jandi.app.dialogs.profile.UserInfoDialogFragment_;
import com.tosslab.jandi.app.events.RequestMoveDirectMessageEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.profile.ProfileDetailEvent;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.to.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import com.tosslab.jandi.app.ui.message.detail.model.InvitationViewModel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.GoogleAnalyticsUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
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
                        if (type == MembersListActivity.TYPE_MEMBERS_LIST_TEAM) {
                            members = memberModel.getTeamMembers();
                        } else {
                            members = memberModel.getTopicMembers(entityId);
                        }

                        List<ChatChooseItem> chatChooseItems = new ArrayList<ChatChooseItem>();
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
                    public void call(List<ChatChooseItem> topicMembers) {view.showListMembers(topicMembers);}
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

    public void onEventMainThread(ProfileDetailEvent event) {
        int entityId = event.getEntityId();
        UserInfoDialogFragment_.builder()
                .entityId(entityId)
                .build()
                .show(activity.getSupportFragmentManager(), "dialog");
        GoogleAnalyticsUtil.sendEvent(AnalyticsValue.Screen.Participants, AnalyticsValue.Action.ViewProfile);
    }

    public void onEvent(RetrieveTopicListEvent event) {
        initMemberList();
    }

    public void setView(View view) {
        this.view = view;
    }
}