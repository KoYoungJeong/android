package com.tosslab.jandi.app.network.models.search;

import android.text.TextUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by tee on 16. 7. 20..
 */

public class ReqSearch {

    // 작성자 ID
    private long writerId = -1;

    // ROOM ID - -100일 경우 룸아이디가 아직 생성안된 멤버
    private long roomId = -1;

    // 검색 타입 all, message, file, poll
    private String type = "all";

    // 컨텐츠의 접근 혹은 공유 범위 accessible, joined, notShared
    private String accessType = "joined";

    // 파일 타입
    private String fileType = "all";

    // 검색 키워드 - default 없음
    private String keyword = null;

    // 페이지 - default 1
    private int page = -1;

    // 결과 수 - default 20
    private int count = -1;

    // 검색 대상의 시작 시간
    private Date startAt = null;

    // 검색 대상의 끝 시간
    private Date endAt = null;

    // 정렬 순서
    private String order = "desc";

    public long getWriterId() {
        return writerId;
    }

    private void setWriterId(long writerId) {
        this.writerId = writerId;
    }

    public long getRoomId() {
        return roomId;
    }

    private void setRoomId(long roomId) {
        this.roomId = roomId;
    }

    public String getType() {
        return type;
    }

    private void setType(String type) {
        this.type = type;
    }

    public String getAccessType() {
        return accessType;
    }

    private void setAccessType(String accessType) {
        this.accessType = accessType;
    }

    public String getFileType() {
        return fileType;
    }

    private void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getKeyword() {
        return keyword;
    }

    private void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getPage() {
        return page;
    }

    private void setPage(int page) {
        this.page = page;
    }

    public int getCount() {
        return count;
    }

    private void setCount(int count) {
        this.count = count;
    }

    public Date getStartAt() {
        return startAt;
    }

    private void setStartAt(Date startAt) {
        this.startAt = startAt;
    }

    public Date getEndAt() {
        return endAt;
    }

    private void setEndAt(Date endAt) {
        this.endAt = endAt;
    }

    public String getOrder() {
        return order;
    }

    private void setOrder(String order) {
        this.order = order;
    }

    public Map<String, String> convertMap() {

        Map<String, String> map = new HashMap<>();

        if (writerId > 0) {
            map.put("writerId", String.valueOf(writerId));
        }

        if (roomId > 0) {
            map.put("roomId", String.valueOf(roomId));
        }

        if (type != null) {
            map.put("type", type);
        }

        if (!TextUtils.isEmpty(accessType)) {
            map.put("accessType", accessType);
        }

        if (!TextUtils.isEmpty(fileType)) {
            map.put("fileType", fileType);
        }

        if (!TextUtils.isEmpty(keyword)) {
            map.put("keyword", keyword);
        }

        if (page > 0) {
            map.put("page", String.valueOf(page));
        }

        if (count > 0) {
            map.put("count", String.valueOf(count));
        }

        if (startAt != null) {
            map.put("startAt", startAt.toString());
        }

        if (endAt != null) {
            map.put("endAt", endAt.toString());
        }

        if (!TextUtils.isEmpty(order)) {
            map.put("order", order);
        }

        return map;
    }

    public static class Builder {

        private ReqSearch reqSearch = new ReqSearch();

        public Builder setWriterId(long writerId) {
            reqSearch.setWriterId(writerId);
            return this;
        }

        public Builder setRoomId(long roomId) {
            reqSearch.setRoomId(roomId);
            return this;
        }

        public Builder setType(String type) {
            reqSearch.setType(type);
            return this;
        }

        public Builder setAccessType(String accessType) {
            reqSearch.setAccessType(accessType);
            return this;
        }

        public Builder setFileType(String fileType) {
            reqSearch.setFileType(fileType);
            return this;
        }

        public Builder setKeyword(String keyword) {
            reqSearch.setKeyword(keyword);
            return this;
        }

        public Builder setPage(int page) {
            reqSearch.setPage(page);
            return this;
        }

        public Builder setCount(int count) {
            reqSearch.setCount(count);
            return this;
        }

        public Builder setStartAt(Date startAt) {
            reqSearch.setStartAt(startAt);
            return this;
        }

        public Builder setEndAt(Date endAt) {
            reqSearch.setEndAt(endAt);
            return this;
        }

        public Builder setOrder(String order) {
            reqSearch.setOrder(order);
            return this;
        }

        public ReqSearch build() {
            return reqSearch;
        }

    }

}
