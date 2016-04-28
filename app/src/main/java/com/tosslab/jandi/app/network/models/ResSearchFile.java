package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
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
