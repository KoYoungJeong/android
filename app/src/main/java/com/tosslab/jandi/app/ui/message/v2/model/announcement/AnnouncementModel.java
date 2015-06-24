package com.tosslab.jandi.app.ui.message.v2.model.announcement;

import android.content.Context;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.client.teams.TeamsApiClient;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqCreateAnnouncement;
import com.tosslab.jandi.app.network.models.ReqUpdateAnnouncementStatus;
import com.tosslab.jandi.app.network.models.ResAnnouncement;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.TokenUtil;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.rest.RestService;

/**
 * Created by tonyjs on 15. 6. 24..
 */
@EBean
public class AnnouncementModel {

    @RestService
    TeamsApiClient teamsApiClient;

    @RootContext
    Context context;

    public boolean isAnnouncementOpened(int entityId) {
        FormattedEntity entity = EntityManager.getInstance(context).getEntityById(entityId);
        return entity.announcementOpened;
    }

    public ResAnnouncement getAnnouncement(int teamId, int topicId) {
        ResAnnouncement announcement = null;
        try {
            announcement = RequestManager.newInstance(context, () -> {
                teamsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                return teamsApiClient.getAnnouncement(teamId, topicId);
            }).request();
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
        return announcement;
    }

    @Background
    public void createAnnouncement(int teamId, int topicId, int messageId) {
        try {
            RequestManager.newInstance(context, () -> {
                teamsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                ReqCreateAnnouncement reqCreateAnnouncement = new ReqCreateAnnouncement(messageId);
                return teamsApiClient.createAnnouncement(teamId, topicId, reqCreateAnnouncement);
            }).request();
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void updateAnnouncementStatus(int teamId, int topicId, boolean isOpened) {
        int memberId = EntityManager.getInstance(context).getMe().getUser().id;

        try {
            RequestManager.newInstance(context, () -> {
                teamsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                ReqUpdateAnnouncementStatus reqUpdateAnnouncementStatus
                        = new ReqUpdateAnnouncementStatus(topicId, isOpened);
                return teamsApiClient.updateAnnouncementStatus(teamId, memberId, reqUpdateAnnouncementStatus);
            }).request();
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

    @Background
    public void deleteAnnouncement(int teamId, int topicId) {
        try {
            RequestManager.newInstance(context, () -> {
                teamsApiClient.setAuthentication(TokenUtil.getRequestAuthentication(context));
                return teamsApiClient.deleteAnnouncement(teamId, topicId);
            }).request();
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

}
