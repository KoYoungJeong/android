package com.tosslab.jandi.app.ui.starmention.presentor;

import android.content.Intent;
import android.support.v4.app.Fragment;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.starmention.model.StarMentionListModel;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.Collection;
import java.util.List;


/**
 * Created by tee on 15. 7. 30..
 */
@EBean
public class StarMentionListPresentor {

    @Bean
    StarMentionListModel starMentionListModel;
    private View view;

    public void addStarMentionMessagesToList(String listType) {
        addStarMentionMessagesToList(listType, StarMentionListModel.DEFAULT_COUNT);
    }

    @Background
    public void addStarMentionMessagesToList(String listType, int requestCount) {
        if (!starMentionListModel.isFirst()) {
            view.onShowMoreProgressBar();
        }
        try {
            List<StarMentionVO> starMentionList = starMentionListModel.
                    getStarMentionedMessages(listType, requestCount);
            view.onAddAndShowList(starMentionList);
            if (starMentionListModel.hasMore()) {
                view.onSetReadyMoreState();
            } else {
                view.onSetNoMoreState();
            }
        } catch (RetrofitException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            view.onDismissMoreProgressBar();
        }
    }

    public void executeClickEvent(StarMentionVO starMentionVO, Fragment fragment) {
        int contentType = starMentionVO.getContentType();
        if (contentType == StarMentionVO.Type.Text.getValue()) {
            boolean isJoinedTopic = false;

            boolean isTopic = false;
            if (TeamInfoLoader.getInstance().isTopic(starMentionVO.getRoomId())
                    || TeamInfoLoader.getInstance().isUser(starMentionVO.getRoomId())) {

                if (TeamInfoLoader.getInstance().isTopic(starMentionVO.getRoomId())) {
                    isTopic = true;
                    Collection<Long> members = TeamInfoLoader.getInstance()
                            .getTopic(starMentionVO.getRoomId())
                            .getMembers();
                    for (long memberId : members) {
                        if (memberId == TeamInfoLoader.getInstance().getMyId()) {
                            isJoinedTopic = true;
                        }
                    }
                } else {
                    isTopic = false;
                    isJoinedTopic = true;
                }
            }
            if (isJoinedTopic) {
                MessageListV2Activity_.intent(fragment)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .teamId(starMentionVO.getTeamId())
                        .entityId(starMentionVO.getRoomId())
                        .entityType(starMentionVO.getRoomType())
                        .roomId(isTopic ? starMentionVO.getRoomId() : TeamInfoLoader.getInstance().getChatId(starMentionVO.getRoomId()))
                        .isFromSearch(true)
                        .lastReadLinkId(starMentionVO.getLinkId())
                        .start();
            } else {
                ColoredToast.show(fragment.getString(R.string.jandi_starmention_no_longer_in_topic));
            }
        } else if (contentType == StarMentionVO.Type.Comment.getValue()
                || contentType == StarMentionVO.Type.File.getValue()) {
            FileDetailActivity_
                    .intent(fragment)
                    .fileId(starMentionVO.getFileId())
                    .selectMessageId(starMentionVO.getMessageId())
                    .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        }
    }

    public boolean executeLongClickEvent(StarMentionVO starMentionVO, int position) {
        view.onShowDialog(starMentionVO.getTeamId(), starMentionVO.getMessageId(), position);
        return true;
    }

    @Background
    public void unregistStarredMessage(long teamId, long messageId, int position) {
        try {
            starMentionListModel.unregistStarredMessage(teamId, messageId);
            view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_unpinned_message));
            view.onRemoveItem(position);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void refreshList(String listType) {
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        try {
            starMentionListModel.refreshList();
            addStarMentionMessagesToList(listType);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void reloadStartList(String listType, int requestCount) {
        try {
            starMentionListModel.refreshList();
            addStarMentionMessagesToList(listType, requestCount);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    public void setView(View view) {
        this.view = view;
    }

    public boolean isEmpty() {
        return starMentionListModel.isEmpty();
    }

    public void onTopicDeleteEvent(long teamId, long topicId) {
        if (teamId == starMentionListModel.getTeamId()) {
            view.deleteItemOfTopic(topicId);
        }
    }

    public interface View {

        void onAddAndShowList(List<StarMentionVO> starMentionMessageList);

        void onShowMoreProgressBar();

        void onDismissMoreProgressBar();

        void onSetNoMoreState();

        void onSetReadyMoreState();

        void onShowDialog(long teamId, long messageId, int position);

        void onRemoveItem(int position);

        void showSuccessToast(String message);

        void showCheckNetworkDialog();

        void deleteItemOfTopic(long topicId);
    }

}
