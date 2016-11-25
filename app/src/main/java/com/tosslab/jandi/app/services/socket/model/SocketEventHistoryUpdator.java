package com.tosslab.jandi.app.services.socket.model;


import com.tosslab.jandi.app.events.RefreshMypageBadgeCountEvent;
import com.tosslab.jandi.app.events.StartApiCalledEvent;
import com.tosslab.jandi.app.events.entities.ChatListRefreshEvent;
import com.tosslab.jandi.app.events.entities.RetrieveTopicListEvent;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.UnshareFileEvent;
import com.tosslab.jandi.app.events.messages.LinkPreviewUpdateEvent;
import com.tosslab.jandi.app.events.messages.MessageStarEvent;
import com.tosslab.jandi.app.events.messages.SocketPollEvent;
import com.tosslab.jandi.app.events.poll.RequestRefreshPollBadgeCountEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.SendMessageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.socket.SocketEventRepository;
import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.services.socket.JandiSocketServiceModel;
import com.tosslab.jandi.app.services.socket.to.SocketFileDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketFileUnsharedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewMessageEvent;
import com.tosslab.jandi.app.services.socket.to.SocketLinkPreviewThumbnailEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageStarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketMessageUnstarredEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollCreatedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollDeletedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollFinishedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketPollVotedEvent;
import com.tosslab.jandi.app.services.socket.to.SocketRoomMarkerEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Observable;

public class SocketEventHistoryUpdator {

    private static final String TAG = "SocketEventHistoryUpdat";
    private static final int TIME_OF_PRE_EVENT = 1000 * 60 * 10;
    private final Lazy<EventsApi> eventsApi;
    private final Lazy<StartApi> startApi;
    private Map<Class<? extends EventHistoryInfo>, JandiSocketServiceModel.Command> messageEventActorMapper;

    @Inject
    public SocketEventHistoryUpdator(Lazy<EventsApi> eventsApi,
                                     Lazy<StartApi> startApi) {
        this.eventsApi = eventsApi;
        this.startApi = startApi;
        messageEventActorMapper = new HashMap<>();
    }

    public void putAllEventActor(Map<Class<? extends EventHistoryInfo>, JandiSocketServiceModel.Command> messageEventActorMapper) {
        this.messageEventActorMapper.putAll(messageEventActorMapper);
    }

    public void updateEventHistory(EventPost eventPost, AccountRefreshPost accountPost, JandiRestarter restarterPost) {

        long socketConnectedLastTime = JandiPreference.getSocketConnectedLastTime();

        if (socketConnectedLastTime <= 0) {
            return;
        }

        socketConnectedLastTime -= TIME_OF_PRE_EVENT;


        checkEventHistory(socketConnectedLastTime, restarterPost)
                .filter(it -> messageEventActorMapper.containsKey(it.getClass()))
                .filter(it -> SocketEventRepository.getInstance().hasEvent(it))
                .filter(it -> SocketEventRepository.getInstance().addEvent(it))
                .toSortedList((lhs, rhs) -> ((Long) (lhs.getTs() - rhs.getTs())).intValue())
                .filter(it -> !it.isEmpty())
                .doOnNext(its -> {
                    EventHistoryInfo eventHistoryInfo = its.get(its.size() - 1);
                    JandiPreference.setSocketConnectedLastTime(eventHistoryInfo.getTs());
                })
                .subscribe(eventInfos -> {

                    if (!eventInfos.isEmpty()) {
                        if (accountPost != null) {
                            accountPost.refresh(new SocketRoomMarkerEvent());
                        }
                    }

                    List<EventHistoryInfo> messageCreates = new ArrayList<>();
                    List<EventHistoryInfo> etcEvents = new ArrayList<>();

                    EventHistoryInfo eventInfo;
                    for (int idx = 0, eventInfosSize = eventInfos.size(); idx < eventInfosSize; idx++) {
                        eventInfo = eventInfos.get(idx);
                        if (eventInfo instanceof SocketMessageCreatedEvent) {
                            messageCreates.add(eventInfo);
                        } else {
                            etcEvents.add(eventInfo);
                        }

                    }

                    deleteCompledtedMessages(messageCreates, eventPost);

                    if (!etcEvents.isEmpty()) {
                        EventHistoryInfo eventHistoryInfo;
                        for (int idx = 0, etcSize = etcEvents.size(); idx < etcSize; idx++) {
                            eventHistoryInfo = etcEvents.get(idx);
                            proccessMessageEventIfTooMuch(eventHistoryInfo, eventPost);
                        }
                    }

                }, Throwable::printStackTrace);

    }

