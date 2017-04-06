package com.tosslab.jandi.app.team;

import android.support.v4.util.ArrayMap;
import android.support.v4.util.LongSparseArray;
import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.BotRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.ChatRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.FolderRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.HumanRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialMentionInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialPollInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RankRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TeamPlanRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TeamRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TeamUsageRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.TopicRepository;
import com.tosslab.jandi.app.network.json.JsonMapper;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Marker;
import com.tosslab.jandi.app.network.models.start.Mention;
import com.tosslab.jandi.app.network.models.start.Poll;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.network.models.start.TeamPlan;
import com.tosslab.jandi.app.network.models.start.TeamUsage;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.network.models.team.rank.Rank;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.member.WebhookBot;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.utils.JandiPreference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rx.Observable;
import rx.functions.Func0;

public class TeamInfoLoader {
    private static LongSparseArray<TeamInfoLoader> teamInfoLoader;
    private final long teamId;
    private Lock lock;
    private InitialInfo initialInfo;

    private FolderRepository topicFolders;

    private ChatRepository chatRooms;
    private TopicRepository topicRooms;

    private HumanRepository users;
    private BotRepository bots;
    private ArrayMap<Long, Rank> ranks;

    private TeamRepository teamRepository;
    private InitialMentionInfoRepository mention;
    private TeamPlanRepository teamPlan;
    private TeamUsageRepository teamUsage;

    private User me;
    private User jandiBot;

    private InitialPollInfoRepository poll;


    private TeamInfoLoader(long teamId) {
        this.teamId = teamId;
        lock = new ReentrantLock();
        refresh(teamId);
    }

    public static TeamInfoLoader getInstance() {
        return getInstance(AccountRepository.getRepository().getSelectedTeamId());
    }

    synchronized public static TeamInfoLoader getInstance(long teamId) {

        if (teamInfoLoader == null) {
            teamInfoLoader = new LongSparseArray<>();
        }

        if (teamInfoLoader.indexOfKey(teamId) < 0) {
            teamInfoLoader.put(teamId, new TeamInfoLoader(teamId));
        }

        return teamInfoLoader.get(teamId);
    }

    public void refresh() {
        execute(() -> {
            refresh(teamId);
            JandiPreference.setSocketConnectedLastTime(initialInfo.getTs());
        });
    }

    private void refresh(long teamId) {
        execute(() -> {
            RawInitialInfo rawInitialInfo = InitialInfoRepository.getInstance().getRawInitialInfo(teamId);
            if (rawInitialInfo != null) {
                try {
                    initialInfo = JsonMapper.getInstance().getGson()
                            .fromJson(rawInitialInfo.getRawValue(), InitialInfo.class);
                } catch (Exception e) {
                    e.printStackTrace();
                    initialInfo = null;
                }
            } else {
                initialInfo = null;
            }
            setUp(teamId);
        });
    }

    private void setUp(long teamId) {
        users = HumanRepository.getInstance(teamId);
        bots = BotRepository.getInstance(teamId);
        chatRooms = ChatRepository.getInstance(teamId);
        topicRooms = TopicRepository.getInstance(teamId);
        topicFolders = FolderRepository.getInstance(teamId);
        poll = InitialPollInfoRepository.getInstance(teamId);
        teamPlan = TeamPlanRepository.getInstance(teamId);
        teamUsage = TeamUsageRepository.getInstance(teamId);
        mention = InitialMentionInfoRepository.getInstance(teamId);
        teamRepository = TeamRepository.getInstance(teamId);

        users.clear();
        bots.clear();
        chatRooms.clear();
        topicRooms.clear();
        topicFolders.clear();

        if (ranks == null) {
            ranks = new ArrayMap<>();
        }

        ranks.clear();

        if (initialInfo != null) {
            setUpTeam();
            setUpPollBadge();
            setUpTeamPlan();
            setUpTeamUsage();

            setUpMention();

            setUpRanks();
            setUpMembers();
            setUpMe();
            setUpRooms();

            setUpTopicFolders();

        } else {
            jandiBot = null;
            me = null;
        }
    }

