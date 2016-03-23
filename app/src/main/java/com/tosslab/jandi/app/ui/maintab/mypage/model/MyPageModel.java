package com.tosslab.jandi.app.ui.maintab.mypage.model;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;
import com.tosslab.jandi.app.utils.AccountUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MyPageModel {

    public static final int MENTION_LIST_LIMIT = 20;

    public FormattedEntity getMe() {
        return EntityManager.getInstance().getMe();
    }

    public Observable<ResStarMentioned> getMentionsObservable(long offset, int limit) {
        final long teamId = EntityManager.getInstance().getTeamId();

        Observable.OnSubscribe<ResStarMentioned> requestMentionsSubscriber = subscriber -> {
            ResStarMentioned resStarMentioned =
                    RequestApiManager.getInstance().getMentionedMessagesByTeamApi(teamId, offset, limit);
            subscriber.onNext(resStarMentioned);
            subscriber.onCompleted();
        };

        return Observable.create(requestMentionsSubscriber)
                .subscribeOn(Schedulers.io());
    }

    public List<MentionMessage> getConvertedMentionList(List<StarMentionedMessageObject> records) {
        final EntityManager entityManager = EntityManager.getInstance();
        List<MentionMessage> mentions = new ArrayList<>();
        Observable.from(records)
                .map(mentionMessage -> {
                    FormattedEntity user =
                            entityManager.getEntityById(
                                    mentionMessage.getMessage().writerId);
                    FormattedEntity room =
                            entityManager.getEntityById(mentionMessage.getRoom().id);

                    return MentionMessage.create(mentionMessage,
                            room.getName(),
                            user.getName(), user.getUserLargeProfileUrl());
                })
                .subscribe(mentions::add);
        return mentions;
    }
}
