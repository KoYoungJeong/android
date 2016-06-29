package com.tosslab.jandi.app.ui.starmention.model;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by tee on 15. 7. 30..
 */

@EBean
public class StarMentionListModel {

    public static final int DEFAULT_COUNT = 20;

    protected long lastId = 0;
    protected boolean isFirstDatas = true;
    protected boolean hasMore = false;
    protected boolean isEmpty = false;

    @Inject
    Lazy<MessageApi> messageApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public List<StarMentionVO> getStarMentionedMessages(String categoryType, int count) throws RetrofitException {
        int requestCount = Math.max(count, DEFAULT_COUNT);

        ResStarMentioned resStarMentioned = getRawDatas(categoryType, requestCount);

        List<StarMentionedMessageObject> starMentionedMessageObjectList = resStarMentioned.getRecords();

        List<StarMentionVO> starMentionList = makeStarMentionList(categoryType, starMentionedMessageObjectList);

        setFlags(starMentionList.size());

        hasMore = resStarMentioned.hasMore();

        return starMentionList;
    }

    public void unregistStarredMessage(long teamId, long messageId) throws RetrofitException {
        messageApi.get().unregistStarredMessage(teamId, messageId);
    }

    protected ResStarMentioned getMentionRawDatas(long messageId, int count) throws RetrofitException {
        long teamId = getTeamId();
        return messageApi.get().getMentionedMessages(teamId, messageId, count);
    }

    protected ResStarMentioned getStarredRawDatas(String categoryType, long starredId,
                                                  int count) throws RetrofitException {
        long teamId = getTeamId();
        if (categoryType.equals(StarMentionListActivity.TYPE_STAR_LIST_OF_FILES)) {
            return messageApi.get().getStarredMessages(
                    teamId, starredId, count, "file");
        }
        return messageApi.get().getStarredMessages(
                teamId, starredId, count, null);
    }

    protected ResStarMentioned getRawDatas(String categoryType, int count) throws RetrofitException {
        ResStarMentioned resStarMentioned;
        if (categoryType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
            if (isFirstDatas) {
                resStarMentioned = getMentionRawDatas(-1, count);
            } else {
                resStarMentioned = getMentionRawDatas(lastId, count);
            }
        } else {
            if (isFirstDatas) {
                resStarMentioned = getStarredRawDatas(categoryType, -1, count);
            } else {
                resStarMentioned = getStarredRawDatas(categoryType, lastId, count);
            }
        }
        return resStarMentioned;
    }

    protected List<StarMentionVO> makeStarMentionList(String categoryType,
                                                      List<StarMentionedMessageObject> starMentionedMessageObjectList) {
        List<StarMentionVO> starMentionList = new ArrayList<>();
        for (StarMentionedMessageObject starMentionedMessageObject : starMentionedMessageObjectList) {
            StarMentionVO starMentionVO = new StarMentionVO();
            String type = starMentionedMessageObject.getMessage().contentType;

            long messageId = getMessageId(categoryType, starMentionedMessageObject);

            User entity = TeamInfoLoader.getInstance()
                    .getUser(starMentionedMessageObject.getMessage().writerId);
            starMentionVO.setWriterName(entity.getName());
            starMentionVO.setWriterId(starMentionedMessageObject.getMessage().writerId);
            starMentionVO.setWriterPictureUrl(entity.getPhotoUrl());
            starMentionVO.setTeamId(starMentionedMessageObject.getTeamId());
            starMentionVO.setMessageId(starMentionedMessageObject.getMessage().id);

            setLastMessageId(categoryType, starMentionedMessageObject, messageId);

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
                            TeamInfoLoader.getInstance().getMyId() + "", "");
                    userId = userId.replace(":", "");
                    starMentionVO.setRoomName(TeamInfoLoader.getInstance().
                            getName(Integer.valueOf(userId)));
                    starMentionVO.setRoomId(Integer.valueOf(userId));
                }
                starMentionVO.setBody(starMentionedMessageObject.getMessage().content.body);
                starMentionVO.setMentions(starMentionedMessageObject.getMessage().mentions);
                starMentionVO.setLinkId(starMentionedMessageObject.getLinkId());
            } else if (type.equals("comment")) {
                starMentionVO.setFeedbackType(starMentionedMessageObject.getMessage().feedbackType);
                starMentionVO.setContentType(StarMentionVO.Type.Comment.getValue());
                starMentionVO.setFileName(starMentionedMessageObject.getMessage().feedbackTitle);
                starMentionVO.setFileId(starMentionedMessageObject.getMessage().feedbackId);
                starMentionVO.setBody(starMentionedMessageObject.getMessage().content.body);
                starMentionVO.setMentions(starMentionedMessageObject.getMessage().mentions);
            } else if (type.equals("file")) {
                starMentionVO.setContentType(StarMentionVO.Type.File.getValue());
                starMentionVO.setFileName(starMentionedMessageObject.getMessage().content.title);
                starMentionVO.setFileId(starMentionedMessageObject.getMessage().id);
                starMentionVO.setContent(starMentionedMessageObject.getMessage().content);
            }
            starMentionVO.setUpdatedAt(starMentionedMessageObject.getMessage().createdAt);
            starMentionList.add(starMentionVO);
        }

        return starMentionList;
    }

    protected void setFlags(int listSize) {
        if (isFirstDatas && listSize == 0) {
            isEmpty = true;
        }
        isFirstDatas = false;
    }

    protected void setLastMessageId(String categoryType, StarMentionedMessageObject starMentionedMessageObject, long messageId) {
        if (categoryType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
            lastId = messageId;
        } else {
            lastId = starMentionedMessageObject.getStarredId();
        }
    }

    protected long getMessageId(String categoryType, StarMentionedMessageObject starMentionedMessageObject) {
        long messageId;
        if (categoryType.equals(StarMentionListActivity.TYPE_MENTION_LIST)) {
            messageId = starMentionedMessageObject.getMessage().id;
        } else {
            messageId = starMentionedMessageObject.getStarredId();
        }
        return messageId;
    }

    public void refreshList() throws RetrofitException {
        isFirstDatas = true;
        lastId = 0;
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

    public long getTeamId() {
        return AccountRepository.getRepository().getSelectedTeamInfo().getTeamId();
    }

}