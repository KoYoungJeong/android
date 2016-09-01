package com.tosslab.jandi.app.ui.poll.detail.model;

import android.support.annotation.VisibleForTesting;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.EntityClientManager;
import com.tosslab.jandi.app.network.client.EntityClientManager_;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqNull;
import com.tosslab.jandi.app.network.models.ReqSendPollComment;
import com.tosslab.jandi.app.network.models.ReqVotePoll;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResPollCommentCreated;
import com.tosslab.jandi.app.network.models.ResPollComments;
import com.tosslab.jandi.app.network.models.ResPollDetail;
import com.tosslab.jandi.app.network.models.ResPollLink;
import com.tosslab.jandi.app.network.models.ResStarredMessage;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import rx.Observable;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class PollDetailModel {

    @VisibleForTesting
    Lazy<PollApi> pollApi;
    @Inject
    Lazy<MessageApi> messageApi;

    MessageManipulator messageManipulator;
    EntityClientManager entityClientManager;

    public PollDetailModel(Lazy<PollApi> pollApi, Lazy<MessageApi> messageApi) {
        this.pollApi = pollApi;
        this.messageApi = messageApi;
        entityClientManager = EntityClientManager_.getInstance_(JandiApplication.getContext());
        messageManipulator = MessageManipulator_.getInstance_(JandiApplication.getContext());
    }

    public Observable<ResPollDetail> getPollDetailObservable(long pollId) {
        return Observable.<ResPollDetail>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollDetail resPollDetail = pollApi.get().getPollDetail(teamId, pollId);
                    subscriber.onNext(resPollDetail);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResPollLink> getPollVoteObservable(long pollId, Collection<Integer> seqs) {
        return Observable.<ResPollLink>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ReqVotePoll reqVotePoll = ReqVotePoll.create(seqs);
                    ResPollLink pollDetail = pollApi.get().votePoll(teamId, pollId, reqVotePoll);
                    subscriber.onNext(pollDetail);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResPollComments> getPollCommentsObservable(long pollId) {
        return Observable.<ResPollComments>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollComments resPollComments = pollApi.get().getPollComments(teamId, pollId);
                    subscriber.onNext(resPollComments);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResPollCommentCreated> getSendCommentObservable(long pollId,
                                                                      String comment,
                                                                      List<MentionObject> mentions) {
        return Observable.<ResPollCommentCreated>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ReqSendPollComment reqSendComment = new ReqSendPollComment(comment, mentions);
                    ResPollCommentCreated resPollCommentCreated =
                            pollApi.get().sendPollComment(teamId, pollId, reqSendComment);
                    subscriber.onNext(resPollCommentCreated);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });

    }

    public Observable<ResPollCommentCreated> getSendStickerCommentObservable(
            long pollId, long stickerGroupId, String stickerId,
            String comment, List<MentionObject> mentions) {

        return Observable.<ResPollCommentCreated>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ReqSendPollComment reqSendSticker =
                            new ReqSendPollComment(stickerGroupId, stickerId, comment, mentions);
                    ResPollCommentCreated pollDetail =
                            pollApi.get().sendPollComment(teamId, pollId, reqSendSticker);
                    subscriber.onNext(pollDetail);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });

    }

    public Observable<ResCommon> getJoinEntityObservable(TopicRoom room) {
        return Observable.<ResCommon>create(subscriber -> {
            try {
                ResCommon resCommon = entityClientManager.joinChannel(room.getId());
                subscriber.onNext(resCommon);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    public void sortByDate(List<ResMessages.OriginalMessage> comments) {
        Collections.sort(comments, (lhs, rhs) -> lhs.createTime.before(rhs.createTime) ? -1 : 1);
    }

    public boolean isCommentFromMe(long writerId) {
        return TeamInfoLoader.getInstance().getMyId() == writerId;
    }

    public void updateJoinedTopic(long id) {
        TopicRepository.getInstance().updateTopicJoin(id, true);
        TeamInfoLoader.getInstance().refresh();
    }

    public Observable<ResStarredMessage> getCommentStarredObservable(long messageId) {

        return Observable.<ResStarredMessage>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResStarredMessage resStarredMessage =
                            messageApi.get().registStarredMessage(teamId, messageId, new ReqNull());
                    subscriber.onNext(resStarredMessage);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResCommon> getCommentUnStarredObservable(long messageId) {

        return Observable.<ResCommon>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResCommon resCommon =
                            messageApi.get().unregistStarredMessage(teamId, messageId);
                    subscriber.onNext(resCommon);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResCommon> getCommentDeleteObservable(long messageId, long feedbackId) {
        return Observable.<ResCommon>create(subscriber -> {
            try {
                ResCommon resCommon = entityClientManager.deleteMessageComment(messageId, feedbackId);
                subscriber.onNext(resCommon);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    public Observable<ResCommon> getStickerCommentDeleteObservable(long messageId, int messageType) {
        return Observable.<ResCommon>create(subscriber -> {
            try {
                ResCommon resCommon = messageManipulator.deleteSticker(messageId, messageType);
                subscriber.onNext(resCommon);
            } catch (RetrofitException e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    public Observable<ResPollLink> getPollFinishObservable(long pollId) {
        return Observable.<ResPollLink>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollLink pollDetail = pollApi.get().finishPoll(teamId, pollId);
                    subscriber.onNext(pollDetail);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResPollLink> getPollDeleteObservable(long pollId) {
        return Observable.<ResPollLink>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollLink resPollLink = pollApi.get().deletePoll(teamId, pollId);
                    subscriber.onNext(resPollLink);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public void upsertPoll(Poll poll) {
        if (poll == null || poll.getId() <= 0) {
            return;
        }

        PollRepository.getInstance().upsertPoll(poll);
    }

    public ResStarredMessage starPoll(long messageId) throws RetrofitException {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        ResStarredMessage resStarredMessage = messageApi.get()
                .registStarredMessage(teamId, messageId, new ReqNull());
        return resStarredMessage;
    }

    public ResCommon unStarPoll(long messageId) throws RetrofitException {
        long teamId = AccountRepository.getRepository().getSelectedTeamId();
        ResCommon resCommon = messageApi.get()
                .unregistStarredMessage(teamId, messageId);
        return resCommon;
    }

    public void trackStarredPollSuccess(long pollId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.Starred)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.PollId, pollId)
                .build());
    }

    public void trackStarredCommentSuccess(long commentId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.Starred)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.MessageId, commentId)
                .build());
    }

    public void trackStarredFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.Starred)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
    }

    public void trackUnStarredPollSuccess(long pollId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.UnStarred)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.PollId, pollId)
                .build());
    }

    public void trackUnStarredCommentSuccess(long commentId) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.UnStarred)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.MessageId, commentId)
                .build());
    }

    public void trackUnStarredFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.UnStarred)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
    }

    public boolean hasAllMention(String message, List<MentionObject> mentions) {
        return Observable.from(mentions)
                .takeFirst(mentionObject -> {
                    int start = mentionObject.getOffset() + 1;
                    int end = start + mentionObject.getLength();
                    if (message.substring(start, end).equals("All")) {
                        return true;
                    }
                    return false;
                })
                .map(mentionObject -> {
                    if (mentionObject != null) {
                        return true;
                    } else {
                        return false;
                    }
                })
                .toBlocking().firstOrDefault(false);
    }


}