    Observable<EventHistoryInfo> checkEventHistory(long socketConnectedLastTime, JandiRestarter restarterPost) {
        return Observable.defer(() -> {
            if (System.currentTimeMillis() - socketConnectedLastTime > 1000 * 60 * 60 * 24 * 7) {
                restartJandi(restarterPost);
                return Observable.empty();
            }

            long userId = TeamInfoLoader.getInstance().getMyId();
            ResEventHistory eventHistory;
            try {
                long teamId = TeamInfoLoader.getInstance().getTeamId();
                InitialInfo initializeInfo = startApi.get().getInitializeInfo(teamId);
                InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
                TeamInfoLoader.getInstance().refresh();
                JandiPreference.setSocketConnectedLastTime(initializeInfo.getTs());
                EventBus.getDefault().post(new RetrieveTopicListEvent());
                EventBus.getDefault().post(new ChatListRefreshEvent());
                EventBus.getDefault().post(new RefreshMypageBadgeCountEvent());
                EventBus.getDefault().post(new RequestRefreshPollBadgeCountEvent(teamId));
                EventBus.getDefault().post(new StartApiCalledEvent());

                eventHistory = eventsApi.get().getEventHistory(socketConnectedLastTime, userId, 1);

                int total = eventHistory.getTotal();

                if (total > 10000) {
                    restartJandi(restarterPost);
                    return Observable.empty();
                }

                eventHistory = eventsApi.get().getEventHistory(socketConnectedLastTime, userId);
                return Observable.just(eventHistory);

            } catch (RetrofitException e) {
                e.printStackTrace();
                restartJandi(restarterPost);
                return Observable.empty();
            }
        })
                .doOnNext(it -> LogUtil.d(TAG, "Sorted start : " + new Date().toString()))
                .concatMap(resEventHistory -> Observable.from(resEventHistory.getRecords()))
                .filter(SocketEventVersionModel::validVersion);
    }

    private void deleteCompledtedMessages(List<EventHistoryInfo> messageCreateEvents, EventPost eventPost) {

        if (messageCreateEvents != null) {
            // Message 넣기
            List<Long> linkMessageIds = new ArrayList<>();
            ResMessages.Link linkMessage;
            for (EventHistoryInfo eventHistoryInfo : messageCreateEvents) {
                linkMessage = ((SocketMessageCreatedEvent) eventHistoryInfo).getData().getLinkMessage();

                if (linkMessage.fromEntity == TeamInfoLoader.getInstance().getMyId()) {
                    linkMessageIds.add(linkMessage.id);
                }
            }

            SendMessageRepository.getRepository().deleteCompletedMessages(linkMessageIds);

            for (EventHistoryInfo eventHistoryInfo : messageCreateEvents) {
                postEvent(eventHistoryInfo, eventPost);
            }

        }
    }

