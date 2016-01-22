package com.tosslab.jandi.app.network.models;

import com.j256.ormlite.table.DatabaseTable;
import com.tosslab.jandi.app.local.orm.dao.FileDatailDaoImpl;
import com.tosslab.jandi.app.local.orm.dao.LinkDaoImpl;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 24..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResFileDetail {
    public int messageCount;
    public List<ResMessages.OriginalMessage> messageDetails;

    @Override
    public String toString() {
        return "ResFileDetail{" +
                "messageCount=" + messageCount +
                ", messageDetails=" + messageDetails +
                '}';
    }
}
