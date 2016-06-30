package com.tosslab.jandi.app.ui.poll.detail.presenter;

import com.tosslab.jandi.app.events.poll.RequestShowPollParticipantsEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.team.room.TopicRoom;

import java.util.Collection;
import java.util.List;

/**
 * Created by tonyjs on 16. 6. 14..
 */
public interface PollDetailPresenter {

    String TAG = PollDetailPresenter.class.getSimpleName();

    void onInitializePollDetail(long pollId);

    void reInitializePollDetail(long pollId);

    void onVote(long pollId, Collection<Integer> seqs);

    void onCommentCreated(ResMessages.Link linkComment);

    void onCommentDeleted(ResMessages.Link linkComment);

    void onSendComment(long pollId, String message, List<MentionObject> mentions);

    void onSendCommentWithSticker(long pollId, long stickerGroupId, String stickerId, String message, List<MentionObject> mentions);

    void onPollDataChanged(Poll poll);

    void joinAndMove(TopicRoom topic);

    void onChangeCommentStarredState(long messageId, boolean starred);

    void onDeleteComment(int messageType, long messageId, long feedbackId);

    void onPollDeleteAction(long pollId);

    void onPollFinishAction(long pollId);

    void onRequestShowPollParticipantsAction(RequestShowPollParticipantsEvent event);

    interface View {
        void showProgress();

        void dismissProgress();

        void showKeyboard();

        void hideKeyboard();

        void copyToClipboard(String text);

        void initPollDetailExtras(Poll poll);

        void showCheckNetworkDialog(boolean shouldFinishWhenConfirm);

        void showUnExpectedErrorToast();

        void finish();

        void scrollToLastComment();

        void notifyDataSetChanged();

        void moveToMessageListActivity(long entityId, int entityType, long roomId,
                                       boolean isStarred);

        void showCommentStarredSuccessToast();

        void showCommentUnStarredSuccessToast();

        void showCommentDeleteErrorToast();

        void showEmptyParticipantsToast();

        void showAllParticipants(long pollId);

        void showPollIsAnonymousToast();

        void showParticipants(long pollId, Poll.Item item);

        void showPollDeleteSuccessToast();
    }
}
