package com.tosslab.jandi.app.ui.maintab.file.model;

/**
 * Created by Steve SeongUg Jung on 15. 1. 8..
 */

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.database.file.JandiFileDatabaseManager;
import com.tosslab.jandi.app.network.client.file.FileApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ReqSearchFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResSearchFile;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;


@EBean
public class FileListModel {

    @Inject
    Lazy<FileApi> fileApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public ResSearchFile searchFileList(ReqSearchFile reqSearchFile) throws RetrofitException {
        return fileApi.get().searchFile(reqSearchFile);
    }

    public boolean isAllTypeFirstSearch(ReqSearchFile reqSearchFile) {

        return reqSearchFile.startMessageId == -1 &&
                reqSearchFile.sharedEntityId == -1 &&
                TextUtils.equals(reqSearchFile.fileType, "all") &&
                TextUtils.equals(reqSearchFile.writerId, "all") &&
                TextUtils.isEmpty(reqSearchFile.keyword);

    }

    public void saveOriginFirstItems(long teamId, ResSearchFile fileMessages) {
        JandiFileDatabaseManager.getInstance(JandiApplication.getContext()).upsertFiles(teamId, fileMessages);
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

    public void trackFileKeywordSearchSuccess(String keyword) {
        try {
            keyword = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.FileKeywordSearch)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.SearchKeyword, keyword)
                .build());
    }

    public void trackFileKeywordSearchFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.FileKeywordSearch)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
    }
}
