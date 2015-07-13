package com.tosslab.jandi.app.ui.web.model;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.ui.maintab.topic.model.EntityComparator;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import retrofit.RetrofitError;
import rx.Observable;

/**
 * Created by Steve SeongUg Jung on 15. 2. 26..
 */
@EBean
public class InternalWebModel {

    @SystemService
    ClipboardManager clipboardManager;

    @RootContext
    Context context;

    public void copyToClipboard(String contentString) {
        final ClipData clipData = ClipData.newPlainText("", contentString);
        clipboardManager.setPrimaryClip(clipData);
    }

    public List<FormattedEntity> getEntities() {
        EntityManager entityManager = EntityManager.getInstance(context);
        List<FormattedEntity> joinedChannels = entityManager.getJoinedChannels();
        List<FormattedEntity> groups = entityManager.getGroups();
        List<FormattedEntity> formattedUsersWithoutMe = entityManager.getFormattedUsersWithoutMe();

        List<FormattedEntity> entities = new ArrayList<FormattedEntity>();
        entities.addAll(joinedChannels);
        entities.addAll(groups);

        Collections.sort(entities, new EntityComparator());

        Iterator<FormattedEntity> enabledUsers = Observable.from(formattedUsersWithoutMe)
                .filter(entity -> TextUtils.equals(entity.getUser().status, "enabled"))
                .toBlocking()
                .getIterator();

        while (enabledUsers.hasNext()) {
            entities.add(enabledUsers.next());
        }

        return Collections.unmodifiableList(entities);
    }

    public String createMessage(String title, String url) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(title).append("\n").append(url);
        return buffer.toString();
    }

    public void sendMessage(int entityId, int entityType, String text) throws RetrofitError {
        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(context);
        messageManipulator.initEntity(entityType, entityId);
        messageManipulator.sendMessage(text);
    }
}
