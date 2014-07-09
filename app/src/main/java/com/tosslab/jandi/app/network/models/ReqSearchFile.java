package com.tosslab.jandi.app.network.models;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
public class ReqSearchFile {
    public static final String SEARCH_TYPE_FILE = "file";
    public static final String USER_ID_MINE     = "mine";
    public static final String USER_ID_ALL      = "all";
    public static final String FILE_TYPE_ALL    = "all";
    public static final String FILE_TYPE_IMAGE  = "image";
    public static final String FILE_TYPE_PDF    = "pdf";

    public String searchType;
    public String userId;
    public String fileType;
}