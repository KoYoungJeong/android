package com.tosslab.jandi.app.ui.message.v2.model;

import android.support.annotation.Nullable;

import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.client.rooms.AnnounceApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.start.Announcement;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by tonyjs on 15. 6. 24..
 */
@EBean
public class AnnouncementModel {

    @Inject
    Lazy<AnnounceApi> announceApi;
    private boolean isActionFromUser = false;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public boolean isActionFromUser() {
        return isActionFromUser;
    }

    public void setActionFromUser(boolean fromUser) {
        isActionFromUser = fromUser;
    }

    public boolean isAnnouncementOpened(long entityId) {
        return TeamInfoLoader.getInstance().isAnnouncementOpened(entityId);
    }

    @Nullable
    public Announcement getAnnouncement(long teamId, long topicId) {
        return TeamInfoLoader.getInstance().getTopic(topicId).getAnnouncement();
    }

    public void createAnnouncement(long teamId, long topicId, long messageId) {
        try {
            ReqCreateAnnouncement reqCreateAnnouncement = new ReqCreateAnnouncement(messageId);
            announceApi.get().createAnnouncement(teamId, topicId, reqCreateAnnouncement);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void updateAnnouncementStatus(long teamId, long topicId, boolean isOpened) {
        long memberId = TeamInfoLoader.getInstance().getMyId();

        try {
            ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus =
                    new ReqUpdateAnnouncementStatus(topicId, isOpened);
            announceApi.get().updateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

    public void deleteAnnouncement(long teamId, long topicId) {
        try {
            ResCommon resCommon = announceApi.get().deleteAnnouncement(teamId, topicId);
            if (resCommon != null) {
                TopicRepository.getInstance().removeAnnounce(topicId);
            }
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

}
