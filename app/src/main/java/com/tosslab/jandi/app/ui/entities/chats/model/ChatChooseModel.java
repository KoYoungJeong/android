package com.tosslab.jandi.app.ui.entities.chats.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;
import com.tosslab.jandi.app.ui.entities.chats.domain.DisableDummyItem;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func2;

public class ChatChooseModel {

    private Func2<ChatChooseItem, ChatChooseItem, Integer> getChatItemComparator() {
        return (lhs, rhs) -> {
            int compareValue = 0;
            if (lhs.isEnabled()) {
                if (rhs.isEnabled()) {
                    compareValue = 0;
                } else {
                    compareValue = -1;
                }
            } else {
                if (rhs.isEnabled()) {
                    compareValue = 1;
                } else {
                    compareValue = 0;
                }
            }

            if (compareValue != 0) {
                return compareValue;
            }

            if (lhs.isStarred()) {
                if (rhs.isStarred()) {

                    if (lhs.isBot()) {
                        return -1;
                    } else if (rhs.isBot()) {
                        return 1;
                    } else {
                        String lhsName, rhsName;
                        if (!lhs.isInactive()) {
                            lhsName = lhs.getName();
                        } else {
                            lhsName = lhs.getEmail();
                        }

                        if (!rhs.isInactive()) {
                            rhsName = rhs.getName();
                        } else {
                            rhsName = rhs.getEmail();
                        }

                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());

                    }
                } else {
                    return -1;
                }
            } else {
                if (rhs.isStarred()) {
                    return 1;
                } else {
                    if (lhs.isBot()) {
                        return -1;
                    } else if (rhs.isBot()) {
                        return 1;
                    } else {
                        String lhsName, rhsName;
                        if (!lhs.isInactive()) {
                            lhsName = lhs.getName();
                        } else {
                            lhsName = lhs.getEmail();
                        }

                        if (!rhs.isInactive()) {
                            rhsName = rhs.getName();
                        } else {
                            rhsName = rhs.getEmail();
                        }

                        return StringCompareUtil.compare(lhs.getName(), rhs.getName());
                    }
                }
            }
        };
    }

    public List<ChatChooseItem> getChatListWithoutMe(String name) {

        List<User> users = TeamInfoLoader.getInstance().getUserList();
        long myId = TeamInfoLoader.getInstance().getMyId();

        List<ChatChooseItem> chatChooseItems = new ArrayList<>();

        Observable.from(users)
                .filter(user -> user.getId() != myId)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .filter(user -> !TextUtils.isEmpty(user.getName()) && user.getName().toLowerCase().contains(name.toLowerCase()))
                .map(ChatChooseItem::create)
                .mergeWith(Observable.create(subscriber -> {
                    // 잔디봇이 포함되어 있고 잔디봇의 이름이 포함되어 있는 경우 추가한다
                    if (hasJandiBot()) {
                        ChatChooseItem jandiBot = getJandiBot();
                        if (jandiBot.getName().toLowerCase().contains(name.toLowerCase())) {
                            subscriber.onNext(jandiBot);
                        }
                        subscriber.onCompleted();
                    }
                }))
                .toSortedList(getChatItemComparator())
                .collect(() -> chatChooseItems, List::addAll)
                .subscribe(it -> {} , Throwable::printStackTrace);


        return chatChooseItems;

    }

    public long getTeamId() {
        return AccountRepository.getRepository().getSelectedTeamId();
    }

    private boolean hasDisabledUsers() {

        return TeamInfoLoader.getInstance().hasDisabledUser();
    }

    public List<ChatChooseItem> getUsers() {

        List<ChatChooseItem> users = getEnableUsers();

        boolean hasDisabledUsers = hasDisabledUsers();
        if (hasDisabledUsers) {
            users.add(new DisableDummyItem());
        }

        return users;
    }

    private List<ChatChooseItem> getEnableUsers() {
        List<User> users = TeamInfoLoader.getInstance().getUserList();

        List<ChatChooseItem> chatChooseItems = new ArrayList<>();

        long myId = TeamInfoLoader.getInstance().getMyId();
        Observable.from(users)
                .filter(user -> user.getId() != myId)
                .filter(user -> !TeamInfoLoader.getInstance().isJandiBot(user.getId()))
                .filter(User::isEnabled)
                .map(ChatChooseItem::create)
                .mergeWith(Observable.create(subscriber -> {
                    if (hasJandiBot()) {
                        subscriber.onNext(getJandiBot());
                    }

                    subscriber.onCompleted();
                }))
                .toSortedList(getChatItemComparator())
                .collect(() -> chatChooseItems, List::addAll)
                .subscribe(it -> {}, Throwable::printStackTrace);


        return chatChooseItems;
    }

    private ChatChooseItem getJandiBot() {
        User bot = TeamInfoLoader.getInstance().getJandiBot();
        return new ChatChooseItem()
                .enabled(bot.isEnabled())
                .isBot(true)
                .entityId(bot.getId())
                .name(bot.getName())
                .myId(false)
                .level(Level.Member)
                .owner(false)
                .photoUrl(bot.getPhotoUrl())
                .starred(bot.isStarred());
    }

    private boolean hasJandiBot() {
        return TeamInfoLoader.getInstance().hasJandiBot();
    }

}
