package com.tosslab.jandi.app.ui.file.upload.preview.model;

import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.androidannotations.annotations.EBean;

@EBean
public class FileUploadModel {

    public static final String FILE_SAPERATOR = "/";

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
        return TeamInfoLoader.getInstance().isTopic(entityId)
                || TeamInfoLoader.getInstance().isUser(entityId);
    }

    public long getDefaultTopicEntity() {
        return TeamInfoLoader.getInstance().getDefaultTopicId();
    }

    public String getEntityName(long topicId) {
        return TeamInfoLoader.getInstance().getName(topicId);
    }

    public boolean isUser(long entityId) {
        return TeamInfoLoader.getInstance().isUser(entityId);
    }

    public boolean isUploadLimited() {
        boolean isLimited = TeamInfoLoader.getInstance().getTeamPlan().isExceedFile();
        return isLimited;

    }
}
