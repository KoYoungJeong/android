package com.tosslab.jandi.app.network.client.publictopic;

import com.tosslab.jandi.app.network.models.ReqCreateTopic;
import com.tosslab.jandi.app.network.models.ReqDeleteTopic;
import com.tosslab.jandi.app.network.models.ReqInviteTopicUsers;
import com.tosslab.jandi.app.network.models.ReqModifyTopicAutoJoin;
import com.tosslab.jandi.app.network.models.ReqModifyTopicDescription;
import com.tosslab.jandi.app.network.models.ReqModifyTopicName;
import com.tosslab.jandi.app.network.models.ResCommon;

import retrofit.RetrofitError;

/**
 * Created by tee on 15. 6. 23..
 */
public interface IChannelApiAuth {

    ResCommon createChannelByChannelApi(ReqCreateTopic channel) throws RetrofitError;

    ResCommon modifyPublicTopicNameByChannelApi(ReqModifyTopicName channel, int channelId) throws RetrofitError;
    ResCommon modifyPublicTopicDescriptionByChannelApi(ReqModifyTopicDescription description, int channelId) throws RetrofitError;
    ResCommon modifyPublicTopicAutoJoinByChannelApi(ReqModifyTopicAutoJoin topicAutoJoin, int channelId);

    ResCommon deleteTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError;

    ResCommon joinTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError;

    ResCommon leaveTopicByChannelApi(int channelId, ReqDeleteTopic reqDeleteTopic) throws RetrofitError;

    ResCommon invitePublicTopicByChannelApi(int channelId, ReqInviteTopicUsers reqInviteTopicUsers) throws RetrofitError;

}
