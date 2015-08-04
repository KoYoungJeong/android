package com.tosslab.jandi.app.ui.starmention.presentor;

import android.app.Activity;
import android.content.Intent;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Activity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
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

    @Background
    public void addMentionMessagesToList(String listType) {
        if (!starMentionListModel.isFirst()) {
            view.onShowMoreProgressBar();
        }
        try {
            if (listType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
                List<StarMentionVO> starMentionList = starMentionListModel.
                        getStarMentionedMessages(StarMentionListActivity.TYPE_MENTION_LIST);
                view.onAddAndShowList(starMentionList);
            } else if (listType.equals(StarMentionListActivity.TYPE_STAR_ALL)) {
                List<StarMentionVO> starMentionList = starMentionListModel.
                        getStarMentionedMessages(StarMentionListActivity.TYPE_STAR_ALL);
                view.onAddAndShowList(starMentionList);
            } else if (listType.equals(StarMentionListActivity.TYPE_STAR_FILES)) {
                List<StarMentionVO> starMentionList = starMentionListModel.
                        getStarMentionedMessages(StarMentionListActivity.TYPE_STAR_FILES);
                view.onAddAndShowList(starMentionList);
            }
            if (starMentionListModel.hasMore()) {
                view.onSetReadyMoreState();
            } else {
                view.onSetNoMoreState();
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
        view.onDismissMoreProgressBar();
    }

    public void executeClickEvent(StarMentionVO starMentionVO, Activity activity) {
        int contentType = starMentionVO.getContentType();
        if (contentType == StarMentionVO.Type.Text.getValue()) {
            MessageListV2Activity_.intent(activity)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .teamId(starMentionVO.getTeamId())
                    .entityId(starMentionVO.getRoomId())
                    .entityType(starMentionVO.getRoomType())
                    .roomId(starMentionVO.getRoomType() != JandiConstants.TYPE_DIRECT_MESSAGE ?
                            starMentionVO.getRoomId() : starMentionVO.getRoomType())
                    .isFromSearch(true)
                    .lastMarker(starMentionVO.getLinkId()).start();
        } else if (contentType == StarMentionVO.Type.Comment.getValue()
                || contentType == StarMentionVO.Type.File.getValue()) {
            FileDetailActivity_
                    .intent(activity)
                    .fileId(starMentionVO.getFileId())
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
            view.onRemoveItem(position);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    public void setView(View view) {
        this.view = view;
    }

    public boolean isEmpty() {
        return starMentionListModel.isEmpty();
    }

    public static interface View {

        public void onAddAndShowList(List<StarMentionVO> starMentionMessageList);

        public void onShowMoreProgressBar();

        public void onDismissMoreProgressBar();

        public void onSetNoMoreState();

        public void onSetReadyMoreState();

        public void onShowDialog(int teamId, int messageId, int position);

        public void onRemoveItem(int position);

    }

}
