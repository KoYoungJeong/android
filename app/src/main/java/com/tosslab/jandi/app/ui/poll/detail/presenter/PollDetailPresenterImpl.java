package com.tosslab.jandi.app.ui.poll.detail.presenter;

import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResPollComments;
import com.tosslab.jandi.app.network.models.ResPollDetail;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.poll.detail.adapter.model.PollDetailDataModel;
import com.tosslab.jandi.app.ui.poll.detail.model.PollDetailModel;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class PollDetailPresenterImpl implements PollDetailPresenter {

    private final PollDetailModel pollDetailModel;
    private final PollDetailPresenter.View pollDetailView;
    private final PollDetailDataModel pollDetailDataModel;

    private PublishSubject<Long> reInitializePollDetailQueue;
    private Subscription reInitializePollDetailQueueSubs;

    private PublishSubject<Pair<Long, Boolean>> starredStateChangeQueue;
    private Subscription starredStateChangeQueueSubs;

    @Inject
    public PollDetailPresenterImpl(PollDetailModel pollDetailModel,
                                   PollDetailDataModel pollDetailDataModel,
                                   PollDetailPresenter.View pollDetailView) {
        this.pollDetailModel = pollDetailModel;
        this.pollDetailDataModel = pollDetailDataModel;
        this.pollDetailView = pollDetailView;

        initPollDetailInitializeQueue();
        initPollStarQueue();
    }

    @Override
    public void initPollDetailInitializeQueue() {
        reInitializePollDetailQueue = PublishSubject.create();
        reInitializePollDetailQueueSubs =
                reInitializePollDetailQueue.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .subscribeOn(Schedulers.io())
                        .concatMap(pollId ->
                                Observable.combineLatest(
                                        pollDetailModel.getPollDetailObservable(pollId),
                                        pollDetailModel.getPollCommentsObservable(pollId),
                                        Pair::create))
                        .filter(pair ->
                                pair.first != null
                                        && pair.first.getPoll() != null
                                        && pair.first.getPoll().getId() > 0)
                        .doOnNext(pair -> {
                            ResPollDetail resPollDetail = pair.first;
                            if (resPollDetail != null && resPollDetail.getPoll() != null) {
                                pollDetailModel.upsertPoll(resPollDetail.getPoll());
                            }
                        })
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(pair -> {
                            pollDetailDataModel.removeAllRows();

                            ResPollDetail resPollDetail = pair.first;
                            Poll poll = resPollDetail.getPoll();
                            pollDetailDataModel.setPollDetails(poll);

                            ResPollComments resPollComments = pair.second;
                            List<ResMessages.OriginalMessage> pollComments =
                                    resPollComments.getComments();
                            if (pollComments != null && !pollComments.isEmpty()) {
                                pollDetailModel.sortByDate(pollComments);
                                pollDetailDataModel.addPollComments(pollComments);
                            }

                            pollDetailView.notifyDataSetChanged();

                            pollDetailView.initPollDetailExtras(poll);

                        }, e -> {
                            LogUtil.e(TAG, Log.getStackTraceString(e));
                        });
    }

    @Override
    public void initPollStarQueue() {
        starredStateChangeQueue = PublishSubject.create();
        starredStateChangeQueueSubs =
                starredStateChangeQueue.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                        .onBackpressureBuffer()
                        .concatMap(pair -> {
                            long messageId = pair.first;
                            boolean futureStar = pair.second;
                            return Observable.defer(() -> {
                                try {
                                    if (futureStar) {
                                        pollDetailModel.starPoll(messageId);
                                        pollDetailModel.trackStarredPollSuccess(messageId);
                                    } else {
                                        pollDetailModel.unStarPoll(messageId);
                                        pollDetailModel.trackUnStarredPollSuccess(messageId);
                                    }
                                    return Observable.just(pair);
                                } catch (RetrofitException e) {
                                    if (futureStar) {
                                        pollDetailModel.trackStarredPollFail(e.getResponseCode());
                                    } else {
                                        pollDetailModel.trackUnStarredPollFail(e.getResponseCode());
                                    }
                                    return Observable.error(e);
                                }
                            });
                        })
                        .subscribeOn(Schedulers.io())
                        .subscribe(pair -> {
                        }, Throwable::printStackTrace);
    }

    @Override
    public void onInitializePollDetail(final long pollId) {
        if (!NetworkCheckUtil.isConnected()) {
            pollDetailView.showCheckNetworkDialog(true);
            return;
        }

        pollDetailView.showProgress();
        Observable.combineLatest(pollDetailModel.getPollDetailObservable(pollId),
                pollDetailModel.getPollCommentsObservable(pollId),
                Pair::create)
                .doOnNext(pair -> {
                    ResPollDetail resPollDetail = pair.first;
                    if (resPollDetail != null && resPollDetail.getPoll() != null) {
                        pollDetailModel.upsertPoll(resPollDetail.getPoll());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    pollDetailView.dismissProgress();

                    ResPollDetail resPollDetail = pair.first;
                    Poll poll = resPollDetail.getPoll();
                    if (poll == null || poll.getId() <= 0) {
                        pollDetailView.showUnExpectedErrorToast();
                        pollDetailView.finish();
                        return;
                    }

                    pollDetailDataModel.setPollDetails(poll);

                    ResPollComments resPollComments = pair.second;
                    List<ResMessages.OriginalMessage> pollComments = resPollComments.getComments();
                    if (pollComments != null && !pollComments.isEmpty()) {
                        pollDetailModel.sortByDate(pollComments);
                        pollDetailDataModel.addPollComments(pollComments);
                    }

                    pollDetailView.notifyDataSetChanged();

                    pollDetailView.initPollDetailExtras(poll);

                }, e -> {
                    pollDetailView.dismissProgress();
                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    pollDetailView.showUnExpectedErrorToast();
                    pollDetailView.finish();
                });

    }

    @Override
    public void reInitializePollDetail(long pollId) {
        if (!NetworkCheckUtil.isConnected()) {
            return;
        }

        reInitializePollDetailQueue.onNext(pollId);
    }

    @Override
    public void onPollVoteAction(long pollId, Collection<Integer> seqs) {
        if (!NetworkCheckUtil.isConnected()) {
            pollDetailView.showCheckNetworkDialog(false);
            return;
        }

        pollDetailView.showProgress();
        pollDetailModel.getPollVoteObservable(pollId, seqs)
                .doOnNext(resDeletePoll -> {
                    ResMessages.Link linkMessage = resDeletePoll.getLinkMessage();
                    Poll poll = linkMessage != null ? linkMessage.poll : null;
                    pollDetailModel.upsertPoll(poll);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resDeletePoll -> {
                    pollDetailView.dismissProgress();

                    ResMessages.Link linkMessage = resDeletePoll.getLinkMessage();
                    if (linkMessage == null
                            || linkMessage.poll == null || linkMessage.poll.getId() <= 0) {
                        return;
                    }

                    Poll poll = linkMessage.poll;

                    pollDetailDataModel.removePollDetailRow();
                    pollDetailDataModel.replacePollDetails(poll);
                    pollDetailView.notifyDataSetChanged();

                    pollDetailView.initPollDetailExtras(poll);

                    AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                            .event(SprinklerEvents.PollVoted)
                            .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                            .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                            .property(PropertyKey.ResponseSuccess, true)
                            .property(PropertyKey.TeamId, TeamInfoLoader.getInstance().getTeamId())
                            .property(PropertyKey.MemberId, TeamInfoLoader.getInstance().getMyId())
                            .property(PropertyKey.TopicId, poll.getTopicId())
                            .property(PropertyKey.PollId, pollId)
                            .build());
                }, t -> {
                    pollDetailView.dismissProgress();

                    LogUtil.e(TAG, Log.getStackTraceString(t));
                    pollDetailView.showUnExpectedErrorToast();

                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        Poll poll = pollDetailDataModel.getPoll();
                        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                                .event(SprinklerEvents.PollVoted)
                                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                                .property(PropertyKey.ResponseSuccess, false)
                                .property(PropertyKey.ErrorCode, e.getStatusCode())
                                .property(PropertyKey.TeamId, TeamInfoLoader.getInstance().getTeamId())
                                .property(PropertyKey.MemberId, TeamInfoLoader.getInstance().getMyId())
                                .property(PropertyKey.TopicId, poll.getTopicId())
                                .property(PropertyKey.PollId, pollId)
                                .build());
                    }
                });
    }

    @Override
    public void onCommentCreated(ResMessages.Link linkComment) {
        if (linkComment == null
                || linkComment.message == null) {
            return;
        }

        Observable.just(linkComment.message)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .filter(comment -> {
                    // 이미 있는 코멘트 인 경우 무시한다.
                    ResMessages.Link initializedPollComment =
                            pollDetailDataModel.getPollCommentById(linkComment.id);
                    return initializedPollComment.id <= 0;
                })
                .subscribe(comment -> {

                    pollDetailDataModel.addPollComment(comment);
                    pollDetailView.notifyDataSetChanged();

                    if (pollDetailModel.isCommentFromMe(comment.writerId)) {
                        pollDetailView.scrollToLastComment();
                    }

                }, Throwable::printStackTrace);

    }

    @Override
    public void onCommentDeleted(ResMessages.Link linkComment) {
        if (linkComment == null
                || linkComment.message == null) {
            return;
        }

        Observable.just(linkComment.message)
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(comment -> {
                    pollDetailDataModel.removePollComment(comment.id);
                    pollDetailView.notifyDataSetChanged();
                }, Throwable::printStackTrace);
    }

    @Override
    public void onSendComment(long pollId, String message, List<MentionObject> mentions) {
        pollDetailModel.getSendCommentObservable(pollId, message, mentions)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resPollCommentCreated -> {
                    pollDetailModel.trackPollCommentPostSuccess(
                            resPollCommentCreated.getLinkComment().messageId,
                            pollId,
                            mentions.size(),
                            pollDetailModel.hasAllMention(message, mentions)
                    );
                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                    if (throwable instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) throwable;
                        pollDetailModel.trackPollCommentPostFail(e.getResponseCode());
                    }
                });
    }

    @Override
    public void onSendCommentWithSticker(long pollId,
                                         long stickerGroupId, String stickerId,
                                         String message, List<MentionObject> mentions) {
        pollDetailModel.getSendStickerCommentObservable(
                pollId, stickerGroupId, stickerId, message, mentions)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resPollCommentCreated -> {
                    StringBuilder stickerIdStringBuilder =
                            new StringBuilder(String.valueOf(stickerGroupId));
                    stickerIdStringBuilder.append("-");
                    stickerIdStringBuilder.append(String.valueOf(stickerId));

                    long messageId;

                    if (resPollCommentCreated.getLinkSticker() != null) {
                        messageId = resPollCommentCreated.getLinkSticker().messageId;
                    } else {
                        messageId = resPollCommentCreated.getLinkComment().messageId;
                    }

                    pollDetailModel.trackPollStickerCommentPostSuccess(
                            messageId,
                            pollId,
                            stickerIdStringBuilder.toString(),
                            mentions.size(),
                            pollDetailModel.hasAllMention(message, mentions));

                }, throwable -> {
                    LogUtil.e(TAG, Log.getStackTraceString(throwable));
                    if (throwable instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) throwable;
                        pollDetailModel.trackPollCommentPostFail(e.getResponseCode());
                    }

                });
    }

    @Override
    public void joinAndMove(TopicRoom topic) {
        pollDetailView.showProgress();

        pollDetailModel.getJoinEntityObservable(topic)
                .map(resCommon -> {
                    pollDetailModel.updateJoinedTopic(topic.getId());
                    return resCommon;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resCommon -> {

                    pollDetailView.dismissProgress();

                    pollDetailView.moveToMessageListActivity(
                            topic.getId(), JandiConstants.TYPE_PUBLIC_TOPIC, topic.getId(), false);

                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));

                    pollDetailView.dismissProgress();
                });
    }

    @Override
    public void onChangeCommentStarredState(long messageId, boolean starred) {
        if (starred) {
            starComment(messageId);
        } else {
            unStarComment(messageId);
        }
    }

    private void unStarComment(long messageId) {
        pollDetailModel.getCommentUnStarredObservable(messageId)
                .map(resCommon -> {
                    MessageRepository.getRepository().updateStarred(messageId, false);
                    return resCommon;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    pollDetailDataModel.modifyCommentStarredState(messageId, false);
                    pollDetailView.notifyDataSetChanged();
                    pollDetailView.showCommentUnStarredSuccessToast();
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                    pollDetailModel.trackUnStarredCommentSuccess(messageId);
                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    if (e instanceof RetrofitException) {
                        pollDetailModel.trackUnStarredCommentFail(((RetrofitException) e).getResponseCode());
                    }
                });
    }

    private void starComment(long messageId) {
        pollDetailModel.getCommentStarredObservable(messageId)
                .map(starMentionedMessageObject -> {
                    MessageRepository.getRepository().updateStarred(messageId, true);
                    return starMentionedMessageObject;
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    pollDetailDataModel.modifyCommentStarredState(messageId, true);
                    pollDetailView.notifyDataSetChanged();

                    pollDetailView.showCommentStarredSuccessToast();
                    EventBus.getDefault().post(new StarredInfoChangeEvent());
                    pollDetailModel.trackStarredCommentSuccess(messageId);
                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    if (e instanceof RetrofitException) {
                        pollDetailModel.trackStarredCommentFail(((RetrofitException) e).getResponseCode());
                    }
                });
    }

    @Override
    public void onDeleteComment(int messageType, long messageId, long feedbackId) {
        if (!NetworkCheckUtil.isConnected()) {
            pollDetailView.showCheckNetworkDialog(false);
            return;
        }

        Pair<Integer, ResMessages.OriginalMessage> pair =
                pollDetailDataModel.removeCommentByMessageIdAndGet(messageId);
        int adapterPosition = pair.first;
        ResMessages.OriginalMessage comment = pair.second;
        if (adapterPosition < 0) {
            return;
        }

        pollDetailView.notifyDataSetChanged();

        Action1<Throwable> errorAction = e -> {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            pollDetailView.showCommentDeleteErrorToast();

            if (adapterPosition > 0 && comment != null && comment.id > 0) {
                pollDetailDataModel.addPollComment(adapterPosition, comment);
                pollDetailView.notifyDataSetChanged();
            }

            if (e instanceof RetrofitException) {
                pollDetailModel.trackPollCommentDeleteFail(((RetrofitException) e).getResponseCode());
            }
        };

        if (messageType == MessageItem.TYPE_STICKER_COMMNET) {
            deleteStickerComment(messageType, messageId, errorAction);
        } else {
            deleteComment(messageId, feedbackId, errorAction);
        }

    }

    void deleteComment(long messageId, long feedbackId, Action1<Throwable> errorAction) {
        pollDetailModel.getCommentDeleteObservable(messageId, feedbackId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    pollDetailModel.trackPollCommentDeleteSuccess(messageId);
                }, errorAction);
    }

    void deleteStickerComment(int messageType, long messageId, Action1<Throwable> errorAction) {
        pollDetailModel.getStickerCommentDeleteObservable(messageId, messageType)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    pollDetailModel.trackPollCommentDeleteSuccess(messageId);
                }, errorAction);
    }

    @Override
    public void onPollDeleteAction(long pollId) {
        if (!NetworkCheckUtil.isConnected()) {
            pollDetailView.showCheckNetworkDialog(false);
            return;
        }

        pollDetailModel.getPollDeleteObservable(pollId)
                .doOnNext(resDeletePoll -> {
                    ResMessages.Link linkMessage = resDeletePoll.getLinkMessage();
                    Poll poll = linkMessage != null ? linkMessage.poll : null;
                    pollDetailModel.upsertPoll(poll);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resDeletePoll -> {
                    pollDetailView.showPollDeleteSuccessToast();
                    pollDetailView.finish();

                    FutureTrack.Builder trackBuilder = new FutureTrack.Builder()
                            .event(SprinklerEvents.PollDeleted)
                            .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                            .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                            .property(PropertyKey.ResponseSuccess, true)
                            .property(PropertyKey.TeamId, TeamInfoLoader.getInstance().getTeamId())
                            .property(PropertyKey.MemberId, TeamInfoLoader.getInstance().getMyId());

                    Poll poll = resDeletePoll.getLinkMessage().poll;
                    long topicId = poll != null ? poll.getTopicId() : -1;

                    if (topicId > 0) {
                        trackBuilder.property(PropertyKey.TopicId, topicId);
                    }

                    trackBuilder.property(PropertyKey.PollId, pollId);

                    AnalyticsUtil.trackSprinkler(trackBuilder.build());
                }, e -> {
                    pollDetailView.dismissProgress();

                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    pollDetailView.showUnExpectedErrorToast();

                    Poll poll = pollDetailDataModel.getPoll();

                    if (e instanceof RetrofitException) {
                        RetrofitException e1 = (RetrofitException) e;
                        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                                .event(SprinklerEvents.PollDeleted)
                                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                                .property(PropertyKey.ResponseSuccess, false)
                                .property(PropertyKey.ErrorCode, e1.getStatusCode())
                                .property(PropertyKey.TeamId, TeamInfoLoader.getInstance().getTeamId())
                                .property(PropertyKey.MemberId, TeamInfoLoader.getInstance().getMyId())
                                .property(PropertyKey.TopicId, poll.getTopicId())
                                .property(PropertyKey.PollId, pollId)
                                .build());
                    }

                });
    }

    @Override
    public void onPollFinishAction(long pollId) {
        if (!NetworkCheckUtil.isConnected()) {
            pollDetailView.showCheckNetworkDialog(false);
            return;
        }

        pollDetailModel.getPollFinishObservable(pollId)
                .doOnNext(resDeletePoll -> {
                    ResMessages.Link linkMessage = resDeletePoll.getLinkMessage();
                    Poll poll = linkMessage != null ? linkMessage.poll : null;
                    pollDetailModel.upsertPoll(poll);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(resDeletePoll -> {
                    pollDetailView.dismissProgress();

                    ResMessages.Link linkMessage = resDeletePoll.getLinkMessage();
                    if (linkMessage == null || linkMessage.poll == null || linkMessage.poll.getId() <= 0) {
                        return;
                    }

                    Poll poll = linkMessage.poll;

                    pollDetailDataModel.removePollDetailRow();
                    pollDetailDataModel.replacePollDetails(poll);
                    pollDetailView.notifyDataSetChanged();

                    pollDetailView.initPollDetailExtras(poll);

                    AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                            .event(SprinklerEvents.PollFinished)
                            .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                            .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                            .property(PropertyKey.ResponseSuccess, true)
                            .property(PropertyKey.TeamId, TeamInfoLoader.getInstance().getTeamId())
                            .property(PropertyKey.MemberId, TeamInfoLoader.getInstance().getMyId())
                            .property(PropertyKey.TopicId, poll.getTopicId())
                            .property(PropertyKey.PollId, pollId)
                            .build());
                }, e -> {
                    pollDetailView.dismissProgress();

                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    pollDetailView.showUnExpectedErrorToast();
                    if (e instanceof RetrofitException) {
                        RetrofitException e1 = (RetrofitException) e;
                        Poll poll = pollDetailDataModel.getPoll();
                        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                                .event(SprinklerEvents.PollFinished)
                                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                                .property(PropertyKey.ResponseSuccess, false)
                                .property(PropertyKey.ErrorCode, e1.getStatusCode())
                                .property(PropertyKey.TeamId, TeamInfoLoader.getInstance().getTeamId())
                                .property(PropertyKey.TopicId, poll.getTopicId())
                                .property(PropertyKey.MemberId, TeamInfoLoader.getInstance().getMyId())
                                .property(PropertyKey.PollId, pollId)
                                .build());
                    }
                });
    }

    @Override
    public void onTopicDeleted(long topicId) {
        Poll poll = pollDetailDataModel.getPoll();
        if (poll == null || poll.getId() <= 0) {
            return;
        }

        if (poll.getTopicId() == topicId) {
            pollDetailView.finish();
        }
    }

    @Override
    public void clearAllEventQueue() {
        if (reInitializePollDetailQueueSubs != null
                && !(reInitializePollDetailQueueSubs.isUnsubscribed())) {
            reInitializePollDetailQueueSubs.unsubscribe();
        }

        if (starredStateChangeQueueSubs != null
                && !(starredStateChangeQueueSubs.isUnsubscribed())) {
            starredStateChangeQueueSubs.unsubscribe();
        }
    }

    @Override
    public void onRequestShowPollItemParticipants(Poll poll, Poll.Item item) {
        if (poll.isAnonymous()) {
            pollDetailView.showPollIsAnonymousToast();
        } else {
            if (item.getVotedCount() <= 0) {
                pollDetailView.showEmptyParticipantsToast();
            } else {
                pollDetailView.showPollItemParticipants(poll.getId(), item);
            }
        }
    }

    @Override
    public void onChangePollStarredState(Poll poll) {
        poll.setIsStarred(!poll.isStarred());
        pollDetailView.notifyDataSetChanged();

        starredStateChangeQueue.onNext(Pair.create(poll.getMessageId(), poll.isStarred()));
    }

    @Override
    public void onRequestShowPollParticipants(Poll poll) {
        if (poll.getVotedCount() <= 0) {
            pollDetailView.showEmptyParticipantsToast();
        } else {
            pollDetailView.showPollParticipants(poll.getId());
        }
    }

}
