package com.tosslab.jandi.app.team.member;

import android.text.TextUtils;

import com.tosslab.jandi.app.network.models.start.Bot;

public class WebhookBot implements Member {
    private final Bot bot;

    public WebhookBot(Bot bot) {

        this.bot = bot;
    }

    public Bot getRaw() {
        return bot;
    }

    @Override
    public long getId() {
        return bot.getId();
    }

    @Override
    public String getName() {
        return bot.getName();
    }

    @Override
    public String getPhotoUrl() {
        return bot.getPhotoUrl();
    }

    @Override
    public boolean isEnabled() {
        return TextUtils.equals(bot.getStatus(), "enabled");
    }

    @Override
    public String getEmail() {
        return "";
    }

    @Override
    public boolean isInactive() {
        return false;
    }

    @Override
    public boolean isTeamOwner() {
        return false;
    }

    @Override
    public String getType() {
        return bot.getType();
    }

    @Override
    public boolean isBot() {
        return true;
    }

    @Override
    public boolean isStarred() {
        return false;
    }

    public boolean isJandiBot() {
        return TextUtils.equals(bot.getType(), "jandi_bot");
    }

    public void updateStatus(String status) {
        
    }
}
