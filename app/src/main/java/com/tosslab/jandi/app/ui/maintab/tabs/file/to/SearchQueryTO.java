package com.tosslab.jandi.app.ui.maintab.tabs.file.to;

import com.tosslab.jandi.app.network.models.ReqSearchFile;

/**
 * Created by tee on 16. 6. 28..
 */

public class SearchQueryTO {
    private final String CATEGORY_ALL = "all";
    private final int LATEST_MESSAGE = -1;

    private String searchFileType;
    private String searchUser;
    private String keyword;
    private long entityId = -1;
    private long startMessageId;

    public SearchQueryTO() {
        entityId = ReqSearchFile.ALL_ENTITIES;
        startMessageId = LATEST_MESSAGE;
        keyword = "";
        searchFileType = CATEGORY_ALL;    // 서치 모드.   ALL || Images || PDFs
        searchUser = CATEGORY_ALL;        // 사용자.     ALL || Mine || UserID
    }

    public void setToFirst() {
        startMessageId = LATEST_MESSAGE;
    }

    public void setKeyword(String keyword) {
        setToFirst();
        this.keyword = keyword;
    }

    public void setFileType(String fileType) {
        setToFirst();
        searchFileType = fileType;
    }

    public void setWriter(String writerId) {
        setToFirst();
        searchUser = writerId;
    }

    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        setToFirst();
        this.entityId = entityId;
    }

    public String getSearchFileType() {
        return searchFileType;
    }

    public String getSearchUser() {
        return searchUser;
    }

    public void setNext(int startMessageId) {
        this.startMessageId = startMessageId;
    }

    public ReqSearchFile getRequestQuery() {
        ReqSearchFile reqSearchFile = new ReqSearchFile();
        reqSearchFile.searchType = ReqSearchFile.SEARCH_TYPE_FILE;
        reqSearchFile.listCount = ReqSearchFile.MAX;

        reqSearchFile.fileType = searchFileType;
        reqSearchFile.writerId = searchUser;
        reqSearchFile.sharedEntityId = entityId;

        reqSearchFile.startMessageId = startMessageId;
        reqSearchFile.keyword = keyword;
        return reqSearchFile;
    }
}