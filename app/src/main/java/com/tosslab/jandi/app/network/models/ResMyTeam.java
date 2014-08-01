package com.tosslab.jandi.app.network.models;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 1..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include=JsonSerialize.Inclusion.NON_NULL)
public class ResMyTeam {
    public List<Team> teamList;

    public static class Team {
        public int teamId;
        public String type;
        public String name;
        public String status;
        public String t_domain;
    }
}
