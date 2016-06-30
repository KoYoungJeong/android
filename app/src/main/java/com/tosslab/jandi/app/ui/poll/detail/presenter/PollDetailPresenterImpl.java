package com.tosslab.jandi.app.ui.poll.detail.presenter;

import android.util.Log;
import android.util.Pair;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.events.poll.RequestShowPollParticipantsEvent;
import com.tosslab.jandi.app.events.messages.StarredInfoChangeEvent;
import com.tosslab.jandi.app.lists.messages.MessageItem;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.poll.detail.adapter.model.PollDetailDataModel;
import com.tosslab.jandi.app.ui.poll.detail.dto.PollDetail;
import com.tosslab.jandi.app.ui.poll.detail.model.PollDetailModel;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.util.Collection;
import java.util.List;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public class PollDetailPresenterImpl implements PollDetailPresenter {

    private final PollDetailModel pollDetailModel;
    private final PollDetailPresenter.View pollDetailView;
    private final PollDetailDataModel pollDetailDataModel;

    @Inject
    public PollDetailPresenterImpl(PollDetailModel pollDetailModel,
                                   PollDetailDataModel pollDetailDataModel,
                                   PollDetailPresenter.View pollDetailView) {
        this.pollDetailModel = pollDetailModel;
        this.pollDetailDataModel = pollDetailDataModel;
        this.pollDetailView = pollDetailView;
    }

    @Override
    public void onInitializePollDetail(final long pollId) {
        if (!NetworkCheckUtil.isConnected()) {
            pollDetailView.showCheckNetworkDialog(true);
            return;
        }

        pollDetailView.showProgress();
        pollDetailModel.getPollDetailObservable(pollId, new PollDetail())
                .concatMap(pollDetail -> pollDetailModel.getPollCommentsObservable(pollId, pollDetail))
                .doOnNext(pollDetail -> pollDetailModel.upsertPoll(pollDetail.getPoll()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pollDetail -> {
                    pollDetailView.dismissProgress();

                    Poll poll = pollDetail.getPoll();
                    if (poll == null || poll.getId() <= 0) {
                        pollDetailView.showUnExpectedErrorToast();
                        pollDetailView.finish();
                        return;
                    }

                    pollDetailDataModel.setPollDetails(poll);

                    List<ResMessages.OriginalMessage> pollComments = pollDetail.getPollComments();
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

        pollDetailModel.getPollDetailObservable(pollId, new PollDetail())
                .concatMap(pollDetail -> pollDetailModel.getPollCommentsObservable(pollId, pollDetail))
                .doOnNext(pollDetail -> pollDetailModel.upsertPoll(pollDetail.getPoll()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pollDetail -> {
                    pollDetailDataModel.removeAllRows();

                    pollDetailDataModel.setPollDetails(pollDetail.getPoll());

                    List<ResMessages.OriginalMessage> pollComments = pollDetail.getPollComments();
                    if (pollComments != null && !pollComments.isEmpty()) {
                        pollDetailModel.sortByDate(pollComments);
                        pollDetailDataModel.addPollComments(pollComments);
                    }

                    pollDetailView.notifyDataSetChanged();

                    pollDetailView.initPollDetailExtras(pollDetail.getPoll());

                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));
                });
    }

    @Override
    public void onVote(long pollId, Collection<Integer> seqs) {
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
                    if (linkMessage == null || linkMessage.poll == null || linkMessage.poll.getId() <= 0) {
                        return;
                    }

                    Poll poll = linkMessage.poll;

                    pollDetailDataModel.removePollDetailRow();
                    pollDetailDataModel.replacePollDetails(poll);
                    pollDetailView.notifyDataSetChanged();

                    pollDetailView.initPollDetailExtras(poll);
                }, e -> {
                    pollDetailView.dismissProgress();

                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    pollDetailView.showUnExpectedErrorToast();
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
                }, throwable -> LogUtil.e(TAG, Log.getStackTraceString(throwable)));
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
                }, throwable -> LogUtil.e(TAG, Log.getStackTraceString(throwable)));
    }

    @Override
    public void onPollDataChanged(Poll poll) {
        Observable.just(poll)
                .subscribeOn(AndroidSchedulers.mainThread())
                .subscribe(poll1 -> {
                    pollDetailDataModel.removePollDetailRow();
                    pollDetailDataModel.replacePollDetails(poll1);
                    pollDetailView.notifyDataSetChanged();

                    pollDetailView.initPollDetailExtras(poll1);
                }, e -> {
                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    pollDetailView.showUnExpectedErrorToast();
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
                    }, e -> {
                        LogUtil.e(TAG, Log.getStackTraceString(e));
                    });
        } else {

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
                    }, e -> {
                        LogUtil.e(TAG, Log.getStackTraceString(e));
                    });
        }
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

        Action1<Throwable> error = e -> {
            LogUtil.e(TAG, Log.getStackTraceString(e));

            pollDetailView.showCommentDeleteErrorToast();

            if (adapterPosition > 0 && comment != null && comment.id > 0) {
                pollDetailDataModel.addPollComment(adapterPosition, comment);
                pollDetailView.notifyDataSetChanged();
            }
        };

        if (messageType == MessageItem.TYPE_STICKER_COMMNET) {
            pollDetailModel.getStickerCommentDeleteObservable(messageId, messageType)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                    }, error);
        } else {
            pollDetailModel.getCommentDeleteObservable(messageId, feedbackId)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(o -> {
                    }, error);
        }

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
                }, e -> {
                    pollDetailView.dismissProgress();

                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    pollDetailView.showUnExpectedErrorToast();
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
                }, e -> {
                    pollDetailView.dismissProgress();

                    LogUtil.e(TAG, Log.getStackTraceString(e));
                    pollDetailView.showUnExpectedErrorToast();
                });
    }

    @Override
    public void onRequestShowPollParticipantsAction(RequestShowPollParticipantsEvent event) {
        Poll poll = event.getPoll();
        // 전체 참여자
        if (event.getType() == RequestShowPollParticipantsEvent.Type.ALL) {

            showAllParticipants(poll);

        } else {
            // 옵션 참여자

            showOptionParticipants(event, poll);
        }
    }

    private void showOptionParticipants(RequestShowPollParticipantsEvent event, Poll poll) {
        if (poll.isAnonymous()) {
            pollDetailView.showPollIsAnonymousToast();
        } else {
            Poll.Item option = event.getOption();
            if (option.getVotedCount() <= 0) {
                pollDetailView.showEmptyParticipantsToast();
            } else {
                pollDetailView.showParticipants(poll.getId(), option);
            }
        }
    }

    private void showAllParticipants(Poll poll) {
        if (poll.getVotedCount() <= 0) {
            pollDetailView.showEmptyParticipantsToast();
        } else {
            pollDetailView.showAllParticipants(poll.getId());
        }
    }

}