    private void setUpTeamUsage() {
        this.teamUsage.updateTeamUsage(initialInfo.getTeamUsage());
    }

    private void setUpRanks() {
        Observable.from(RankRepository.getInstance().getRanks(teamId))
                .collect(() -> ranks, (maps, rank) -> maps.put(rank.getId(), rank))
                .subscribe();
    }

    private void setUpMention() {
        this.mention.upsertMention(initialInfo.getMention());
    }

    private void setUpTeamPlan() {
        this.teamPlan.updateTeamPlan(initialInfo.getTeamPlan());
    }

    @Deprecated
    public void refreshMention() {
        execute(() -> {
//            if (initialInfo != null) {
//                Mention newMention = InitialMentionInfoRepository.getInstance(teamRepository.getId()).getMention();
//                this.mention = newMention;
//                initialInfo.setMention(mention);
//            }
        });
    }

    private void setUpPollBadge() {
        Poll poll = initialInfo.getPoll();
        this.poll.updateVotableCount(poll.getVotableCount());
    }

    private void setUpTeam() {
        teamRepository.updateTeam(initialInfo.getTeam());
    }

    private void setUpMe() {
        long myId = initialInfo.getSelf().getId();
        if (users.hasUser(myId)) {
            this.me = users.getUser(myId);
            // ref by InitializeInfoConverter.java
            this.me.getRaw().setIsStarred(false);
        } else {
            getUserObservable()
                    .takeFirst(human -> human.getId() == myId)
                    .map((human1) -> new User(human1, ranks.get(human1.getRankId())))
                    .subscribe(it -> this.me = it);
        }
    }

    private void setUpRooms() {

        getTopicObservable()
                .doOnNext(topic -> {
                    // ref by TopicConverter.java
                    List<Marker> markers = topic.getMarkers();

                    if (markers == null || markers.isEmpty()) {
                        // marker 가 없으면 임의로 지정함
                        List<Marker> markers1 = new ArrayList<>();
                        for (Long id : topic.getMembers()) {
                            Marker marker = new Marker();
                            marker.setMemberId(id);
                            marker.setReadLinkId(topic.getLastLinkId() > 0 ? topic.getLastLinkId() : -1);
                            markers1.add(marker);
                        }
                        topic.setMarkers(markers1);
                    }
                })
                .map(TopicRoom::new)
                .subscribe(topic -> topicRooms.addTopicRoom(topic.getId(), topic));

        final long myId = me.getId();
        getChatObservable()
                .doOnNext(chat -> {
                    // ref by : InitializeInfoConverter.java
                    if (!chat.isOpened()) {
                        // close 상태인 Chat 에 대해 최소 정보 추가
                        List<Long> members = chat.getMembers();
                        if (members != null && !members.isEmpty()) {
                            for (Long memberId : members) {
                                if (memberId != myId) {
                                    chat.setCompanionId(memberId);
                                    break;
                                }
                            }
                        }
                    }

                    // ref by ChatConverter.java
                    List<Marker> markers = chat.getMarkers();

                    long lastLinkId = chat.getLastLinkId() > 0 ? chat.getLastLinkId() : 0;
                    if (markers == null || markers.isEmpty()) {
                        // marker 가 없으면 임의로 지정함
                        List<Marker> markers1 = new ArrayList<>();
                        for (Long id : chat.getMembers()) {
                            Marker marker = new Marker();
                            marker.setMemberId(id);
                            marker.setReadLinkId(lastLinkId);
                            markers1.add(marker);
                        }
                        chat.setMarkers(markers1);
                    }

                    if (!chat.isOpened()) {
                        chat.setReadLinkId(lastLinkId);
                    }
                })
                .map(DirectMessageRoom::new)
                .subscribe(chatRoom -> chatRooms.addDirectRoom(chatRoom.getId(), chatRoom));
    }

