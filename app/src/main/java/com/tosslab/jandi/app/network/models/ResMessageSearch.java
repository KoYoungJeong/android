package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Date;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 15. 3. 11..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResMessageSearch {

    @JsonProperty("cursor")
    private QueryCursor queryCursor;
    @JsonProperty("records")
    private List<SearchRecord> searchRecords;

    public QueryCursor getQueryCursor() {
        return queryCursor;
    }

    public void setQueryCursor(QueryCursor queryCursor) {
        this.queryCursor = queryCursor;
    }

    public List<SearchRecord> getSearchRecords() {
        return searchRecords;
    }

    public void setSearchRecords(List<SearchRecord> searchRecords) {
        this.searchRecords = searchRecords;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class QueryCursor {
        private int page;
        private int perPage;
        private int pageCount;
        private int totalCount;
        private int recordCount;

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public int getPerPage() {
            return perPage;
        }

        public void setPerPage(int perPage) {
            this.perPage = perPage;
        }

        public int getPageCount() {
            return pageCount;
        }

        public void setPageCount(int pageCount) {
            this.pageCount = pageCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public void setTotalCount(int totalCount) {
            this.totalCount = totalCount;
        }

        public int getRecordCount() {
            return recordCount;
        }

        public void setRecordCount(int recordCount) {
            this.recordCount = recordCount;
        }
    }

    // Todo 삭제 예정
    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class SearchRecord {
        @JsonProperty("entity")
        private SearchEntityInfo searchEntityInfo;

        @JsonProperty("current")
        private Record currentRecord;
        @JsonProperty("prev")
        private Record prevRecord;
        @JsonProperty("next")
        private Record nextRecord;

        public SearchEntityInfo getSearchEntityInfo() {
            return searchEntityInfo;
        }

        public void setSearchEntityInfo(SearchEntityInfo searchEntityInfo) {
            this.searchEntityInfo = searchEntityInfo;
        }

        public Record getCurrentRecord() {
            return currentRecord;
        }

        public void setCurrentRecord(Record currentRecord) {
            this.currentRecord = currentRecord;
        }

        public Record getPrevRecord() {
            return prevRecord;
        }

        public void setPrevRecord(Record prevRecord) {
            this.prevRecord = prevRecord;
        }

        public Record getNextRecord() {
            return nextRecord;
        }

        public void setNextRecord(Record nextRecord) {
            this.nextRecord = nextRecord;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class Record {
        private String type;
        private long linkId;
        private long memberId;
        private long messageId;
        private String text;
        @JsonProperty("time")
        private Date lastDate;
        private String status;
        @JsonProperty("file")
        private FileInfo fileInfo;
        @JsonProperty("poll")
        private PollInfo pollInfo;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getLinkId() {
            return linkId;
        }

        public void setLinkId(long linkId) {
            this.linkId = linkId;
        }

        public long getMemberId() {
            return memberId;
        }

        public void setMemberId(long memberId) {
            this.memberId = memberId;
        }

        public long getMessageId() {
            return messageId;
        }

        public void setMessageId(long messageId) {
            this.messageId = messageId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Date getLastDate() {
            return lastDate;
        }

        public void setLastDate(Date lastDate) {
            this.lastDate = lastDate;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public FileInfo getFileInfo() {
            return fileInfo;
        }

        public void setFileInfo(FileInfo fileInfo) {
            this.fileInfo = fileInfo;
        }

        public PollInfo getPollInfo() {
            return pollInfo;
        }

        public void setPollInfo(PollInfo pollInfo) {
            this.pollInfo = pollInfo;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class SearchEntityInfo {
        private String type;
        private long id;
        private String name;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }


    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class FileInfo {
        private long writerId;
        private String title;
        private String name;

        public long getWriterId() {
            return writerId;
        }

        public void setWriterId(long writerId) {
            this.writerId = writerId;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class PollInfo {
        private long creatorId;
        private long pollId;
        private String subject;

        public long getCreatorId() {
            return creatorId;
        }

        public void setCreatorId(long creatorId) {
            this.creatorId = creatorId;
        }

        public long getPollId() {
            return pollId;
        }

        public void setPollId(long pollId) {
            this.pollId = pollId;
        }

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }
    }
}
