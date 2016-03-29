package com.tosslab.jandi.app.ui.maintab.mypage.model;

import android.util.Pair;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

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
            try {
                ResStarMentioned resStarMentioned =
                        RequestApiManager.getInstance()
                                .getMentionedMessagesByTeamApi(teamId, offset, limit);

                subscriber.onNext(resStarMentioned);
            } catch (RetrofitError error) {
                subscriber.onError(error);
            }
            subscriber.onCompleted();
        };

        return Observable.create(requestMentionsSubscriber);
    }

    public Observable<Pair<Boolean, List<MentionMessage>>> getConvertedMentionObservable(
            ResStarMentioned resStarMentioned) {
        return Observable.just(resStarMentioned)
                .map(resStarMentioned1 -> {
                    List<MentionMessage> convertedMentionList =
                            getConvertedMentionList(resStarMentioned.getRecords());
                    return Pair.create(resStarMentioned1.hasMore(), convertedMentionList);
                });
    }

    public List<MentionMessage> getConvertedMentionList(List<StarMentionedMessageObject> records) {
        List<MentionMessage> mentions = new ArrayList<>();
        if (records == null || records.isEmpty()) {
            return mentions;
        }

        final EntityManager entityManager = EntityManager.getInstance();
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
