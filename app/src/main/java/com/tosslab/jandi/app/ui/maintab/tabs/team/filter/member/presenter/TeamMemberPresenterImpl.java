package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.presenter;

import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.InvitationSuccessEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.local.orm.repositories.search.MemberRecentKeywordRepository;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamDisabledMemberItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.model.TeamMemberModel;
import com.tosslab.jandi.app.utils.StringCompareUtil;
import com.tosslab.jandi.app.views.spannable.HighlightSpannable;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subjects.BehaviorSubject;

public class TeamMemberPresenterImpl implements TeamMemberPresenter {

    private final View view;
    TeamMemberModel teamMemberModel;
    private final TeamMemberDataModel teamMemberDataModel;
    private final HighlightSpannable highlightSpan;
    private boolean selectMode;

    BehaviorSubject<String> filterSubject;
    Subscription filterSubscription;
    private long roomId = -1;

    private ToggleCollector toggledIds;


    @Inject
    public TeamMemberPresenterImpl(View view,
                                   TeamMemberModel teamMemberModel,
                                   TeamMemberDataModel teamMemberDataModel,
                                   ToggleCollector toggledIds) {
        this.view = view;
        this.teamMemberModel = teamMemberModel;
        this.teamMemberDataModel = teamMemberDataModel;
        this.toggledIds = toggledIds;

        int highlighteColor = JandiApplication.getContext().getResources().getColor(R.color.rgb_00abe8);
        highlightSpan = new HighlightSpannable(Color.TRANSPARENT, highlighteColor);
    }

    @Override
    public void onCreate() {
        filterSubject = BehaviorSubject.create("");
        filterSubscription = filterSubject
                .throttleLast(100, TimeUnit.MILLISECONDS)
                .onBackpressureBuffer()
                .distinctUntilChanged()
                .observeOn(Schedulers.io())
                .map(String::toLowerCase)
                .concatMap(it -> teamMemberModel.getFilteredUser(it, selectMode, roomId)
                        .compose(sort())
                        .compose(textToSpan(it)))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(users -> {
                    teamMemberDataModel.clear();
                    if (!users.isEmpty()) {
                        teamMemberDataModel.addAll(users);
                        view.dismissEmptyView();
                    } else {
                        view.showEmptyView(filterSubject.getValue());
                    }
                    view.refreshDataView();
                }, Throwable::printStackTrace);

    }

    private Observable.Transformer<? super List<TeamMemberItem>, ? extends List<TeamMemberItem>> textToSpan(String it) {
        return observable -> observable.concatMap(new Func1<List<TeamMemberItem>, Observable<? extends List<TeamMemberItem>>>() {
            @Override
            public Observable<? extends List<TeamMemberItem>> call(List<TeamMemberItem> teamMemberItems) {
                if (TextUtils.isEmpty(it)) {
                    return Observable.from(teamMemberItems)
                            .map(it -> {
                                it.setNameOfSpan(it.getName());
                                return it;
                            })
                            .collect(ArrayList::new, List::add);
                } else {
                    return Observable.from(teamMemberItems)
                            .map(item -> {
                                int index = item.getName().toLowerCase().indexOf(it.toLowerCase());
                                if (index >= 0) {
                                    SpannableStringBuilder builder = new SpannableStringBuilder(item.getName());
                                    builder.setSpan(highlightSpan, index, index + it.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                                    item.setNameOfSpan(builder);
                                } else {
                                    item.setNameOfSpan(item.getName());
                                }
                                return item;
                            })
                            .collect(ArrayList::new, List::add);
                }
            }
        });
    }

    private Observable.Transformer<? super TeamMemberItem, ? extends List<TeamMemberItem>> sort() {
        return userObservable -> userObservable.toSortedList((entity, entity2) -> {

                    if (entity instanceof TeamDisabledMemberItem) {
                        return 1;
                    } else if (entity2 instanceof TeamDisabledMemberItem) {
                        return -1;
                    }

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

        if (teamMemberDataModel.getItem(position) instanceof TeamDisabledMemberItem) {
            view.moveDisabledMembers();
            return;
        }

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

        for (long user : users) {
            int position = teamMemberDataModel.findItemOfEntityId(user);
            if (position >= 0) {
                teamMemberDataModel.getItem(position).getChatChooseItem().setIsChooseItem(true);
                toggledIds.addId(user);
            }
        }

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
        teamMemberModel.deferInvite(toggledIds, roomId)
                .subscribeOn(Schedulers.io())
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
