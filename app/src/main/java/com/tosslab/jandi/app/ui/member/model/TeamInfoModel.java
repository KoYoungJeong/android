package com.tosslab.jandi.app.ui.member.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 2. 16..
 */
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

        return users;
    }

}
