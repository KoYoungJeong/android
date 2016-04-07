package com.tosslab.jandi.app.ui.members.search.model;

import android.text.TextUtils;

import com.tosslab.jandi.app.lists.BotEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.ui.members.model.MembersModel;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

/**
 * Created by tonyjs on 16. 4. 7..
 */
public class MemberSearchModel {

    public Observable<List<FormattedEntity>> getEnabledMembersObservable() {
        return Observable.<List<FormattedEntity>>create(subscriber -> {
            try {
                List<FormattedEntity> enabledTeamMember = MembersModel.getEnabledTeamMember();
                subscriber.onNext(enabledTeamMember);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    public List<FormattedEntity> getSearchedMembers(final String query,
                                                    List<FormattedEntity> currentMembers) {
        List<FormattedEntity> searchedMembers = new ArrayList<>();
        if (currentMembers == null || currentMembers.isEmpty()) {
            return searchedMembers;
        }

        Observable.from(currentMembers)
                .filter(member -> {
                    if (TextUtils.isEmpty(query)) {
                        return true;
                    }

                    return member.getName().toLowerCase().contains(query.toLowerCase());
                })
                .toSortedList((entity, entity2) -> {
                    if (entity instanceof BotEntity) {
                        return -1;
                    } else if (entity2 instanceof BotEntity) {
                        return 1;
                    } else {
                        return entity.getName().toLowerCase()
                                .compareTo(entity2.getName().toLowerCase());
                    }
                })
                .subscribe(searchedMembers::addAll);

        return searchedMembers;
    }
}
