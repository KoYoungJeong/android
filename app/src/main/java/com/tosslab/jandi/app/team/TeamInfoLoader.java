package com.tosslab.jandi.app.team;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.models.start.Bot;
import com.tosslab.jandi.app.network.models.start.Chat;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.network.models.start.Team;
import com.tosslab.jandi.app.network.models.start.Topic;
import com.tosslab.jandi.app.team.member.Member;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.team.member.WebhookBot;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.team.room.Room;
import com.tosslab.jandi.app.team.room.TopicFolder;
import com.tosslab.jandi.app.team.room.TopicRoom;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import rx.Observable;
import rx.functions.Func0;

public class TeamInfoLoader {
    private static TeamInfoLoader teamInfoLoader;

    private Lock lock;
    private InitialInfo initialInfo;


    private List<TopicFolder> topicFolders;

    private List<Room> rooms;
    private Map<Long, DirectMessageRoom> chatRooms;
    private Map<Long, TopicRoom> topicRooms;

    private List<Member> members;
    private Map<Long, User> users;
    private Map<Long, WebhookBot> bots;

    private User me;
    private Team team;
    private User jandiBot;

    private int pollBadge;

    private TeamInfoLoader() {
        lock = new ReentrantLock();
        rooms = new ArrayList<>();
        chatRooms = new HashMap<>();
        topicRooms = new HashMap<>();

        topicFolders = new ArrayList<>();
        members = new ArrayList<>();
        users = new HashMap<>();
        bots = new HashMap<>();

        refresh();
    }

    public TeamInfoLoader(long teamId) {
        lock = new ReentrantLock();
        rooms = new ArrayList<>();
        chatRooms = new HashMap<>();
        topicRooms = new HashMap<>();

        topicFolders = new ArrayList<>();
        members = new ArrayList<>();
        users = new HashMap<>();
        bots = new HashMap<>();

        refresh(teamId);
    }

    synchronized public static TeamInfoLoader getInstance() {
        if (teamInfoLoader == null) {
            teamInfoLoader = new TeamInfoLoader();
        }
        return teamInfoLoader;
    }

    /**
     * 재사용하지 않음<br/>
     * Share 등 특수한 상황에서 쓰기 위함
     *
     * @param teamId 정보가 필요한 팀 ID
     * @return 팀 정보
     */
    public static TeamInfoLoader getInstance(long teamId) {
        return new TeamInfoLoader(teamId);
    }

    public void refresh() {
        execute(() -> {
            long teamId = AccountRepository.getRepository().getSelectedTeamId();
            if (teamId > 0) {
                refresh(teamId);
            }
        });
    }

    public void refresh(InitialInfo info) {
        execute(() -> {
            TeamInfoLoader.this.initialInfo = info;
            setUp();
        });
    }

    public void refresh(long teamId) {
        execute(() -> {
            initialInfo = InitialInfoRepository.getInstance().getInitialInfo(teamId);
            if (initialInfo != null) {
                setUp();
            }
        });
    }

    private void setUp() {
        setUpTeam();
        setUpRooms();
        setUpMembers();
        setUpMe();
        setUpTopicFolders();
        setUpPollBadge();
    }

    private void setUpPollBadge() {
        InitialInfo.Poll poll = initialInfo.getPoll();
        setPollBadge(poll != null ? poll.getVotableCount() : 0);
    }

    private void setUpTeam() {
        team = initialInfo.getTeam();
    }

    private void setUpMe() {
        long myId = initialInfo.getSelf().getId();
        if (users.containsKey(myId)) {
            this.me = users.get(myId);
        } else {
            getUserObservable()
                    .takeFirst(human -> human.getId() == myId)
                    .map(User::new)
                    .subscribe(it -> {
                        this.me = it;
                    });
        }
    }

    private void setUpRooms() {
        rooms.clear();
        chatRooms.clear();
        topicRooms.clear();

        getTopicObservable()
                .map(TopicRoom::new)
                .subscribe(topic -> {
                    topicRooms.put(topic.getId(), topic);
                    rooms.add(topic);
                });
        getChatObservable()
                .map(DirectMessageRoom::new)
                .subscribe(chatRoom -> {
                    chatRooms.put(chatRoom.getId(), chatRoom);
                    rooms.add(chatRoom);
                });
    }

    private void setUpMembers() {
        members.clear();
        users.clear();
        bots.clear();
        jandiBot = null;

        getUserObservable()
                .map(User::new)
                .subscribe(user -> {
                    members.add(user);
                    users.put(user.getId(), user);
                    if (user.isBot()) {
                        jandiBot = user;
                    }
                });

        getBotObservable()
                .map(WebhookBot::new)
                .subscribe(bot -> {
                    members.add(bot);
                    bots.put(bot.getId(), bot);
                });

        if (jandiBot == null) {
            jandiBot = new User(new Human());
        }
    }

