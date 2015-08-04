package com.tosslab.jandi.app.ui.maintab.file.model;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.database.file.JandiFileDatabaseManager;
import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit.RetrofitError;

@EBean
public class FileListModel {

    @RootContext
    Context context;

    public ResSearchFile searchFileList(ReqSearchFile reqSearchFile) throws RetrofitError {
        ResSearchFile resSearchFile = RequestApiManager.getInstance().searchFileByMainRest(reqSearchFile);
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

    public boolean isDefaultSearchQuery(ReqSearchFile searchFile) {
        return searchFile.sharedEntityId == -1 &&
                searchFile.startMessageId == -1 &&
                TextUtils.isEmpty(searchFile.keyword) &&
                TextUtils.equals(searchFile.fileType, "all") &&
                TextUtils.equals(searchFile.writerId, "all");
    }

    public boolean isDefaultSearchQueryIgnoreMessageId(ReqSearchFile searchFile) {
        return searchFile.sharedEntityId == -1 &&
                TextUtils.isEmpty(searchFile.keyword) &&
                TextUtils.equals(searchFile.fileType, "all") &&
                TextUtils.equals(searchFile.writerId, "all");
    }

}
