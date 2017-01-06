package com.tosslab.jandi.app.ui.maintab.tabs.util;

import com.tosslab.jandi.app.ui.maintab.tabs.chat.ChatTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.file.FileTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.mypage.MypageTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.MainTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.team.TeamTabInfo;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.TopicTabInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 2016. 8. 18..
 */
public class MainTabFactory {

    public static List<MainTabInfo> getTabs(long selectedEntity) {
        List<MainTabInfo> tabInfos = new ArrayList<>();
        tabInfos.add(new TopicTabInfo(selectedEntity));
        tabInfos.add(new ChatTabInfo(selectedEntity));
        tabInfos.add(new FileTabInfo());
        tabInfos.add(new TeamTabInfo());
        tabInfos.add(new MypageTabInfo());
        return tabInfos;
    }

}
