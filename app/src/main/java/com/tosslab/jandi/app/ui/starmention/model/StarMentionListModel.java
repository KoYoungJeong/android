package com.tosslab.jandi.app.ui.starmention.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.commonobject.CursorObject;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 7. 30..
 */

@EBean
public class StarMentionListModel {

    List<StarMentionVO> starMentionList;
    private int totalCount = 0;
    private int listCount = 0;
    private int page = 0;
    private int pagePerCount = 0;

    public ResStarMentioned getMentionRawDatas(int page, int pagePerCount) throws RetrofitError {
        int teamId = getTeamId(JandiApplication.getContext());
        return RequestApiManager.getInstance().getMentionedMessagesByTeamApi(teamId, page, pagePerCount);
    }

    public ResStarMentioned getStarredRawDatas(String categoryType, int page,
                                               int pagePerCount) throws RetrofitError {
        int teamId = getTeamId(JandiApplication.getContext());
        if (categoryType.equals(StarMentionListActivity.TYPE_STAR_FILES)) {
            return RequestApiManager.getInstance().getStarredMessagesByTeamApi(
                    teamId, "file", page, pagePerCount);
        }

        return RequestApiManager.getInstance().getStarredMessagesByTeamApi(
                teamId, null, page, pagePerCount);
    }

    public void unregistStarredMessage(int teamId, int messageId) {
        try {
            RequestApiManager.getInstance()
                    .unregistStarredMessageByTeamApi(teamId, messageId);
        } catch (RetrofitError e) {
            e.printStackTrace();
            throw e;
        }
    }

    public List<StarMentionVO> getStarMentionedMessages(String categoryType, int page,
                                                        int pagePerCount) throws RetrofitError {

        starMentionList = new ArrayList<>();

        ResStarMentioned resStarMentioned = null;

        if (categoryType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
            resStarMentioned = getMentionRawDatas(page, pagePerCount);
        } else {
            resStarMentioned = getStarredRawDatas(categoryType, page, pagePerCount);
        }

        List<StarMentionedMessageObject> starMentionedMessageObjectList = resStarMentioned.getRecords();
        CursorObject mentionedCursorList = resStarMentioned.getCursor();
        totalCount = mentionedCursorList.getTotalCount();
        listCount += mentionedCursorList.getRecordCount();
        page = mentionedCursorList.getPage();
        pagePerCount = mentionedCursorList.getPerPage();
        for (StarMentionedMessageObject starMentionedMessageObject : starMentionedMessageObjectList) {
            StarMentionVO starMentionVO = new StarMentionVO();
            String type = starMentionedMessageObject.getMessage().contentType;
            FormattedEntity entity = EntityManager.getInstance(JandiApplication.getContext())
                    .getEntityById(starMentionedMessageObject.getMessage().writerId);
            starMentionVO.setWriterName(entity.getUser().name);
            starMentionVO.setWriterPictureUrl(entity.getUserSmallProfileUrl());
            starMentionVO.setTeamId(starMentionedMessageObject.getTeamId());
            starMentionVO.setMessageId(starMentionedMessageObject.getMessage().id);

            if (type.equals("text")) {
                starMentionVO.setContentType(StarMentionVO.Type.Text.getValue());
                starMentionVO.setRoomName(starMentionedMessageObject.getRoom().name);
                starMentionVO.setRoomId(starMentionedMessageObject.getRoom().id);
                if (starMentionedMessageObject.getRoom().type.equals("channel")) {
                    starMentionVO.setRoomType(JandiConstants.TYPE_PUBLIC_TOPIC);
                } else if (starMentionedMessageObject.getRoom().type.equals("privateGroup")) {
                    starMentionVO.setRoomType(JandiConstants.TYPE_PRIVATE_TOPIC);
                } else if (starMentionedMessageObject.getRoom().type.equals("chat")) {
                    starMentionVO.setRoomType(JandiConstants.TYPE_DIRECT_MESSAGE);
                }
                starMentionVO.setLinkId(starMentionedMessageObject.getLinkId());
            }

            if (type.equals("comment")) {
                starMentionVO.setContentType(StarMentionVO.Type.Comment.getValue());
                starMentionVO.setFileName(starMentionedMessageObject.getMessage().feedbackTitle);
                starMentionVO.setFileId(starMentionedMessageObject.getMessage().feedbackId);
            }

            if (!type.equals("file")) {
                starMentionVO.setContent(starMentionedMessageObject.getMessage().contentBody);
                starMentionVO.setMentions(starMentionedMessageObject.getMessage().mentions);
            }

            if (type.equals("file")) {
                starMentionVO.setContentType(StarMentionVO.Type.File.getValue());
                starMentionVO.setFileName(starMentionedMessageObject.getMessage().contentTitle);
                starMentionVO.setFileId(starMentionedMessageObject.getMessage().id);
                starMentionVO.setImageResource(getMimeTypeIconImageResource(
                        starMentionedMessageObject.getTeamId(),
                        starMentionedMessageObject.getMessage().id));
            }

            starMentionVO.setUpdatedAt(starMentionedMessageObject.getCreatedAt());
            starMentionList.add(starMentionVO);

        }

        return starMentionList;

    }

    public int getMimeTypeIconImageResource(int teamId, int fileId) {
        try {
            ResMessages.OriginalMessage message = RequestApiManager.getInstance()
                    .getMessage(teamId, fileId);
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) message;
            return MimeTypeUtil.getMimeTypeIconImage(
                    fileMessage.content.serverUrl, fileMessage.content.icon);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public int getListCount() {
        return listCount;
    }

    public int getCurrentPage() {
        return page;
    }


    public int getTeamId(Context context) {
        return AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
    }

}