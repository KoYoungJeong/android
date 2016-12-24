package com.tosslab.jandi.app.ui.members.presenter;

import android.text.TextUtils;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.model.MembersModel;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrTopicMemberInvite;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Tee on 15. x. x..
 */

public class MembersListPresenterImpl implements MembersListPresenter {


    MembersModel memberModel;
    EntityClientManager entityClientManager;
    private View view;

    private PublishSubject<String> objectPublishSubject;
    private PublishSubject<Integer> entityRefreshPublishSubject;
    private Subscription subscribe;
    private Subscription entityRefreshSubscriber;

    @Inject
    public MembersListPresenterImpl(View view,
                                    MembersModel memberModel,
                                    EntityClientManager entityClientManager) {
        this.memberModel = memberModel;
        this.view = view;
        this.entityClientManager = entityClientManager;

        initObject();
    }

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
        } else {
            members = memberModel.getTeamMembers();
        }
        return members;
    }

    @Override
    public void onInit() {
        view.showProgressWheel();
        objectPublishSubject.onNext("");
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
        view.inviteMember(entityId);
    }

    @Override
    public void inviteInBackground(List<Long> invitedUsers, long entityId) {

        Completable.fromCallable(() -> {
            if (TeamInfoLoader.getInstance().isPublicTopic(entityId)) {
                entityClientManager.inviteChannel(entityId, invitedUsers);
            } else {
                entityClientManager.invitePrivateGroup(entityId, invitedUsers);
            }

            TopicRepository.getInstance().addMember(entityId, invitedUsers);

            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new InvitationSuccessEvent());
            SprinklrTopicMemberInvite.sendLog(entityId, invitedUsers.size());

            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.showInviteSucceed(invitedUsers.size());
                }, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        int errorCode = e.getStatusCode();
                        SprinklrTopicMemberInvite.sendFailLog(errorCode);
                        LogUtil.e("fail to invite entity");
                        view.showInviteFailed(JandiApplication.getContext().getString(R.string.err_entity_invite));
                    } else {
                        SprinklrTopicMemberInvite.sendFailLog(-1);
                        view.showInviteFailed(JandiApplication.getContext().getString(R.string.err_entity_invite));
                    }
                });
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

        if (TeamInfoLoader.getInstance().getUser(item.getEntityId()).getLevel() != Level.Guest) {
            view.showDialogKick(item.getName(), item.getPhotoUrl(), item.getEntityId());
        } else {
            long guestId = item.getEntityId();
            Observable<Integer> share = Observable.from(TeamInfoLoader.getInstance().getTopicList())
                    .filter(it -> it.getMembers().contains(guestId))
                    .map(it -> 1)
                    .defaultIfEmpty(0)
                    .reduce((integer, integer2) -> integer + integer2)
                    .share();

            share.filter(it -> it > 1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(integer -> {
                        view.showDialogKick(item.getName(), item.getPhotoUrl(), item.getEntityId());
                    }, Throwable::printStackTrace);

            share.filter(it -> it <= 1)
                    .observeOn(AndroidSchedulers.mainThread())
                    .doOnNext(it -> view.showProgressWheel())
                    .observeOn(Schedulers.io())
                    .concatMap(it -> {
                        try {
                            return Observable.just(memberModel.getMemberInfo(TeamInfoLoader.getInstance().getTeamId(), item.getEntityId()));
                        } catch (RetrofitException e) {
                            return Observable.error(e);
                        }
                    }).observeOn(AndroidSchedulers.mainThread())
                    .doOnUnsubscribe(() -> view.dismissProgressWheel())
                    .subscribe(memberInfo -> {
                        if (memberInfo.getJoinTopics().size() > 1) {
                            view.showDialogKick(item.getName(), item.getPhotoUrl(), item.getEntityId());
                        } else {
                            view.showDialogGuestKick(item.getEntityId());
                        }
                    }, Throwable::printStackTrace);

        }

    }

    @Override
    public void onKickUser(long topicId, long userEntityId) {
        if (!NetworkCheckUtil.isConnected()) {
            view.showKickFailToast();
            return;
        }

        long teamId = TeamInfoLoader.getInstance().getTeamId();
        view.showProgressWheel();

        Completable.fromCallable(() -> {
            memberModel.kickUser(teamId, topicId, userEntityId);
            // UI 갱신은 요청 전 성공
            // 나머지 정보는 소켓에 의해 자동 갱신 될 것으로 예상되나 메모리상 정보도 갱신하도록 함
            memberModel.removeMember(topicId, userEntityId);
            EventBus.getDefault().post(new RetrieveTopicListEvent());
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.removeUser(userEntityId);
                    view.showKickSuccessToast();
                    view.dismissProgressWheel();
                }, t -> {
                    t.printStackTrace();
                    view.refreshMemberList();
                    view.showKickFailToast();
                    view.dismissProgressWheel();
                });


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

    @Override
    public void onAssignToTopicOwner(long topicId, long memberId) {
        if (!NetworkCheckUtil.isConnected()) {
            view.showAssignTopicOwnerFailToast();
            return;
        }

        view.showProgressWheel();

        Completable.fromCallable(() -> {
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            memberModel.assignToTopicOwner(teamId, topicId, memberId);

            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.dismissProgressWheel();
                    view.showAssignTopicOwnerSuccessToast();
                    view.setResultAndFinish(memberId);
                }, t -> {
                    LogUtil.e(Log.getStackTraceString(t));
                    view.dismissProgressWheel();
                    view.showAssignTopicOwnerFailToast();
                    view.setResultAndFinish(-1);
                });

    }

}