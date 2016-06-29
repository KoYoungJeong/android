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
import com.tosslab.jandi.app.network.models.ResDeletePoll;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResPollCommentCreated;
import com.tosslab.jandi.app.network.models.ResPollComments;
import com.tosslab.jandi.app.network.models.ResPollDetail;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.poll.detail.dto.PollDetail;
import com.tosslab.jandi.app.utils.logger.LogUtil;

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

    public PollDetailModel(Lazy<PollApi> api) {
        pollApi = api;
        entityClientManager = EntityClientManager_.getInstance_(JandiApplication.getContext());
        messageManipulator = MessageManipulator_.getInstance_(JandiApplication.getContext());
    }

    public Observable<PollDetail> getPollDetailObservable(long pollId, final PollDetail pollDetail) {
        return Observable.<PollDetail>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollDetail resPollDetail = pollApi.get().getPollDetail(teamId, pollId);
                    pollDetail.setPoll(resPollDetail.getPoll());
                    subscriber.onNext(pollDetail);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResDeletePoll> getPollVoteObservable(long pollId, Collection<Integer> seqs) {
        return Observable.<ResDeletePoll>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ReqVotePoll reqVotePoll = ReqVotePoll.create(seqs);
                    ResDeletePoll pollDetail = pollApi.get().votePoll(teamId, pollId, reqVotePoll);
                    subscriber.onNext(pollDetail);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<PollDetail> getPollCommentsObservable(long pollId, PollDetail pollDetail) {
        return Observable.<PollDetail>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResPollComments resPollComments = pollApi.get().getPollComments(teamId, pollId);
                    pollDetail.setPollComments(resPollComments.getComments());
                    subscriber.onNext(pollDetail);
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
                    ResPollCommentCreated pollDetail =
                            pollApi.get().sendPollComment(teamId, pollId, reqSendComment);
                    subscriber.onNext(pollDetail);
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

    public Observable<StarMentionedMessageObject> getCommentStarredObservable(long messageId) {

        return Observable.<StarMentionedMessageObject>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    StarMentionedMessageObject resCommon =
                            messageApi.get().registStarredMessage(teamId, messageId, new ReqNull());
                    subscriber.onNext(resCommon);
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

    public Observable<ResDeletePoll> getPollFinishObservable(long pollId) {
        return Observable.<ResDeletePoll>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResDeletePoll pollDetail = pollApi.get().finishPoll(teamId, pollId);
                    subscriber.onNext(pollDetail);
                } catch (RetrofitException e) {
                    subscriber.onError(e);
                }
                subscriber.onCompleted();
            }
        });
    }

    public Observable<ResDeletePoll> getPollDeleteObservable(long pollId) {
        return Observable.<ResDeletePoll>create(subscriber -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();

            if (teamId <= 0l) {
                subscriber.onError(new NullPointerException("has not selected team."));
                subscriber.onCompleted();
            } else {
                try {
                    ResDeletePoll resDeletePoll = pollApi.get().deletePoll(teamId, pollId);
                    subscriber.onNext(resDeletePoll);
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

        LogUtil.i("tony24", poll.toString());
        PollRepository.getInstance().upsertPoll(poll);

        Poll pollById = PollRepository.getInstance().getPollById(poll.getId());
        LogUtil.i("tony24", pollById.toString());
    }
}
