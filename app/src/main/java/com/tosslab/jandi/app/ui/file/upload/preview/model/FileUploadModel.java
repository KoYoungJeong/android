package com.tosslab.jandi.app.ui.file.upload.preview.model;

import com.tosslab.jandi.app.network.models.start.TeamPlan;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;

import javax.inject.Inject;

public class FileUploadModel {

    public static final String FILE_SAPERATOR = "/";

    @Inject
    public FileUploadModel() { }

    public String getFileName(String path) {
        int separatorIndex = path.lastIndexOf(FILE_SAPERATOR);
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }

    public String getEntityString(long entityId) {

        String originEntityName = TeamInfoLoader.getInstance().getName(entityId);

        String prefix;

        if (TeamInfoLoader.getInstance().isUser(entityId)
                || TeamInfoLoader.getInstance().isJandiBot(entityId)) {
            prefix = "@";
        } else {
            prefix = "#";

        }

        return String.format("%s%s", prefix, originEntityName);
    }

    public boolean isValid(long entityId) {
        return (TeamInfoLoader.getInstance().isTopic(entityId)
                && TeamInfoLoader.getInstance().getTopic(entityId).isJoined())
                || TeamInfoLoader.getInstance().isUser(entityId);
    }

    public long getDefaultTopicEntity() {
        long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();
        if (TeamInfoLoader.getInstance().getTopic(defaultTopicId).isJoined()) {
            return defaultTopicId;
        } else {
            for (TopicRoom topicRoom : TeamInfoLoader.getInstance().getTopicList()) {
                if (topicRoom.isJoined()) {
                    return topicRoom.getId();
                }
            }
        }
        return -1;
    }

    public String getEntityName(long topicId) {
        return TeamInfoLoader.getInstance().getName(topicId);
    }

    public boolean isUser(long entityId) {
        return TeamInfoLoader.getInstance().isUser(entityId);
    }

    public boolean isUploadLimited() {
        TeamPlan teamPlan = TeamInfoLoader.getInstance().getTeamPlan();
        if (teamPlan != null) {

            boolean isLimited = teamPlan.isExceedFile();
            return isLimited;
        } else {
            return true;
        }

    }
}
