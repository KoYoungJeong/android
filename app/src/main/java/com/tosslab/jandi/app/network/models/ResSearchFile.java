package com.tosslab.jandi.app.network.models;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
public class ResSearchFile {
    public int fileCount;
    public List<ResMessages.OriginalMessage> files;
    public int firstIdOfReceivedList;

    @Override
    public String toString() {
        return "ResSearchFile{" +
                "fileCount=" + fileCount +
                ", files=" + files +
                ", firstIdOfReceivedList=" + firstIdOfReceivedList +
                '}';
    }
}
