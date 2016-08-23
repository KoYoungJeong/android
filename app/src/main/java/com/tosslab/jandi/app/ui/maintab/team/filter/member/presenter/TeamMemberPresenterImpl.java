package com.tosslab.jandi.app.ui.maintab.team.filter.member.presenter;

import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.local.orm.repositories.search.MemberRecentKeywordRepository;
import com.tosslab.jandi.app.network.client.privatetopic.GroupApi;
import com.tosslab.jandi.app.network.client.publictopic.ChannelApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.domain.TeamMemberItem;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class TeamMemberPresenterImpl implements TeamMemberPresenter {

    private final View view;
    private final TeamMemberDataModel teamMemberDataModel;
    private final Lazy<ChannelApi> channelApi;
    private final Lazy<GroupApi> groupApi;
    private boolean selectMode;

    private BehaviorSubject<String> filterSubject;
    private Subscription filterSubscription;
    private long roomId = -1;

    private ToggleCollector toggledIds;


    @Inject
    public TeamMemberPresenterImpl(View view, TeamMemberDataModel teamMemberDataModel, Lazy<ChannelApi> channelApi, Lazy<GroupApi> groupApi, ToggleCollector toggledIds) {
        this.view = view;
        this.teamMemberDataModel = teamMemberDataModel;
        this.channelApi = channelApi;
        this.groupApi = groupApi;
        this.toggledIds = toggledIds;
    }

    @Override
    public void onCreate() {
        filterSubject = BehaviorSubject.create("");
        filterSubscription = filterSubject
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .map(String::toLowerCase)
                .concatMap(it -> Observable.from(TeamInfoLoader.getInstance().getUserList())
                        .filter(User::isEnabled)
                        .filter(user -> user.getName().toLowerCase().contains(it))
                        .filter(user -> {
                            if (!selectMode || roomId < 0) {
                                return true;
                            }

                            Room room = TeamInfoLoader.getInstance().getRoom(roomId);
                            if (room != null) {
                                return !room.getMembers().contains(user.getId());
                            }

                            return true;
                        })
                        .map(TeamMemberItem::new)
                        .compose(sort()))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> {
                    teamMemberDataModel.clear();
                    teamMemberDataModel.addAll(users);
                    view.refreshDataView();
                });
    }

    private Observable.Transformer<? super TeamMemberItem, ? extends List<TeamMemberItem>> sort() {
        return userObservable -> userObservable.toSortedList((entity, entity2) -> {
                    if (selectMode) {
                        return StringCompareUtil.compare(entity.getName(), entity2.getName());
                    } else {
                        if (entity.getChatChooseItem().isStarred()) {
                            return -1;
                        } else if (entity2.getChatChooseItem().isStarred()) {
                            return 1;
                        } else {
                            return StringCompareUtil.compare(entity.getName(), entity2.getName());
                        }
                    }
                }
        );
    }

    @Override
    public void onDestroy() {
        filterSubject.onCompleted();
        filterSubscription.unsubscribe();
    }

    @Override
    public void onItemClick(int position) {

        String value = filterSubject.getValue();
        if (value.length() > 0) {
            MemberRecentKeywordRepository.getInstance().upsertKeyword(value);
        }

        TeamMemberItem item = teamMemberDataModel.getItem(position);
        long userId = item.getChatChooseItem().getEntityId();
        if (!selectMode) {
            view.moveProfile(userId);
        } else {

            if (roomId > 0) {
                if (toggledIds.containsId(userId)) {
                    toggledIds.removeId(userId);
                } else {
                    toggledIds.addId(userId);
                }

                view.updateToggledUser(toggledIds.count());
                view.refreshDataView();
            } else {

                onUserSelect(userId);
            }


        }

    }

    @Override
    public void addToggledUser(long[] users) {

        view.refreshDataView();
        view.updateToggledUser(toggledIds.count());
    }

    @Override
    public void addToggleOfAll() {
        for (int idx = 0, size = teamMemberDataModel.getSize(); idx < size; idx++) {
            ChatChooseItem chatChooseItem = teamMemberDataModel.getItem(idx).getChatChooseItem();
            toggledIds.addId(chatChooseItem.getEntityId());
        }

        view.refreshDataView();
        view.updateToggledUser(toggledIds.count());
    }

    @Override
    public void onUserSelect(long userId) {
        long roomId = TeamInfoLoader.getInstance().getChatId(userId);
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long lastLinkId;
        if (roomId > 0) {
            lastLinkId = TeamInfoLoader.getInstance().getRoom(roomId).getLastLinkId();
        } else {
            lastLinkId = -1;
        }


        view.moveDirectMessage(teamId, userId, roomId, lastLinkId);
    }

    @Override
    public void clearToggle() {
        toggledIds.clearIds();
        view.refreshDataView();
        view.updateToggledUser(toggledIds.count());
    }

    @Override
    public void inviteToggle() {
        view.showPrgoress();
        Observable.defer(() -> {
            List<Long> userIds = toggledIds.getIds();
            long teamId = TeamInfoLoader.getInstance().getTeamId();
            ResCommon resCommon;
            try {
                if (TeamInfoLoader.getInstance().isPublicTopic(roomId)) {
                    resCommon = channelApi.get().invitePublicTopic(roomId, new ReqInviteTopicUsers(userIds, teamId));
                } else {
                    resCommon = groupApi.get().inviteGroup(roomId, new ReqInviteTopicUsers(userIds, teamId));
                }
                return Observable.just(resCommon);
            } catch (RetrofitException e) {
                return Observable.error(e);
            }

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    TopicRepository.getInstance().addMember(roomId, toggledIds.getIds());
                    TeamInfoLoader.getInstance().refresh();
                    EventBus.getDefault().post(new InvitationSuccessEvent());

                    view.dismissProgress();
                    view.successToInvitation();
                }, e -> {
                    view.dismissProgress();
                    view.showFailToInvitation();
                });

    }

    @Override
    public void onSearchKeyword(String text) {
        filterSubject.onNext(text);
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}
