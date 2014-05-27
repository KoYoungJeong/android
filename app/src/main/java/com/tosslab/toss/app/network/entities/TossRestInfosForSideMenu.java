package com.tosslab.toss.app.network.entities;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */
public class TossRestInfosForSideMenu {
    public int channelCount;
    public List<Channel> channels;
    public int memberCount;
    public List<Member> members;
    public int privateGroupCount;
    public List<PrivateGroup> privateGroups;


    public static class Channel {
        public int id;
        public String name;
    }

    public static class Member {
        public String id;
        public String nickname;
    }

    public static class PrivateGroup {
        public int id;
        public String name;
    }
}
