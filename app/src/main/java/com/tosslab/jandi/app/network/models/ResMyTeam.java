package com.tosslab.jandi.app.network.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 8. 1..
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
public class ResMyTeam {
    public List<Team> teamList;

    @Override
    public String toString() {
        return "ResMyTeam{" +
                "teamList=" + teamList +
                '}';
    }

    public static class Team {
        public long teamId;
        public String type;
        public String name;
        public String status;
        public String t_domain;

        @Override
        public String toString() {
            return "Team{" +
                    "teamId=" + teamId +
                    ", type='" + type + '\'' +
                    ", name='" + name + '\'' +
                    ", status='" + status + '\'' +
                    ", t_domain='" + t_domain + '\'' +
                    '}';
        }
    }
}
