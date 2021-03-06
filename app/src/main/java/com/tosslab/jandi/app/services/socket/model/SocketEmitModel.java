package com.tosslab.jandi.app.services.socket.model;


import android.support.annotation.Nullable;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.socket.domain.SocketUpdateRoom;
import com.tosslab.jandi.app.services.socket.to.SocketTopicCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicInvitedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicJoinedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketTopicLeftEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.TokenUtil;

public class SocketEmitModel {

//    @Nullable
//    public static SocketUpdateMember teamCreated(Object data) {
//        try {
//            SocketTeamCreatedEvent object = SocketModelExtractor.getObject(data, SocketTeamCreatedEvent.class, true, false);
//            long memberId = object.getData().getMemberId();
//            long teamId = object.getData().getTeam().getTeamId();
//            String accessToken = TokenUtil.getAccessToken();
//            return SocketUpdateMember.join(accessToken, memberId, teamId);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    @Nullable
//    public static SocketUpdateMember teamJoin(Object data) {
//        try {
//            SocketTeamJoinEvent object = SocketModelExtractor.getObject(data, SocketTeamJoinEvent.class, true, false);
//            String accountId = object.getData().getMembers().getAccountId();
//            if (TextUtils.equals(AccountRepository.getRepository().getAccountInfo().getId(), accountId)) {
//                long memberId = object.getData().getMembers().getId();
//                long teamId = object.getData().getTeamId();
//                String accessToken = TokenUtil.getAccessToken();
//                return SocketUpdateMember.join(accessToken, memberId, teamId);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

//    @Nullable
//    public static SocketUpdateMember teamLeft(Object data) {
//        try {
//            SocketTeamLeaveEvent object = SocketModelExtractor.getObject(data, SocketTeamLeaveEvent.class, true, false);
//            long memberId = object.getData().getMemberId();
//            long teamId = object.getTeamId();
//            if (AccountRepository.getRepository().isMine(memberId)) {
//                String accessToken = TokenUtil.getAccessToken();
//                return SocketUpdateMember.leave(accessToken, memberId, teamId);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//
//    }

//    @Nullable
//    public static SocketUpdateMember teamDeleted(Object data) {
//        try {
//            SocketTeamDeletedEvent object = SocketModelExtractor.getObject(data, SocketTeamDeletedEvent.class, true, false);
//            long teamId = object.getData().getTeamId();
//            ResAccountInfo.UserTeam teamInfo = AccountRepository.getRepository().getTeamInfo(teamId);
//            if (teamInfo != null && teamInfo.getMemberId() > 0) {
//                long memberId = teamInfo.getMemberId();
//                String accessToken = TokenUtil.getAccessToken();
//                return SocketUpdateMember.leave(accessToken, memberId, teamId);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }

    @Nullable
    public static SocketUpdateRoom topicLeft(Object data) {

        try {
            SocketTopicLeftEvent object = SocketModelExtractor.getObject(data, SocketTopicLeftEvent.class, true, false);
            long memberId = object.getData().getMemberId();
            if (AccountRepository.getRepository().isMine(memberId)) {
                long topicId = object.getData().getTopicId();
                String accessToken = TokenUtil.getAccessToken();
                return SocketUpdateRoom.leave(accessToken, memberId, topicId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static SocketUpdateRoom topicDelete(Object data) {
        try {
            SocketTopicDeletedEvent object = SocketModelExtractor.getObject(data, SocketTopicDeletedEvent.class, true, false);
            long teamId = object.getTeamId();
            ResAccountInfo.UserTeam teamInfo = AccountRepository.getRepository().getTeamInfo(teamId);
            long topicId = object.getData().getTopicId();

            Topic topic = TopicRepository.getInstance(object.getTeamId()).getTopic(topicId);
            if (teamInfo != null
                    && teamInfo.getMemberId() > 0
                    && topic != null
                    && topic.getMembers().contains(teamInfo.getMemberId())) {
                String accessToken = TokenUtil.getAccessToken();
                return SocketUpdateRoom.leave(accessToken, teamInfo.getMemberId(), topicId);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @Nullable
    public static SocketUpdateRoom topicCreate(Object data) {
        try {
            SocketTopicCreatedEvent object = SocketModelExtractor.getObject(data, SocketTopicCreatedEvent.class, true, false);
            Topic topic = object.getData().getTopic();
            long creatorId = topic.getCreatorId();
            if (AccountRepository.getRepository().isMine(creatorId)) {
                long topicId = topic.getId();
                String accessToken = TokenUtil.getAccessToken();
                return SocketUpdateRoom.join(accessToken, creatorId, topicId);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;


    }

    @Nullable
    public static SocketUpdateRoom topicJoin(Object data) {
        try {
            SocketTopicJoinedEvent object = SocketModelExtractor.getObject(data, SocketTopicJoinedEvent.class, true, false);
            for (long memberId : object.getData().getMemberIds()) {
                if (AccountRepository.getRepository().isMine(memberId)) {
                    long topicId = object.getData().getTopicId();
                    String accessToken = TokenUtil.getAccessToken();
                    return SocketUpdateRoom.join(accessToken, memberId, topicId);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static SocketUpdateRoom topicInvited(Object data) {
        try {
            SocketTopicInvitedEvent object = SocketModelExtractor.getObject(data, SocketTopicInvitedEvent.class, true, false);
            long topicId = object.getData().getTopic().getId();
            String accessToken = TokenUtil.getAccessToken();
            return SocketUpdateRoom.join(accessToken, TeamInfoLoader.getInstance().getMyId(), topicId);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
