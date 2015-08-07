package com.tosslab.jandi.app.ui.starmention.model;

import android.content.Context;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
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
    private boolean isFirstDatas = true;
    private int lastId = 0;
    private int pagePerCount = 20;
    private boolean hasMore = false;
    private boolean isEmpty = false;
    private String currentType = null;

    public ResStarMentioned getMentionRawDatas(Integer messageId, int count) throws RetrofitError {
        int teamId = getTeamId(JandiApplication.getContext());
        return RequestApiManager.getInstance().getMentionedMessagesByTeamApi(teamId, messageId, count);
    }

    public ResStarMentioned getStarredRawDatas(String categoryType, Integer starredId,
                                               int count) throws RetrofitError {
        int teamId = getTeamId(JandiApplication.getContext());
        if (categoryType.equals(StarMentionListActivity.TYPE_STAR_FILES)) {
            return RequestApiManager.getInstance().getStarredMessagesByTeamApi(
                    teamId, starredId, count, "file");
        }

        return RequestApiManager.getInstance().getStarredMessagesByTeamApi(
                teamId, starredId, count, null);

    }

    public void unregistStarredMessage(int teamId, int messageId) {
        try {
            RequestApiManager.getInstance()
                    .unregistStarredMessageByTeamApi(teamId, messageId);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    public List<StarMentionVO> getStarMentionedMessages(String categoryType) throws RetrofitError {

        starMentionList = new ArrayList<>();

        ResStarMentioned resStarMentioned = null;

        currentType = categoryType;

        if (categoryType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
            if (isFirstDatas) {
                resStarMentioned = getMentionRawDatas(null, pagePerCount);
            } else {
                resStarMentioned = getMentionRawDatas(lastId, pagePerCount);
            }
        } else {
            if (isFirstDatas) {
                resStarMentioned = getStarredRawDatas(categoryType, null, pagePerCount);
            } else {
                resStarMentioned = getStarredRawDatas(categoryType, lastId, pagePerCount);
            }
        }

        hasMore = resStarMentioned.isHasMore();

        List<StarMentionedMessageObject> starMentionedMessageObjectList = resStarMentioned.getRecords();
        for (StarMentionedMessageObject starMentionedMessageObject : starMentionedMessageObjectList) {

            StarMentionVO starMentionVO = new StarMentionVO();
            String type = starMentionedMessageObject.getMessage().contentType;
            int messageId = 0;

            if (categoryType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
                messageId = starMentionedMessageObject.getMessage().id;
            } else {
                messageId = starMentionedMessageObject.getStarredId();
            }

            FormattedEntity entity = EntityManager.getInstance(JandiApplication.getContext())
                    .getEntityById(starMentionedMessageObject.getMessage().writerId);
            starMentionVO.setWriterName(entity.getUser().name);
            starMentionVO.setWriterPictureUrl(entity.getUserSmallProfileUrl());
            starMentionVO.setTeamId(starMentionedMessageObject.getTeamId());
            starMentionVO.setMessageId(messageId);
            lastId = messageId;

            if (type.equals("text")) {
                starMentionVO.setContentType(StarMentionVO.Type.Text.getValue());

                if (starMentionedMessageObject.getRoom().type.equals("channel")) {
                    starMentionVO.setRoomType(JandiConstants.TYPE_PUBLIC_TOPIC);
                    starMentionVO.setRoomName(starMentionedMessageObject.getRoom().name);
                    starMentionVO.setRoomId(starMentionedMessageObject.getRoom().id);
                } else if (starMentionedMessageObject.getRoom().type.equals("privateGroup")) {
                    starMentionVO.setRoomType(JandiConstants.TYPE_PRIVATE_TOPIC);
                    starMentionVO.setRoomName(starMentionedMessageObject.getRoom().name);
                    starMentionVO.setRoomId(starMentionedMessageObject.getRoom().id);
                } else if (starMentionedMessageObject.getRoom().type.equals("chat")) {
                    starMentionVO.setRoomType(JandiConstants.TYPE_DIRECT_MESSAGE);

                    String userId = starMentionedMessageObject.getRoom().name.replaceAll(
                            EntityManager.getInstance(JandiApplication.getContext()).getMe().getId() + "", "");
                    userId = userId.replace(":", "");


                    starMentionVO.setRoomName(EntityManager.getInstance(
                            JandiApplication.getContext()).getEntityById(Integer.valueOf(userId)).getName());
                    starMentionVO.setRoomId(Integer.valueOf(userId));
                }
                starMentionVO.setLinkId(starMentionedMessageObject.getLinkId());
            }


            if (type.equals("comment")) {
                starMentionVO.setContentType(StarMentionVO.Type.Comment.getValue());
                starMentionVO.setFileName(starMentionedMessageObject.getMessage().feedbackTitle);
                starMentionVO.setFileId(starMentionedMessageObject.getMessage().feedbackId);
            }

            if (!type.equals("file")) {
                starMentionVO.setContent(starMentionedMessageObject.getMessage().content.body);
                starMentionVO.setMentions(starMentionedMessageObject.getMessage().mentions);
            }

            if (type.equals("file")) {
                starMentionVO.setContentType(StarMentionVO.Type.File.getValue());
                starMentionVO.setFileName(starMentionedMessageObject.getMessage().content.title);
                starMentionVO.setFileId(starMentionedMessageObject.getMessage().id);
                starMentionVO.setImageResource(MimeTypeUtil.getMimeTypeIconImage(
                        starMentionedMessageObject.getMessage().content.serverUrl,
                        starMentionedMessageObject.getMessage().content.icon));
            }

            starMentionVO.setUpdatedAt(starMentionedMessageObject.getCreatedAt());
            starMentionList.add(starMentionVO);
        }

        if (isFirstDatas && starMentionList.size() == 0) {
            isEmpty = true;
        }

        isFirstDatas = false;

        return starMentionList;

    }

    public void refreshList() throws RetrofitError {
        starMentionList = null;
        isFirstDatas = true;
        lastId = 0;
        pagePerCount = 20;
        hasMore = false;
        isEmpty = false;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public boolean isFirst() {
        return isFirstDatas;
    }

    public boolean hasMore() {
        return hasMore;
    }

    public int getTeamId(Context context) {
        return AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
    }

}