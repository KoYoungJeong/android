package com.tosslab.jandi.app.ui.message.v2.model;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.AnnouncementRepository;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import retrofit.RetrofitError;

/**
 * Created by tonyjs on 15. 6. 24..
 */
@EBean
public class AnnouncementModel {

    @RootContext
    Context context;

    private boolean isActionFromUser = false;

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

    public ResAnnouncement getAnnouncement(long teamId, long topicId) {
        ResAnnouncement announcement = null;
        try {

            if (!NetworkCheckUtil.isConnected()) {
                return AnnouncementRepository.getRepository().getAnnounce(topicId);
            }

            announcement = RequestApiManager.getInstance().getAnnouncement(teamId, topicId);
            AnnouncementRepository.getRepository().upsertAnnounce(announcement);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
        return announcement;
    }

    public void createAnnouncement(long teamId, long topicId, long messageId) {
        try {
            ReqCreateAnnouncement reqCreateAnnouncement = new ReqCreateAnnouncement(messageId);
            RequestApiManager.getInstance().createAnnouncement(teamId, topicId, reqCreateAnnouncement);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    @Background
    public void updateAnnouncementStatus(long teamId, long topicId, boolean isOpened) {
        long memberId = EntityManager.getInstance().getMe().getUser().id;

        try {
            ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus =
                    new ReqUpdateAnnouncementStatus(topicId, isOpened);
            RequestApiManager.getInstance()
                    .updateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    public void deleteAnnouncement(long teamId, long topicId) {
        try {
            ResCommon resCommon = RequestApiManager.getInstance().deleteAnnouncement(teamId, topicId);
            if (resCommon != null) {
                AnnouncementRepository.getRepository().deleteAnnouncement(topicId);
            }
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

}
