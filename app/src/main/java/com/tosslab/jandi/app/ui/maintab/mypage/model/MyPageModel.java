package com.tosslab.jandi.app.ui.maintab.mypage.model;

import android.util.Pair;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.maintab.mypage.dto.MentionMessage;
import com.tosslab.jandi.app.utils.logger.LogUtil;

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
                .filter(mention -> mention.getMessage() != null)
                .filter(mention -> TeamInfoLoader.getInstance().isUser(mention.getMessage().writerId))
                .map(mentionMessage -> {
                    User user = TeamInfoLoader.getInstance()
                            .getUser(mentionMessage.getMessage().writerId);

                    if (mentionMessage.getRoom().id > 0) {
                        // message
                        if (TeamInfoLoader.getInstance().isTopic(mentionMessage.getRoom().id)) {
                            TopicRoom topic = TeamInfoLoader.getInstance().getTopic(mentionMessage.getRoom().id);
                            return MentionMessage.create(mentionMessage,
                                    topic.getName(),
                                    user.getName(), user.getPhotoUrl());
                        } else {
                            Room room = TeamInfoLoader.getInstance().getRoom(mentionMessage.getRoom().id);
                            DirectMessageRoom room1 = (DirectMessageRoom) room;
                            long companionId = room1.getCompanionId();
                            String userName = TeamInfoLoader.getInstance().getMemberName(companionId);
                            return MentionMessage.create(mentionMessage,
                                    userName,
                                    user.getName(), user.getPhotoUrl());
                        }
                    } else {
                        // comment
                        return MentionMessage.create(mentionMessage,
                                mentionMessage.getMessage().feedbackTitle,
                                user.getName(), user.getPhotoUrl());
                    }

                })
                .subscribe(mentions::add);
        return mentions;
    }

    public Observable<List<Poll>> getEnablePollListObservable() {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();

        return Observable.from(PollRepository.getInstance().getPolls())
                .filter(poll ->
                        poll.getTeamId() == teamId
                                && "created".equals(poll.getStatus())
                                && "enabled".equals(poll.getVoteStatus()))
                .map(poll -> {
                    LogUtil.e("tony50", poll.toString());
                    return poll;
                })
                .toList();

    }
}
