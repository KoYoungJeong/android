package com.tosslab.jandi.app.ui.member.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.maintab.topic.model.EntityComparator;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 2. 16..
 */
@Deprecated
@EBean
public class TeamInfoModel {

    @RootContext
    Context context;

    public List<FormattedEntity> retrieveTeamUserList() {
        List<FormattedEntity> formattedUsers = EntityManager.getInstance(context).getFormattedUsers();

        Iterator<FormattedEntity> enabled = Observable.from(formattedUsers)
                .filter(entity -> TextUtils.equals(entity.getUser().status, "enabled")).toBlocking()
                .getIterator();

        List<FormattedEntity> users = new ArrayList<FormattedEntity>();
        while (enabled.hasNext()) {
            users.add(enabled.next());
        }

        Collections.sort(users, new EntityComparator());

        return users;
    }

}