    private void setUpTopicFolders() {
        topicFolders.clear();

        getFolderObservable()
                .map(folder -> {
                    List<TopicRoom> topicRooms = new ArrayList<>();
                    Collection<Long> rooms = folder.getRooms();
                    if (rooms != null) {
                        Observable.from(rooms)
                                .filter(roomId -> TeamInfoLoader.this.topicRooms.containsKey(roomId))
                                .map(roomId -> TeamInfoLoader.this.topicRooms.get(roomId))
                                .collect(() -> topicRooms, List::add)
                                .subscribe();

                    }

                    return new TopicFolder(folder, topicRooms);
                })
                .toSortedList((lhs, rhs) -> lhs.getSeq() - rhs.getSeq())
                .subscribe(topicFolder -> {
                    topicFolders.addAll(topicFolder);
                });
    }

    private <T> T execute(Call0<T> call) {
        if (call == null) {
            return null;
        }

        lock.lock();

        try {
            return call.execute();
        } finally {
            lock.unlock();
        }
    }

    private void execute(Call1 call) {
        if (call == null) {
            return;
        }

        lock.lock();

        try {
            call.execute();
        } finally {
            lock.unlock();
        }
    }

    public String getMemberName(long memberId) {

        return execute(() -> Observable.from(members)
                .takeFirst(member -> member.getId() == memberId)
                .map(Member::getName)
                .defaultIfEmpty("")
                .toBlocking()
                .first());
    }

    private Observable<Human> getUserObservable() {
        return execute(() -> Observable.from(initialInfo.getMembers()));
    }

    public List<User> getUserList() {
        return execute(() -> Collections.unmodifiableList(new ArrayList<>(users.values())));
    }

    private Observable<Bot> getBotObservable() {
        return execute(() -> Observable.from(initialInfo.getBots()));
    }

    private Observable<Folder> getFolderObservable() {
        return execute(() -> Observable.from(initialInfo.getFolders()));
    }

    private Observable<Topic> getTopicObservable() {
        return execute(() -> Observable.from(initialInfo.getTopics()));
    }

    public List<TopicRoom> getTopicList() {
        return execute(() -> Collections.unmodifiableList(new ArrayList<>(topicRooms.values())));
    }

    private Observable<Chat> getChatObservable() {
        return execute(() -> Observable.from(initialInfo.getChats()));
    }

    public long getMyId() {
        return execute(() -> me.getId());
    }

    public boolean isAnnouncementOpened(long topicId) {
        return execute(() -> topicRooms.get(topicId).isAnnouncementOpened());
    }

    public boolean isStarredUser(long userId) {
        return execute(() -> {
            if (users.containsKey(userId)) {
                return users.get(userId).isStarred();
            } else {
                return false;
            }
        });
    }

    public long getChatId(long userId) {
        return execute(() -> getChatObservable().takeFirst(chat -> chat.getMembers().contains(userId))
                .map(Chat::getId)
                .defaultIfEmpty(-1L)
                .toBlocking()
                .first());
    }

    public String getName(long id) {
        return execute(() -> {

            if (topicRooms.containsKey(id)) {
                return topicRooms.get(id).getName();
            } else if (users.containsKey(id)) {
                return users.get(id).getName();
            } else if (bots.containsKey(id)) {
                return bots.get(id).getName();
            }

            return "";
        });
    }

    public boolean isBot(long id) {
        return execute(() -> bots.containsKey(id));
    }

    public boolean isUser(long id) {
        return execute(() -> users.containsKey(id));
    }

    public boolean isEnabled(long id) {
        return execute(() -> {
            if (topicRooms.containsKey(id)) {
                return topicRooms.get(id).isEnabled();
            } else if (users.containsKey(id)) {
                return users.get(id).isEnabled();
            } else if (bots.containsKey(id)) {
                return bots.get(id).isEnabled();
            } else {
                return false;
            }
        });
    }

    public boolean isStarred(long roomId) {
        return execute(() -> {

            if (topicRooms.containsKey(roomId)) {
                return topicRooms.get(roomId).isStarred();
            } else if (chatRooms.containsKey(roomId)) {
                return isStarredUser(chatRooms.get(roomId).getCompanionId());
            }

            return false;
        });
    }

    public boolean isPushSubscribe(long topicId) {
        return execute(() -> {
            if (topicRooms.containsKey(topicId)) {
                return topicRooms.get(topicId).isPushSubscribe();
            } else {
                return false;
            }
        });
    }

