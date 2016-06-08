package com.tosslab.jandi.app.ui.maintab.mypage.model;

import android.util.Pair;

import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;

import java.util.ArrayList;
import java.util.List;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tonyjs on 16. 3. 17..
 */
public class MyPageModel {

    public static final int MENTION_LIST_LIMIT = 20;
    private final Lazy<MessageApi> messageApi;

    public MyPageModel(Lazy<MessageApi> messageApi) {
        this.messageApi = messageApi;
    }

    public User getMe() {
        return TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
    }

    public Observable<ResStarMentioned> getMentionsObservable(long offset, int limit) {
        final long teamId = TeamInfoLoader.getInstance().getTeamId();

        Observable.OnSubscribe<ResStarMentioned> requestMentionsSubscriber = subscriber -> {
            try {
                ResStarMentioned resStarMentioned =
                        messageApi.get().getMentionedMessages(teamId, offset, limit);

                subscriber.onNext(resStarMentioned);
            } catch (RetrofitException error) {
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

        Observable.from(records)
                .filter(mention -> mention.getLinkId() > 0 && mention.getRoom() != null && mention.getRoom().id > 0)
                .map(mentionMessage -> {
                    User user =
                            TeamInfoLoader.getInstance().getUser(
                                    mentionMessage.getMessage().writerId);
                    Room room =
                            TeamInfoLoader.getInstance().getRoom(mentionMessage.getRoom().id);

                    if (TeamInfoLoader.getInstance().isTopic(room.getId())) {
                        TopicRoom topicRoom = (TopicRoom) room;
                        return MentionMessage.create(mentionMessage,
                                topicRoom.getName(),
                                user.getName(), user.getPhotoUrl());
                    } else {
                        String name = Observable.from(room.getMembers())
                                .takeFirst(memberId -> memberId != TeamInfoLoader.getInstance().getMyId())
                                .map(memberId -> TeamInfoLoader.getInstance().getUser(memberId).getName())
                                .toBlocking()
                                .firstOrDefault("");
                        return MentionMessage.create(mentionMessage, name,
                                user.getName(), user.getPhotoUrl());
                    }

                })
                .subscribe(mentions::add);
        return mentions;
    }
}
