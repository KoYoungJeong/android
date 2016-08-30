package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain;


import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.utils.FirstCharacterUtil;

public class TeamMemberItem {

    private final ChatChooseItem chatChooseItem;
    private final String firstCharacter;
    private final String keyword;
    private final String name;
    private CharSequence nameOfSpan;


    public TeamMemberItem(User user, String keyword) {
        if (user != null) {

            chatChooseItem = ChatChooseItem.create(user);
            if (!chatChooseItem.isInactive()) {
                name = chatChooseItem.getName();
            } else {
                name = chatChooseItem.getEmail();
            }
            firstCharacter = FirstCharacterUtil.firstCharacter(name);
        } else {
            chatChooseItem = null;
            name = "";
            firstCharacter = "";
        }
        this.keyword = keyword;

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

    public String getKeyword() {
        return keyword;
    }

    public CharSequence getNameOfSpan() {
        return nameOfSpan;
    }

    public void setNameOfSpan(CharSequence nameOfSpan) {
        this.nameOfSpan = nameOfSpan;
    }
}
