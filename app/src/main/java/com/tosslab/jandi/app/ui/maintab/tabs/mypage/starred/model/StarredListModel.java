package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.model;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import dagger.Lazy;
import rx.Observable;

/**
 * Created by tee on 15. 7. 30..
 */
public class StarredListModel {

    public static final int DEFAULT_COUNT = 20;

    private final Lazy<MessageApi> messageApi;

    public StarredListModel(Lazy<MessageApi> messageApi) {
        this.messageApi = messageApi;
    }

    public Observable<ResStarMentioned> getStarredListObservable(String type, long offset, int count) {
        return Observable.defer(() -> {
                    long teamId = getTeamId();
                    try {
                        ResStarMentioned starredMessages = messageApi.get()
                                .getStarredMessages(teamId, offset, count, type);
                        return Observable.just(starredMessages);
                    } catch (RetrofitException e) {
                        return Observable.error(e);
                    }
                }
        );
    }

    public Observable<ResCommon> getUnStarMessageObservable(long messageId) {
        return Observable.defer(() -> {
                    long teamId = getTeamId();

                    try {
                        ResCommon resCommon = messageApi.get().unregistStarredMessage(teamId, messageId);
                        return Observable.just(resCommon);
                    } catch (RetrofitException e) {
                        return Observable.error(e);
                    }
                }
        );
    }

    public long getTeamId() {
        return AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
    }

}