    private void setUpMembers() {
        jandiBot = null;

        getUserObservable()
                .map((human) -> new User(human, ranks.get(human.getRankId())))
                .subscribe(user -> {
                    users.addUser(user);
                    if (user.isBot()) {
                        jandiBot = user;
                    }
                });

        getBotObservable()
                .map(WebhookBot::new)
                .subscribe(bot -> bots.addWebhookBot(bot.getId(), bot));

        if (jandiBot == null) {
            jandiBot = new User(new Human());
        }
    }

    private void setUpTopicFolders() {

        Observable.from(initialInfo.getFolders())
                .subscribe(topicFolders::addFolder);

    }

    private <T> T execute(Call0<T> call) {
        lock.lock();

        try {
            if (call == null) {
                return null;
            }

            return call.execute();
        } finally {
            lock.unlock();
        }
    }

    private void execute(Call1 call) {
        lock.lock();

        try {
            if (call == null) {
                return;
            }
            call.execute();
        } finally {
            lock.unlock();
        }
    }

    public String getMemberName(long memberId) {

        return execute(() -> {
            if (users.hasUser(memberId)) {
                return users.getUser(memberId).getName();
            } else if (bots.hasBot(memberId)) {
                return bots.getWebhookBot(memberId).getName();
            }
            {
                return "";
            }
        });
    }

    private Observable<Human> getUserObservable() {
        return execute(() -> Observable.from(initialInfo.getMembers()));
    }

    public List<User> getUserList() {
        return execute(() -> Collections.unmodifiableList(new ArrayList<>(users.getUsers())));
    }

    private Observable<Bot> getBotObservable() {
        return execute(() -> {
            if (initialInfo.getBots() == null) {
                return Observable.empty();
            }
            return Observable.from(initialInfo.getBots());
        });
    }

    private Observable<Topic> getTopicObservable() {
        return execute(() -> Observable.from(initialInfo.getTopics()));
    }

    public List<TopicRoom> getTopicList() {
        return execute(() -> Collections.unmodifiableList(new ArrayList<>(topicRooms.getTopicRooms())));
    }

    private Observable<Chat> getChatObservable() {
        return execute(() -> {
            if (initialInfo == null || initialInfo.getChats() == null) {
                return Observable.empty();
            }
            return Observable.from(initialInfo.getChats());
        });
    }

    public long getMyId() {
        //noinspection ConstantConditions
        return execute(() -> me != null ? me.getId() : -1);
    }

    public Level getMyLevel() {
        return execute(() -> me != null ? me.getLevel() : Level.Member);
    }

    public boolean isAnnouncementOpened(long topicId) {
        //noinspection ConstantConditions
        return execute(() -> topicRooms.getTopicRoom(topicId).isAnnouncementOpened());
    }

    public boolean isStarredUser(long userId) {
        //noinspection ConstantConditions
        return execute(() -> users.hasUser(userId) && users.getUser(userId).isStarred());
    }

    public long getChatId(long userId) {
        //noinspection ConstantConditions
        return execute(() -> getChatObservable()
                .takeFirst(chat -> chat.getCompanionId() == userId)
                .map(Chat::getId)
                .defaultIfEmpty(-1L)
                .toBlocking()
                .first());
    }

    public String getName(long id) {
        return execute(() -> {
            if (topicRooms.isTopic(id)) {
                return topicRooms.getTopicRoom(id).getName();
            } else if (users.hasUser(id)) {
                return users.getUser(id).getName();
            } else if (bots.hasBot(id)) {
                return bots.getWebhookBot(id).getName();
            }

            return "";
        });
    }

    public boolean isBot(long botId) {
        //noinspection ConstantConditions
        return execute(() -> bots.hasBot(botId));
    }

    public boolean isUser(long userId) {
        //noinspection ConstantConditions
        return execute(() -> users.hasUser(userId));
    }

    public boolean isEnabled(long id) {
        //noinspection ConstantConditions
        return execute(() -> {
            if (topicRooms.isTopic(id)) {
                return topicRooms.getTopicRoom(id).isEnabled();
            } else if (users.hasUser(id)) {
                return users.getUser(id).isEnabled();
            } else {
                return bots.hasBot(id) && bots.getWebhookBot(id).isEnabled();
            }
        });
    }

