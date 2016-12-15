package com.tosslab.jandi.app.ui.entities.chats.presenter;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.entities.chats.adapter.ChatChooseAdapterDataModel;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.DisableDummyItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.EmptyChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.model.ChatChooseModel;
import com.tosslab.jandi.app.ui.invites.InviteDialogExecutor;
import com.tosslab.jandi.app.ui.team.create.teaminfo.model.InsertTeamInfoModel;

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
    InsertTeamInfoModel insertTeamInfoModel;
    View view;
    private ChatChooseAdapterDataModel chatChooseAdapterDataModel;
    private PublishSubject<String> publishSubject;

    public ChatChoosePresenterImpl(ChatChooseModel chatChooseModel,
                                   InsertTeamInfoModel teamDomainInfoModel,
                                   View view,
                                   ChatChooseAdapterDataModel chatChooseAdapterDataModel) {
        this.chatChooseModel = chatChooseModel;
        this.insertTeamInfoModel = teamDomainInfoModel;
        this.view = view;
        this.chatChooseAdapterDataModel = chatChooseAdapterDataModel;
        initObject();
    }

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
    public void invite(Context context) {
        InviteDialogExecutor.getInstance().executeInvite(context);
    }

    @Override
    public void onMoveChatMessage(long entityId) {
        Observable.empty()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                }, Throwable::printStackTrace, () -> {
                    long teamId = TeamInfoLoader.getInstance().getTeamId();
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
