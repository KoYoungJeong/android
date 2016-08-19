package com.tosslab.jandi.app.ui.maintab.team.filter.member.domain;


import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.utils.FirstCharacterUtil;

public class TeamMemberItem {

    private final ChatChooseItem chatChooseItem;
    private final String name;
    private final String firstCharacter;

    public TeamMemberItem(User user) {
        chatChooseItem = ChatChooseItem.create(user);
        if (!chatChooseItem.isInactive()) {
            name = chatChooseItem.getName();
        } else {
            name = chatChooseItem.getEmail();
        }

        firstCharacter = FirstCharacterUtil.firstCharacter(name);
    }

    public ChatChooseItem getChatChooseItem() {
        return chatChooseItem;
    }

    public String getName() {
        return name;
    }

    public String getFirstCharacter() {
        return firstCharacter;
    }
}
