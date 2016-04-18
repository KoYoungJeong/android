package com.tosslab.jandi.app.ui.message.v2.model;

import android.support.annotation.Nullable;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AnnouncementRepository;
import com.tosslab.jandi.app.network.client.rooms.AnnounceApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

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
        FormattedEntity entity = EntityManager.getInstance().getEntityById(entityId);
        return entity.announcementOpened;
    }

    @Nullable
    public ResAnnouncement getAnnouncement(long teamId, long topicId) {
        ResAnnouncement announcement = null;
        try {

            if (!NetworkCheckUtil.isConnected()) {
                return AnnouncementRepository.getRepository().getAnnounce(topicId);
            }

            announcement = announceApi.get().getAnnouncement(teamId, topicId);
            AnnouncementRepository.getRepository().upsertAnnounce(announcement);
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
        return announcement;
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
        long memberId = EntityManager.getInstance().getMe().getUser().id;

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
                AnnouncementRepository.getRepository().deleteAnnouncement(topicId);
            }
        } catch (RetrofitException e) {
            e.printStackTrace();
        }
    }

}