    public boolean isTopicOwner(long topicId, long myId) {
        return execute(() -> {
            if (topicRooms.containsKey(topicId)) {
                return topicRooms.get(topicId).getCreatorId() == myId;
            } else {
                return false;
            }
        });
    }

    public boolean isPublicTopic(long topicId) {
        return execute(() -> {
            if (topicRooms.containsKey(topicId)) {
                return topicRooms.get(topicId).isPublicTopic();
            } else {
                return false;
            }
        });
    }

    public int getTopicMemberCount(long topicId) {
        return execute(() -> {
            if (topicRooms.containsKey(topicId)) {
                return topicRooms.get(topicId).getMemberCount();
            } else {
                return 1;
            }
        });
    }

    public List<User> getTopicMember(long topicId) {
        return execute(() -> {

            if (topicRooms.containsKey(topicId)) {
                return Observable.from(topicRooms.get(topicId).getMembers())
                        .map(memberId -> users.get(memberId))
                        .collect((Func0<ArrayList<User>>) ArrayList::new, List::add)
                        .toBlocking()
                        .first();
            } else {
                return new ArrayList<User>();
            }

        });
    }

    public boolean isDefaultTopic(long topicId) {
        return execute(() -> {
            if (topicRooms.containsKey(topicId)) {
                return topicRooms.get(topicId).isDefaultTopic();
            } else {
                return false;
            }
        });
    }

    public long getTeamId() {
        return execute(() -> team.getId());
    }

    public boolean hasDisabledUser() {
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
        return execute(() -> jandiBot != null && jandiBot.getId() == memberId);
    }

    public boolean hasJandiBot() {
        return execute(() -> jandiBot != null && jandiBot.getId() > 0);
    }

    public Room getRoom(long roomId) {
        return execute(() -> {
            if (topicRooms.containsKey(roomId)) {
                return topicRooms.get(roomId);
            } else if (chatRooms.containsKey(roomId)) {
                return chatRooms.get(roomId);
            }
            return null;
        });
    }

    public boolean isRoom(long roomId) {
        return execute(() -> topicRooms.containsKey(roomId) || chatRooms.containsKey(roomId));
    }

    public long getDefaultTopicId() {
        return execute(() -> getTopicObservable()
                .takeFirst(Topic::isDefault)
                .map(Topic::getId)
                .defaultIfEmpty(-1L)
                .toBlocking()
                .first());
    }

    public User getUser(long memberId) {
        return execute(() -> users.get(memberId));
    }

    public boolean isTopic(long id) {
        return execute(() -> topicRooms.containsKey(id));
    }

    public boolean isChat(long id) {
        return execute(() -> chatRooms.containsKey(id));
    }

    public WebhookBot getBot(long botId) {
        return execute(() -> {
            if (bots.containsKey(botId)) {
                return bots.get(botId);
            } else {
                return null;
            }
        });
    }

    public boolean isMember(long memberId) {
        return execute(() -> users.containsKey(memberId) || bots.containsKey(memberId));
    }

    public Member getMember(long memberId) {
        return execute(() -> {
            if (users.containsKey(memberId)) {
                return users.get(memberId);
            } else if (bots.containsKey(memberId)) {
                return bots.get(memberId);
            }
            return null;
        });
    }

    public String getTeamName() {
        return execute(() -> team.getName());
    }

    public TopicRoom getTopic(long topicId) {
        return execute(() -> {
            if (topicRooms.containsKey(topicId)) {
                return topicRooms.get(topicId);
            } else {
                return null;
            }
        });
    }

    public String getTeamDomain() {
        return execute(() -> team.getDomain());
    }

    public List<TopicFolder> getTopicFolders() {
        return execute(() -> Collections.unmodifiableList(topicFolders));
    }

    public List<DirectMessageRoom> getDirectMessageRooms() {
        return execute(() -> Collections.unmodifiableList(new ArrayList<>(chatRooms.values())));
    }

    public DirectMessageRoom getChat(long roomId) {
        return execute(() -> chatRooms.get(roomId));
    }

    public String getInvitationUrl() {
        return execute(() -> team.getInvitationUrl());
    }

    public String getInvitationStatus() {
        return execute(() -> team.getInvitationStatus());
    }

    public int getPollBadge() {
        return execute(() -> pollBadge);
    }

    private void setPollBadge(int pollBadge) {
        this.pollBadge = pollBadge;
    }

    interface Call0<T> {
        T execute();
    }

    interface Call1 {
        void execute();
    }
}
