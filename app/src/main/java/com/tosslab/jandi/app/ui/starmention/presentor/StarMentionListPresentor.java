package com.tosslab.jandi.app.ui.starmention.presentor;

import android.app.Activity;
import android.content.Intent;
import android.widget.Toast;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.starmention.model.StarMentionListModel;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

import retrofit.RetrofitError;

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
            List<StarMentionVO> starMentionList;
            starMentionList = starMentionListModel.
                    getStarMentionedMessages(listType, requestCount);
            view.onAddAndShowList(starMentionList);
            if (starMentionListModel.hasMore()) {
                view.onSetReadyMoreState();
            } else {
                view.onSetNoMoreState();
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            view.onDismissMoreProgressBar();
        }
    }

    public void executeClickEvent(StarMentionVO starMentionVO, Activity activity) {
        int contentType = starMentionVO.getContentType();
        if (contentType == StarMentionVO.Type.Text.getValue()) {
            boolean isJoinedTopic = false;
            EntityManager entityManager = EntityManager.getInstance();
            FormattedEntity formattedEntity = entityManager.getEntityById(starMentionVO.getRoomId());

            if (formattedEntity != null) {

                if (!formattedEntity.isUser()) {
                    for (Integer memberId : formattedEntity.getMembers()) {
                        if (memberId == entityManager.getMe().getId()) {
                            isJoinedTopic = true;
                        }
                    }
                } else {
                    isJoinedTopic = true;
                }
            }
            if (isJoinedTopic) {
                MessageListV2Activity_.intent(activity)
                        .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                        .teamId(starMentionVO.getTeamId())
                        .entityId(starMentionVO.getRoomId())
                        .entityType(starMentionVO.getRoomType())
                        .roomId(starMentionVO.getRoomType() != JandiConstants.TYPE_DIRECT_MESSAGE ?
                                starMentionVO.getRoomId() : -1)
                        .isFromSearch(true)
                        .lastMarker(starMentionVO.getLinkId()).start();
            } else {
                Toast.makeText(activity,
                        R.string.jandi_starmention_no_longer_in_topic, Toast.LENGTH_SHORT).show();
            }
        } else if (contentType == StarMentionVO.Type.Comment.getValue()
                || contentType == StarMentionVO.Type.File.getValue()) {
            FileDetailActivity_
                    .intent(activity)
                    .fileId(starMentionVO.getFileId())
                    .selectMessageId(starMentionVO.getMessageId())
                    .startForResult(JandiConstants.TYPE_FILE_DETAIL_REFRESH);
        }
    }

    public boolean executeLongClickEvent(StarMentionVO starMentionVO, int position) {
        try {
            view.onShowDialog(starMentionVO.getTeamId(), starMentionVO.getMessageId(), position);
            return true;
        } catch (RetrofitError e) {
            e.printStackTrace();
            return false;
        }
    }

    @Background
    public void unregistStarredMessage(int teamId, int messageId, int position) {
        try {
            starMentionListModel.unregistStarredMessage(teamId, messageId);
            view.showSuccessToast(JandiApplication.getContext().getString(R.string.jandi_unpinned_message));
            view.onRemoveItem(position);
        } catch (RetrofitError e) {
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

        starMentionListModel.refreshList();
        addStarMentionMessagesToList(listType);

    }

    @Background
    public void reloadStartList(String listType, int requestCount) {
        starMentionListModel.refreshList();
        addStarMentionMessagesToList(listType, requestCount);
    }

    public void setView(View view) {
        this.view = view;
    }

    public boolean isEmpty() {
        return starMentionListModel.isEmpty();
    }

    public interface View {

        void onAddAndShowList(List<StarMentionVO> starMentionMessageList);

        void onShowMoreProgressBar();

        void onDismissMoreProgressBar();

        void onSetNoMoreState();

        void onSetReadyMoreState();

        void onShowDialog(int teamId, int messageId, int position);

        void onRemoveItem(int position);

        void showSuccessToast(String message);

        void showCheckNetworkDialog();
    }

}
