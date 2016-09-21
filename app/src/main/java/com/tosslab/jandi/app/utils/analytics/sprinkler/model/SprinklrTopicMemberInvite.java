package com.tosslab.jandi.app.utils.analytics.sprinkler.model;

import com.tosslab.jandi.app.utils.analytics.sprinkler.PropertyKey;
import com.tosslab.jandi.app.utils.analytics.sprinkler.SprinklerEvents;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.IMemberCount;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.Properties.ITopicId;

/**
 * Created by tee on 2016. 9. 9..
 */

public class SprinklrTopicMemberInvite extends MainSprinklrModel
        implements ITopicId, IMemberCount {

    private SprinklrTopicMemberInvite() {
        super(SprinklerEvents.TopicMemberInvite, true, true);
    }

    public static void sendFailLog(int errorCode) {
        new SprinklrTopicMemberInvite().sendFail(errorCode);
    }

    public static void sendLog(long topicId, int memberCount) {
        new SprinklrTopicMemberInvite()
                .setTopicId(topicId)
                .setMemberCount(memberCount)
                .sendSuccess();
    }

    @Override
    public SprinklrTopicMemberInvite setMemberCount(int count) {
        setProperty(PropertyKey.MemberCount, count);
        return this;
    }

    @Override
    public SprinklrTopicMemberInvite setTopicId(long topicId) {
        setProperty(PropertyKey.TopicId, topicId);
        return this;
    }

}
