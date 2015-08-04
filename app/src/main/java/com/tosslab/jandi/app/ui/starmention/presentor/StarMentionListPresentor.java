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
    private int page = 1;
    private int pagePerCount = 20;
    private View view;

    @Background
    public void addMentionMessagesToList(String listType) {
//        if (page <= 1) {
//            view.onShowMoreProgressBar();
//        }
        try {

            if (listType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
                List<StarMentionVO> starMentionList = starMentionListModel.
                        getStarMentionedMessages(
                                StarMentionListActivity.TYPE_MENTION_LIST, page, pagePerCount);
                view.onAddAndShowList(starMentionList);
            } else if (listType.equals(StarMentionListActivity.TYPE_STAR_ALL)) {
                List<StarMentionVO> starMentionList = starMentionListModel.
                        getStarMentionedMessages(
                                StarMentionListActivity.TYPE_STAR_ALL, page, pagePerCount);
                view.onAddAndShowList(starMentionList);
            } else if (listType.equals(StarMentionListActivity.TYPE_STAR_FILES)) {
                List<StarMentionVO> starMentionList = starMentionListModel.
                        getStarMentionedMessages(
                                StarMentionListActivity.TYPE_STAR_FILES, page, pagePerCount);
                view.onAddAndShowList(starMentionList);
            }

            if (starMentionListModel.getTotalCount() > starMentionListModel.getListCount()) {
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
        } finally {
//            if (page <= 1) {
//                view.onDismissMoreProgressBar();
//            }
            page++;
        }
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

    public boolean executeLongClickEvent(StarMentionVO starMentionVO) {
//        try {
//            view.onShowDialog(starMentionVO.getTeamId(), starMentionVO.getMessageId());
//            return true;
//        } catch (RetrofitError e) {
//            e.printStackTrace();
//            return false;
//        }
        return false;
    }

    @Background
    public void unregistStarredMessage(int teamId, int messageId) {
        try {
            starMentionListModel.unregistStarredMessage(teamId, messageId);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }

    }

    public void setView(View view) {
        this.view = view;
    }

    public int getTotalCount() {
        return starMentionListModel.getTotalCount();
    }

    public static interface View {

        public void onAddAndShowList(List<StarMentionVO> starMentionMessageList);

        public void onShowMoreProgressBar();

        public void onDismissMoreProgressBar();

        public void onSetNoMoreState();

        public void onSetReadyMoreState();

//        public void onShowDialog(int teamId, int messageId);

    }

}
