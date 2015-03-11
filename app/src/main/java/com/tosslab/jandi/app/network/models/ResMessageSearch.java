package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;
import org.codehaus.jackson.map.annotate.JsonSerialize;

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
        private int linkId;
        private int memberId;
        private int messageId;
        private String text;
        private Date date;


        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getLinkId() {
            return linkId;
        }

        public void setLinkId(int linkId) {
            this.linkId = linkId;
        }

        public int getMemberId() {
            return memberId;
        }

        public void setMemberId(int memberId) {
            this.memberId = memberId;
        }

        public int getMessageId() {
            return messageId;
        }

        public void setMessageId(int messageId) {
            this.messageId = messageId;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    @JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
    public static class SearchEntityInfo {
        private String type;
        private int id;
        private String name;

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }
}
