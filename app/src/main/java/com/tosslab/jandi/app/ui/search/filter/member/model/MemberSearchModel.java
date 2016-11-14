package com.tosslab.jandi.app.ui.search.filter.member.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.StringCompareUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public class MemberSearchModel {

    public static List<User> getEnabledTeamMember() {
        List<User> members = new ArrayList<>();

        members.addAll(TeamInfoLoader.getInstance().getUserList());

        Observable.from(members)
                .filter(User::isEnabled)
                .filter(it -> !it.isBot())
                .toSortedList((entity, entity2) -> {
                    return StringCompareUtil.compare(entity.getName(), entity2.getName());
                })
                .subscribe(entities -> {
                    members.clear();
                    members.addAll(entities);
                }, Throwable::printStackTrace);
        return members;
    }

    public Observable<List<User>> getEnabledMembersObservable() {
        return Observable.<List<User>>create(subscriber -> {
            try {
                List<User> enabledTeamMember = getEnabledTeamMember();
                subscriber.onNext(enabledTeamMember);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    public List<User> getSearchedMembers(final String query,
                                         List<User> currentMembers) {
        List<User> searchedMembers = new ArrayList<>();
        if (currentMembers == null || currentMembers.isEmpty()) {
            return searchedMembers;
        }

        Observable.from(currentMembers)
                .filter(member -> {
                    if (TextUtils.isEmpty(query)) {
                        return true;
                    }

                    return member.getName().toLowerCase().contains(query.toLowerCase())
                            || member.getDivision().toLowerCase().contains(query.toLowerCase());
                })
                .toSortedList((entity, entity2) -> {
                    if (entity.isBot()) {
                        return -1;
                    } else if (entity2.isBot()) {
                        return 1;
                    } else {
                        return entity.getName().toLowerCase()
                                .compareTo(entity2.getName().toLowerCase());
                    }
                })
                .subscribe(searchedMembers::addAll, Throwable::printStackTrace);

        return searchedMembers;
    }

}
