package com.tosslab.jandi.app.lists;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import java.util.ArrayList;
import java.util.Collection;

public class BotEntity extends FormattedEntity {
    private final ResLeftSideMenu.Bot bot;

    public BotEntity(ResLeftSideMenu.Bot bot) {
        this.bot = bot;
    }

    public ResLeftSideMenu.Bot getBot() {
        return bot;
    }

    public String getBotType() {
        return bot.botType;
    }

    @Override
    public boolean isEnabled() {
        return TextUtils.equals(bot.status, "enabled");
    }

    @Override
    public boolean isPublicTopic() {
        return false;
    }

    @Override
    public boolean isPrivateGroup() {
        return false;
    }

    @Override
    public boolean isUser() {
        return false;
    }

    @Override
    public boolean isDummy() {
        return false;
    }

    @Override
    public ResLeftSideMenu.Entity getEntity() {
        return null;
    }

    @Override
    public long getId() {
        return bot.id;
    }

    @Override
    public String getName() {
        return bot.name;
    }

    @Override
    public int getMemberCount() {
        return 0;
    }

    @Override
    public int getIconImageResId() {
        return 0;
    }

    @Override
    public ResLeftSideMenu.Channel getChannel() {
        return null;
    }

    @Override
    public ResLeftSideMenu.PrivateGroup getPrivateGroup() {
        return null;
    }

    @Override
    public ResLeftSideMenu.User getUser() {
        return null;
    }

    @Override
    public Collection<Long> getMembers() {
        return new ArrayList<>();
    }

    @Override
    public String getUserStatusMessage() {
        return "";
    }

    @Override
    public String getUserEmail() {
        return "";
    }

    @Override
    public String getUserSmallProfileUrl() {
        return bot.thumbnailUrl;
    }

    @Override
    public String getUserMediumProfileUrl() {
        return bot.thumbnailUrl;
    }

    @Override
    public String getUserLargeProfileUrl() {
        return bot.thumbnailUrl;
    }

    @Override
    public String getUserPhoneNumber() {
        return "";
    }

    @Override
    public String getUserDivision() {
        return "";
    }

    @Override
    public String getUserPosition() {
        return "";
    }

    @Override
    public int getDummyNameRes() {
        return 0;
    }

    @Override
    public boolean hasGivenId(long entityId) {
        return false;
    }

    @Override
    public boolean hasGivenIds(Collection<Long> entityIds) {
        return false;
    }

    @Override
    public boolean isMine(long myId) {
        return false;
    }

    @Override
    public String toString() {
        return "BotEntity{" +
                "bot=" + bot +
                '}';
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public boolean isAutoJoin() {
        return false;
    }
}