    private void proccessMessageEventIfTooMuch(EventHistoryInfo eventHistoryInfo, EventPost eventPost) {
        if (eventHistoryInfo instanceof SocketFileDeletedEvent) {
            SocketFileDeletedEvent event = (SocketFileDeletedEvent) eventHistoryInfo;
            MessageRepository.getRepository().updateStatus(event.getFile().getId(), "archived");
            postEvent(new DeleteFileEvent(event.getTeamId(), event.getFile().getId()), eventPost);
        } else if (eventHistoryInfo instanceof SocketFileUnsharedEvent) {
            SocketFileUnsharedEvent event = (SocketFileUnsharedEvent) eventHistoryInfo;
            long fileId = event.getFile().getId();
            long roomId = event.room.id;

            MessageRepository.getRepository().deleteSharedRoom(fileId, roomId);
            postEvent(new UnshareFileEvent(roomId, fileId), eventPost);
        } else if (eventHistoryInfo instanceof SocketLinkPreviewMessageEvent) {
            SocketLinkPreviewMessageEvent event = (SocketLinkPreviewMessageEvent) eventHistoryInfo;
            SocketLinkPreviewMessageEvent.Data data = event.getData();
            ResMessages.TextMessage textMessage = MessageRepository.getRepository().getTextMessage(data.getMessageId());
            if (textMessage != null) {
                textMessage.linkPreview = data.getLinkPreview();
                MessageRepository.getRepository().upsertTextMessage(textMessage);
                postEvent(new LinkPreviewUpdateEvent(data.getMessageId()), eventPost);
            }


        } else if (eventHistoryInfo instanceof SocketLinkPreviewThumbnailEvent) {
            SocketLinkPreviewThumbnailEvent event = (SocketLinkPreviewThumbnailEvent) eventHistoryInfo;
            SocketLinkPreviewThumbnailEvent.Data data = event.getData();
            ResMessages.LinkPreview linkPreview = data.getLinkPreview();

            long messageId = data.getMessageId();

            ResMessages.TextMessage textMessage =
                    MessageRepository.getRepository().getTextMessage(messageId);
            textMessage.linkPreview = linkPreview;
            MessageRepository.getRepository().upsertTextMessage(textMessage);
            postEvent(new LinkPreviewUpdateEvent(data.getMessageId()), eventPost);
        } else if (eventHistoryInfo instanceof SocketMessageStarredEvent) {
            SocketMessageStarredEvent event = (SocketMessageStarredEvent) eventHistoryInfo;
            MessageRepository.getRepository().updateStarred(event.getStarredInfo()
                    .getMessageId(), true);
            postEvent(new MessageStarEvent(event.getStarredInfo().getMessageId(), true), eventPost);

        } else if (eventHistoryInfo instanceof SocketMessageUnstarredEvent) {
            SocketMessageUnstarredEvent event = (SocketMessageUnstarredEvent) eventHistoryInfo;
            MessageRepository.getRepository().updateStarred(event.getStarredInfo()
                    .getMessageId(), false);
            postEvent(new MessageStarEvent(event.getStarredInfo().getMessageId(), false), eventPost);

        } else if (eventHistoryInfo instanceof SocketMessageDeletedEvent) {
            SocketMessageDeletedEvent event = (SocketMessageDeletedEvent) eventHistoryInfo;
            long messageId = event.getData().getMessageId();
            MessageRepository.getRepository().deleteMessageOfMessageId(messageId);
            postEvent(event, eventPost);

        } else if (eventHistoryInfo instanceof SocketPollCreatedEvent) {
            SocketPollCreatedEvent event = (SocketPollCreatedEvent) eventHistoryInfo;
            SocketPollCreatedEvent.Data data = event.getData();

            Poll poll = data != null ? data.getPoll() : null;

            boolean isSameTeam =
                    event.getTeamId() == AccountRepository.getRepository().getSelectedTeamId();
            if (isSameTeam
                    && poll != null && poll.getId() > 0) {
                upsertPoll(poll);
                poll = getPollFromDatabase(poll.getId());

                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.CREATED), eventPost);
            }
        } else if (eventHistoryInfo instanceof SocketPollFinishedEvent) {
            SocketPollFinishedEvent event = (SocketPollFinishedEvent) eventHistoryInfo;
            SocketPollFinishedEvent.Data data = event.getData();

            ResMessages.Link link = data.getLinkMessage();
            Poll poll = link != null ? link.poll : null;
            boolean isSameTeam =
                    event.getTeamId() == AccountRepository.getRepository().getSelectedTeamId();
            if (isSameTeam
                    && poll != null && poll.getId() > 0) {
                upsertPoll(poll);
                poll = getPollFromDatabase(poll.getId());
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.FINISHED), eventPost);
            }

        } else if (eventHistoryInfo instanceof SocketPollDeletedEvent) {
            SocketPollDeletedEvent event = (SocketPollDeletedEvent) eventHistoryInfo;
            SocketPollDeletedEvent.Data data = event.getData();

            ResMessages.Link link = data.getLinkMessage();
            Poll poll = link != null ? link.poll : null;
            boolean isSameTeam =
                    event.getTeamId() == AccountRepository.getRepository().getSelectedTeamId();
            if (isSameTeam
                    && poll != null && poll.getId() > 0) {
                upsertPoll(poll);
                poll = getPollFromDatabase(poll.getId());
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.DELETED), eventPost);
            }
        } else if (eventHistoryInfo instanceof SocketPollVotedEvent) {
            SocketPollVotedEvent event = (SocketPollVotedEvent) eventHistoryInfo;
            SocketPollVotedEvent.Data data = event.getData();

            ResMessages.Link link = data.getLinkMessage();
            Poll poll = link != null ? link.poll : null;
            boolean isSameTeam =
                    event.getTeamId() == AccountRepository.getRepository().getSelectedTeamId();
            if (isSameTeam
                    && poll != null && poll.getId() > 0 && poll.isMine()) {
                upsertPollVotedStatus(poll);
                poll = getPollFromDatabase(poll.getId());
                postEvent(new SocketPollEvent(poll, SocketPollEvent.Type.VOTED), eventPost);
            }
        }
    }

    void postEvent(Object o, EventPost eventPost) {
        if (eventPost != null) {
            eventPost.post(o);
        }
    }

    void restartJandi(JandiRestarter restarterPost) {
        if (restarterPost != null) {
            restarterPost.restart();
        }
    }

    private void upsertPoll(Poll poll) {
        PollRepository.getInstance().upsertPoll(poll);
    }

    private Poll getPollFromDatabase(long pollId) {
        return PollRepository.getInstance().getPollById(pollId);
    }

    private void upsertPollVotedStatus(Poll poll) {
        PollRepository.getInstance().upsertPollVoteStatus(poll);
    }

    public interface EventPost {
        void post(Object o);
    }

    public interface AccountRefreshPost {
        void refresh(Object o);
    }

    public interface JandiRestarter {
        void restart();
    }

}
