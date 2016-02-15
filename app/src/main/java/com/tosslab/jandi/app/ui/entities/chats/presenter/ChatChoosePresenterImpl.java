package com.tosslab.jandi.app.ui.entities.chats.presenter;

import android.text.TextUtils;

import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.model.ChatChooseModel;
import com.tosslab.jandi.app.ui.invites.InvitationDialogExecutor;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

@EBean
public class ChatChoosePresenterImpl implements ChatChoosePresenter {
    @Bean
    ChatChooseModel chatChooseModel;
    @Bean
    TeamDomainInfoModel teamDomainInfoModel;
    @Bean
    InvitationDialogExecutor invitationDialogExecutor;
    private View view;
    private PublishSubject<String> publishSubject;

    @AfterInject
    void initObject() {
        publishSubject = PublishSubject.create();
        publishSubject
                .throttleWithTimeout(500, TimeUnit.MILLISECONDS)
                .observeOn(Schedulers.io())
                .map(name -> {
                    if (!TextUtils.isEmpty(name)) {
                        return chatChooseModel.getChatListWithoutMe(name);
                    } else {
                        return chatChooseModel.getUsers();
                    }
                })
                .subscribe((users) -> {
                    if (view != null) {
                        view.setUsers(users);
                    }
                });
    }

    @Override
    public void setView(View view) {
        this.view = view;
    }

    @Override
    public void initMembers() {
        List<ChatChooseItem> users = chatChooseModel.getUsers();
        view.setUsers(users);
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
        EntityManager entityManager = EntityManager.getInstance();
        long teamId = entityManager.getTeamId();
        view.moveChatMessage(teamId, entityId);
    }
}
