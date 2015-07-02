package com.tosslab.jandi.app.ui.message.v2.model.announcement;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResAnnouncement;

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

    public boolean isAnnouncementOpened(int entityId) {
        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        return entity.announcementOpened;
    }

    public ResAnnouncement getAnnouncement(int teamId, int topicId) {
        ResAnnouncement announcement = null;
        try {
            announcement = RequestApiManager.getInstance().getAnnouncement(teamId, topicId);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
        return announcement;
    }

    @Background
    public void createAnnouncement(int teamId, int topicId, int messageId) {
        try {
            ReqCreateAnnouncement reqCreateAnnouncement = new ReqCreateAnnouncement(messageId);
            RequestApiManager.getInstance().createAnnouncement(teamId, topicId, reqCreateAnnouncement);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    @Background
    public void updateAnnouncementStatus(int teamId, int topicId, boolean isOpened) {
        int memberId = EntityManager.getInstance(context).getMe().getUser().id;

        try {
            ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus
                    = new ReqUpdateAnnouncementStatus(topicId, isOpened);
            RequestApiManager.getInstance()
                    .updateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

    @Background
    public void deleteAnnouncement(int teamId, int topicId) {
        try {
            RequestApiManager.getInstance().deleteAnnouncement(teamId, topicId);
        } catch (RetrofitError e) {
            e.printStackTrace();
        }
    }

}
