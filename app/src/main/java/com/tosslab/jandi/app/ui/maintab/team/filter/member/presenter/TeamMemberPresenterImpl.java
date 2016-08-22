package com.tosslab.jandi.app.ui.maintab.team.filter.member.presenter;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.domain.TeamMemberItem;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class TeamMemberPresenterImpl implements TeamMemberPresenter {

    private final View view;
    private final TeamMemberDataModel teamMemberDataModel;
    private boolean selectMode;

    private BehaviorSubject<String> filterSubject;
    private Subscription filterSubscription;
    private long roomId = -1;

    private Set<Long> toggledIds;


    @Inject
    public TeamMemberPresenterImpl(View view, TeamMemberDataModel teamMemberDataModel) {
        this.view = view;
        this.teamMemberDataModel = teamMemberDataModel;
    }

    @Override
    public void onCreate() {
        filterSubject = BehaviorSubject.create("");
        filterSubscription = filterSubject
                .throttleLast(100, TimeUnit.MILLISECONDS)
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
                                return room.getMembers().contains(user.getId());
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
        TeamMemberItem item = teamMemberDataModel.getItem(position);
        long userId = item.getChatChooseItem().getEntityId();
        if (!selectMode) {
            view.moveProfile(userId);
        } else {

            if (toggledIds.contains(userId)) {
                toggledIds.remove(userId);
                item.getChatChooseItem().setIsChooseItem(false);
            } else {
                toggledIds.add(userId);
                item.getChatChooseItem().setIsChooseItem(true);
            }

            view.updateToggledUser(toggledIds.size());
            view.refreshDataView();
        }

    }

    @Override
    public void addToggledUser(long[] users) {
        for (long user : users) {
            toggledIds.add(user);
            int position = teamMemberDataModel.findItemOfEntityId(user);
            if (position >= 0) {
                teamMemberDataModel.getItem(position).getChatChooseItem().setIsChooseItem(true);
            }
        }

        view.refreshDataView();
        view.updateToggledUser(toggledIds.size());
    }

    @Override
    public void onSearchKeyword(String text) {
        filterSubject.onNext(text);
    }

    public void setSelectMode(boolean selectMode) {
        this.selectMode = selectMode;
        if (selectMode) {
            toggledIds = new HashSet<>();
        }
    }

    public void setRoomId(long roomId) {
        this.roomId = roomId;
    }
}
