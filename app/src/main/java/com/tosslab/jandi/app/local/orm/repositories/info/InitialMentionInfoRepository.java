package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.v4.util.LongSparseArray;

import com.tosslab.jandi.app.local.orm.repositories.template.LockTemplate;
import com.tosslab.jandi.app.network.models.start.Mention;
import com.tosslab.jandi.app.team.TeamInfoLoader;

public class InitialMentionInfoRepository extends LockTemplate {

    private static LongSparseArray<InitialMentionInfoRepository> instance;

    private Mention mention;

    private InitialMentionInfoRepository() {
        super();
    }

    synchronized public static InitialMentionInfoRepository getInstance(long teamId) {
        if (instance == null) {
            instance = new LongSparseArray<>();
        }

        if (instance.indexOfKey(teamId) >= 0) {
            return instance.get(teamId);
        } else {
            InitialMentionInfoRepository value = new InitialMentionInfoRepository();
            instance.put(teamId, value);
            return value;

        }
    }

    public static InitialMentionInfoRepository getInstance() {
        return getInstance(TeamInfoLoader.getInstance().getTeamId());
    }

    public Mention getMention() {
        return execute(() -> {

            if (mention == null) {
                mention = new Mention();
                mention.setLastMentionedMessageId(-1);
                mention.setUnreadCount(0);
            }
            return mention;

        });
    }

    public boolean upsertMention(Mention mention) {

        return execute(() -> {
            this.mention = mention;
            return true;
        });
    }

    public boolean clearUnreadCount() {
        return execute(() -> {
            getMention().setUnreadCount(0);
            getMention().setLastMentionedMessageId(-1);

            return true;
        });
    }

    public boolean increaseUnreadCount() {
        return execute(() -> {


            Mention mention = getMention();
            mention.setUnreadCount(mention.getUnreadCount() + 1);

            return true;
        });
    }

    public boolean decreaseUnreadCount() {
        return execute(() -> {

            Mention mention = getMention();
            mention.setUnreadCount(mention.getUnreadCount() - 1);


            return true;
        });
    }
}