    public boolean isStarred(long roomId) {
        //noinspection ConstantConditions
        return execute(() -> {

            if (topicRooms.isTopic(roomId)) {
                return topicRooms.getTopicRoom(roomId).isStarred();
            } else if (chatRooms.hasChat(roomId)) {
                return isStarredUser(chatRooms.getDirectRoom(roomId).getCompanionId());
            }

            return false;
        });
    }

    public boolean isPushSubscribe(long topicId) {
        //noinspection ConstantConditions
        return execute(() -> topicRooms.isTopic(topicId)
                && topicRooms.getTopicRoom(topicId).isPushSubscribe());
    }

    public boolean isTopicOwner(long topicId, long myId) {
        //noinspection ConstantConditions
        return execute(() -> topicRooms.isTopic(topicId)
                && topicRooms.getTopicRoom(topicId).getCreatorId() == myId);
    }

    public boolean isPublicTopic(long topicId) {
        //noinspection ConstantConditions
        return execute(() -> topicRooms.isTopic(topicId)
                && topicRooms.getTopicRoom(topicId).isPublicTopic());
    }

    public int getTopicMemberCount(long topicId) {
        //noinspection ConstantConditions
        return execute(() -> {
            if (topicRooms.isTopic(topicId)) {
                return topicRooms.getTopicRoom(topicId).getMemberCount();
            } else {
                return 1;
            }
        });
    }

    public List<User> getTopicMember(long topicId) {
        return execute(() -> {

            if (topicRooms.isTopic(topicId)) {
                return Observable.from(topicRooms.getTopicRoom(topicId).getMembers())
                        .map(memberId -> users.getUser(memberId))
                        .collect((Func0<ArrayList<User>>) ArrayList::new, List::add)
                        .toBlocking()
                        .firstOrDefault(new ArrayList<>());
            } else {
                return new ArrayList<User>();
            }

        });
    }

    public boolean isDefaultTopic(long topicId) {
        //noinspection ConstantConditions
        return execute(() ->
                topicRooms.isTopic(topicId) && topicRooms.getTopicRoom(topicId).isDefaultTopic());
    }

    public long getTeamId() {
        //noinspection ConstantConditions
        return execute(() -> {
            if (teamRepository != null) {
                return teamId;
            }
            return -1L;
        });
    }

    public boolean hasDisabledUser() {
        //noinspection ConstantConditions
        return execute(() -> getUserObservable()
                .filter(human -> TextUtils.equals(human.getStatus(), "disabled"))
                .map(entity -> true)
                .firstOrDefault(false)
                .toBlocking()
                .first());

    }

    public User getJandiBot() {
        return execute(() -> jandiBot);
    }

    public boolean isJandiBot(long memberId) {
        //noinspection ConstantConditions
        return execute(() -> jandiBot != null && jandiBot.getId() == memberId);
    }

    public boolean hasJandiBot() {
        //noinspection ConstantConditions
        return execute(() -> jandiBot != null && jandiBot.getId() > 0);
    }

    public Room getRoom(long roomId) {
        return execute(() -> {
            if (topicRooms.isTopic(roomId)) {
                return topicRooms.getTopicRoom(roomId);
            } else if (chatRooms.hasChat(roomId)) {
                return chatRooms.getDirectRoom(roomId);
            }
            return null;
        });
    }

    public boolean isRoom(long roomId) {
        //noinspection ConstantConditions
        return execute(() -> topicRooms.isTopic(roomId) || chatRooms.hasChat(roomId));
    }

    public long getDefaultTopicId() {
        //noinspection ConstantConditions
        return execute(() -> Observable.from(topicRooms.getTopicRooms())
                .takeFirst(TopicRoom::isDefaultTopic)
                .map(TopicRoom::getId)
                .defaultIfEmpty(-1L)
                .toBlocking()
                .first());
    }

    public User getUser(long memberId) {
        return execute(() -> users.getUser(memberId));
    }

