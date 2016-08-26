package com.tosslab.jandi.app.ui.maintab.tabs.file.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.client.messages.MessageApi;
import com.tosslab.jandi.app.network.client.teams.search.SearchApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.search.ReqSearch;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.lib.sprinkler.io.domain.track.FutureTrack;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.inject.Inject;

import dagger.Lazy;

public class FileListModel {

    Lazy<SearchApi> searchApi;
    Lazy<MessageApi> messageApi;

    @Inject
    public FileListModel(Lazy<SearchApi> searchApi, Lazy<MessageApi> messageApi) {
        this.searchApi = searchApi;
        this.messageApi = messageApi;
    }

    public boolean isDefaultSearchQuery(long page, long roomId, long writerId, String keyword, String fileType) {
        return page == 1 &&
                roomId == -1 &&
                writerId == -1 &&
                TextUtils.isEmpty(keyword) &&
                TextUtils.equals(fileType, "all");
    }

    public void trackFileKeywordSearchSuccess(String keyword) {
        try {
            keyword = URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.FileKeywordSearch)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, true)
                .property(PropertyKey.SearchKeyword, keyword)
                .build());
    }

    public void trackFileKeywordSearchFail(int errorCode) {
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(SprinklerEvents.FileKeywordSearch)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .memberId(AccountUtil.getMemberId(JandiApplication.getContext()))
                .property(PropertyKey.ResponseSuccess, false)
                .property(PropertyKey.ErrorCode, errorCode)
                .build());
    }

    public long getSelectedTeamId() {
        return TeamInfoLoader.getInstance().getTeamId();
    }

    public ResSearch getResults(ReqSearch it) throws RetrofitException {
        return searchApi.get().getSearch(getSelectedTeamId(), it);
    }

    public ResMessages.OriginalMessage getImageFile(long fileId) throws RetrofitException {
        return messageApi.get().getMessage(TeamInfoLoader.getInstance().getTeamId(), fileId);
    }
}
