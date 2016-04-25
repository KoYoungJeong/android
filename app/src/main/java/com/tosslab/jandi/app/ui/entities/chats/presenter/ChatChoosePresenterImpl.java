package com.tosslab.jandi.app.ui.entities.chats.presenter;

import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapterDataModel;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.DisableDummyItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.EmptyChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.model.ChatChooseModel;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class ChatChoosePresenterImpl implements ChatChoosePresenter {
    ChatChooseModel chatChooseModel;
    TeamDomainInfoModel teamDomainInfoModel;
    InvitationDialogExecutor invitationDialogExecutor;
    View view;
    private ChatChooseAdapterDataModel chatChooseAdapterDataModel;

    public ChatChoosePresenterImpl(ChatChooseModel chatChooseModel, TeamDomainInfoModel teamDomainInfoModel, InvitationDialogExecutor invitationDialogExecutor, View view, ChatChooseAdapterDataModel chatChooseAdapterDataModel) {
        this.chatChooseModel = chatChooseModel;
        this.teamDomainInfoModel = teamDomainInfoModel;
        this.invitationDialogExecutor = invitationDialogExecutor;
        this.view = view;
        this.chatChooseAdapterDataModel = chatChooseAdapterDataModel;
        initObject();
    }

    private PublishSubject<String> publishSubject;

    void initObject() {
        publishSubject = PublishSubject.create();
        publishSubject
                .throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(name -> {
                    if (!TextUtils.isEmpty(name)) {
                        return Pair.create(name, chatChooseModel.getChatListWithoutMe(name));
                    } else {
                        return Pair.create(name, chatChooseModel.getUsers());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((usersPair) -> {
                    if (view != null) {
                        chatChooseAdapterDataModel.clear();
                        if (!usersPair.second.isEmpty()) {
                            chatChooseAdapterDataModel.addAll(usersPair.second);
                        } else {
                            if (!TextUtils.isEmpty(usersPair.first)) {
                                chatChooseAdapterDataModel.add(new EmptyChatChooseItem(usersPair.first));
                            }
                        }
                        view.refresh();
                    }
                });
    }

    @Override
    public void initMembers() {
        Observable.from(chatChooseModel.getUsers())
                .subscribeOn(Schedulers.computation())
                .collect((Func0<List<ChatChooseItem>>) ArrayList::new, List::add)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((chatListWithoutMe) -> {
                            chatChooseAdapterDataModel.clear();
                            chatChooseAdapterDataModel.addAll(chatListWithoutMe);
                        },
                        Throwable::printStackTrace,
                        view::refresh);

    }

    @Override
    public void onSearch(String text) {
        publishSubject.onNext(text);
    }

    @Override
    public void invite() {
        invitationDialogExecutor.setFrom(InvitationDialogExecutor.FROM_CHAT_CHOOSE);
        invitationDialogExecutor.execute();
    }

    @Override
    public void onMoveChatMessage(long entityId) {
        Observable.empty()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {}, Throwable::printStackTrace, () -> {
                    EntityManager entityManager = EntityManager.getInstance();
                    long teamId = entityManager.getTeamId();
                    view.moveChatMessage(teamId, entityId);
                });
    }

    @Override
    public void onItemClick(int position) {
        ChatChooseItem chatChooseItem = chatChooseAdapterDataModel.getItem(position);
        if (chatChooseItem instanceof DisableDummyItem) {
            view.MoveDisabledEntityList();
        } else if (chatChooseItem instanceof EmptyChatChooseItem) {
            // do nothing.
        } else {
            onMoveChatMessage(chatChooseItem.getEntityId());
        }
    }
}