    public boolean isTopic(long id) {
        //noinspection ConstantConditions
        return execute(() -> topicRooms.isTopic(id));
    }

    public boolean isChat(long id) {
        //noinspection ConstantConditions
        return execute(() -> chatRooms.hasChat(id));
    }

    public WebhookBot getBot(long botId) {
        return execute(() -> {
            if (bots.hasBot(botId)) {
                return bots.getWebhookBot(botId);
            } else {
                return null;
            }
        });
    }

    public boolean isMember(long memberId) {
        //noinspection ConstantConditions
        return execute(() -> users.hasUser(memberId) || bots.hasBot(memberId));
    }

    public Member getMember(long memberId) {
        return execute(() -> {
            if (users.hasUser(memberId)) {
                return users.getUser(memberId);
            } else if (bots.hasBot(memberId)) {
                return bots.getWebhookBot(memberId);
            }
            return null;
        });
    }

    public String getTeamName() {
        return execute(() -> teamRepository.getTeam().getName());
    }

    public TopicRoom getTopic(long topicId) {
        return execute(() -> {
            if (topicRooms.isTopic(topicId)) {
                return topicRooms.getTopicRoom(topicId);
            } else {
                return null;
            }
        });
    }

    public String getTeamDomain() {
        return execute(() -> teamRepository.getTeam().getDomain());
    }

    public List<TopicFolder> getTopicFolders() {
        return execute(() -> {
            List<TopicFolder> folders = new ArrayList<>();
            TopicFolder topicFolder;
            ArrayList<TopicRoom> rooms;
            for (Folder folder : topicFolders.getFolders()) {
                rooms = new ArrayList<>();
                topicFolder = new TopicFolder(folder, rooms);
                folders.add(topicFolder);
                List<Long> roomsOfFolder = folder.getRooms();
                if (roomsOfFolder != null && !roomsOfFolder.isEmpty()) {
                    for (Long roomId : roomsOfFolder) {
                        rooms.add(topicRooms.getTopicRoom(roomId));
                    }
                }
            }
            return folders;
        });
    }

    public List<DirectMessageRoom> getDirectMessageRooms() {
        return execute(() -> Collections.unmodifiableList(new ArrayList<>(chatRooms.getDirectRooms())));
    }

    public DirectMessageRoom getChat(long roomId) {
        return execute(() -> chatRooms.getDirectRoom(roomId));
    }

    public String getInvitationUrl() {
        return execute(() -> teamRepository.getTeam().getInvitationUrl());
    }

    public String getInvitationStatus() {
        return execute(() -> teamRepository.getTeam().getInvitationStatus());
    }

    public int getPollBadge() {
        //noinspection ConstantConditions
        return execute(() -> poll.getVotableCount());
    }

    public Mention getMention() {
        return execute(() -> mention.getMention());
    }

    public TeamPlan getTeamPlan() {
        return execute(() -> teamPlan.getTeamPlan());
    }

    public TeamUsage getTeamUsage() {
        return execute(() -> teamUsage.getTeamUsage());
    }

    public Rank getRankOfGuest() {
        for (Rank rank : ranks.values()) {
            if (rank.getLevel() == Level.Guest.getLevel()) {
                return rank;
            }
        }
        return null;
    }

    public Rank getRankOfMember() {
        for (Rank rank : ranks.values()) {
            if (rank.getLevel() == Level.Member.getLevel()) {
                return rank;
            }
        }
        return null;
    }

    public void updateUser(long memberId) {
        execute(() -> {
            Human human = users.getHuman(memberId);
            if (human != null) {
                users.addUser(new User(human, ranks.get(human.getRankId())));
            }
        });
    }

    public void updateTeamUsage(TeamUsage usage) {
        execute(() -> {
            initialInfo.setTeamUsage(usage);
            setUpTeamUsage();
            InitialInfoRepository.getInstance().upsertInitialInfo(initialInfo);
        });
    }

    interface Call0<T> {
        T execute();
    }

    interface Call1 {
        void execute();
    }
}
