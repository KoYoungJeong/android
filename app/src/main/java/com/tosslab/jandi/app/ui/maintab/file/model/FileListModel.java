package com.tosslab.jandi.app.ui.maintab.file.model;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.local.database.file.JandiFileDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.ui.message.v2.model.MessageListModel;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@EBean
public class FileListModel {

    @RootContext
    Context context;

    public ResSearchFile searchFileList(ReqSearchFile reqSearchFile) throws JandiNetworkException {
        RequestManager<ResSearchFile> requestManager = RequestManager.newInstance(context, FileSearchRequest.create(context, reqSearchFile));
        ResSearchFile resSearchFile = requestManager.request();

        return resSearchFile;
    }

    public boolean isAllTypeFirstSearch(ReqSearchFile reqSearchFile) {

        return reqSearchFile.startMessageId == -1 &&
                reqSearchFile.sharedEntityId == -1 &&
                TextUtils.equals(reqSearchFile.fileType, "all") &&
                TextUtils.equals(reqSearchFile.writerId, "all") &&
                TextUtils.isEmpty(reqSearchFile.keyword);

    }

    public void saveOriginFirstItems(int teamId, ResSearchFile fileMessages) {
        JandiFileDatabaseManager.getInstance(context).upsertFiles(teamId, fileMessages);
    }

    public ResSearchFile getFiles(int teamId) {
        return JandiFileDatabaseManager.getInstance(context).getFiles(teamId);
    }

    public EntityManager retrieveEntityManager() {
        EntityManager entityManager = EntityManager.getInstance(context);

        if (entityManager != null) {
            return entityManager;
        }

        return null;
    }

    public List<ResMessages.OriginalMessage> descSortByCreateTime(List<ResMessages.OriginalMessage> links) {
        List<ResMessages.OriginalMessage> ret = new ArrayList<ResMessages.OriginalMessage>(links);

        Comparator<ResMessages.OriginalMessage> sort = new Comparator<ResMessages.OriginalMessage>() {
            @Override
            public int compare(ResMessages.OriginalMessage link, ResMessages.OriginalMessage link2) {
                if (link.createTime.getTime() > link2.createTime.getTime())
                    return -1;
                else if (link.createTime.getTime() == link2.createTime.getTime())
                    return 0;
                else
                    return 1;
            }
        };
        Collections.sort(ret, sort);
        return ret;
    }

    public boolean isOverSize(String realFilePath) {
        File uploadFile = new File(realFilePath);
        return uploadFile.exists() && uploadFile.length() > MessageListModel.MAX_FILE_SIZE;
    }
}
