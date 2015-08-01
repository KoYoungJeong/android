package com.tosslab.jandi.app.ui.starmention.model;

import android.content.Context;
import android.util.Log;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.database.account.JandiAccountDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResMentioned;
import com.tosslab.jandi.app.network.models.commonobject.CursorObject;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tee on 15. 7. 30..
 */

@EBean
public class StarMentionListModel {

    List<StarMentionVO> starMentionList;
    private int totalCount = 0;
    private int recordCount = 0;

    public ResMentioned getMentionRawDatas(int page, int pagePerCount) {
        int teamId = getTeamId(JandiApplication.getContext());
        return RequestApiManager.getInstance().getMentionedMessagesByTeamApi(teamId, page, pagePerCount);
    }

    public ResMentioned getStarredRawDatas() {
        int teamId = getTeamId(JandiApplication.getContext());
        return RequestApiManager.getInstance().getStarredMessages(teamId);
    }

    public List<StarMentionVO> addMentionMessagesToList(int page, int pagePerCount) {

        if (starMentionList == null) {
            starMentionList = new ArrayList<>();
        }

        ResMentioned resMentioned = getMentionRawDatas(page, pagePerCount);
        Log.e("xx", resMentioned.toString());
        List<StarMentionedMessageObject> starMentionedMessageObjectList = resMentioned.getRecords();
        CursorObject mentionedCursorList = resMentioned.getCursor();
        totalCount = mentionedCursorList.getTotalCount();
        recordCount = mentionedCursorList.getRecordCount();
        for (StarMentionedMessageObject starMentionedMessageObject : starMentionedMessageObjectList) {
            StarMentionVO starMentionVO = new StarMentionVO();
            String type = starMentionedMessageObject.getMessage().contentType;
            if (type.equals("text")) {
                starMentionVO.setContentType(StarMentionVO.Type.Text.getValue());
                starMentionVO.setRoomName(starMentionedMessageObject.getRoom().name);
            } else if (type.equals("comment")) {
                starMentionVO.setContentType(StarMentionVO.Type.Comment.getValue());
                starMentionVO.setFileName(starMentionedMessageObject.getMessage().feedbackTitle);
            }

            FormattedEntity entity = EntityManager.getInstance(JandiApplication.getContext())
                    .getEntityById(starMentionedMessageObject.getMessage().writerId);

            starMentionVO.setWriterName(entity.getUser().name);
            starMentionVO.setWriterPictureUrl(entity.getUserSmallProfileUrl());
            starMentionVO.setContent(starMentionedMessageObject.getMessage().contentBody);
            starMentionVO.setUpdatedAt(starMentionedMessageObject.getCreatedAt());
            starMentionList.add(starMentionVO);
        }
        return starMentionList;

    }

    public List<StarMentionVO> addStarredMessagesToList(String categoryType) {

        if (starMentionList == null) {
            starMentionList = new ArrayList<>();
        }

        ResMentioned resMentioned = getStarredRawDatas();
        List<StarMentionedMessageObject> starMentionedMessageObjectList = resMentioned.getRecords();
        CursorObject mentionedCursorList = resMentioned.getCursor();
        totalCount = mentionedCursorList.getTotalCount();
        recordCount = mentionedCursorList.getRecordCount();
        for (StarMentionedMessageObject starMentionedMessageObject : starMentionedMessageObjectList) {
            StarMentionVO starMentionVO = new StarMentionVO();
            String type = starMentionedMessageObject.getMessage().contentType;
            FormattedEntity entity = EntityManager.getInstance(JandiApplication.getContext())
                    .getEntityById(starMentionedMessageObject.getMessage().writerId);
            starMentionVO.setWriterName(entity.getUser().name);
            starMentionVO.setWriterPictureUrl(entity.getUserSmallProfileUrl());

            if (type.equals("text")) {
                starMentionVO.setContentType(StarMentionVO.Type.Text.getValue());
                starMentionVO.setRoomName(starMentionedMessageObject.getRoom().name);
            }

            if (type.equals("comment")) {
                starMentionVO.setContentType(StarMentionVO.Type.Comment.getValue());
                starMentionVO.setFileName(starMentionedMessageObject.getMessage().feedbackTitle);
            }

            if (!type.equals("file")) {
                starMentionVO.setContent(starMentionedMessageObject.getMessage().contentBody);
            }

            if (type.equals("file")) {
                starMentionVO.setContentType(StarMentionVO.Type.File.getValue());
                starMentionVO.setFileName(starMentionedMessageObject.getMessage().contentTitle);
            }

            starMentionVO.setUpdatedAt(starMentionedMessageObject.getCreatedAt());

            starMentionList.add(starMentionVO);

        }
        return starMentionList;

    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public int getTeamId(Context context) {
        return JandiAccountDatabaseManager.getInstance(context).getSelectedTeamInfo().getTeamId();
    }

}