package com.tosslab.jandi.app.network.models.commonobject;

/**
 * Created by tee on 15. 7. 30..
 */
public class CursorObject {

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

    @Override
    public String toString() {
        return "CursorObject{" +
                "page=" + page +
                ", perPage=" + perPage +
                ", pageCount=" + pageCount +
                ", totalCount=" + totalCount +
                ", recordCount=" + recordCount +
                '}';
    }
}
