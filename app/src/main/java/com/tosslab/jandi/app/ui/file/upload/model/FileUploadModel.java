package com.tosslab.jandi.app.ui.file.upload.model;

import android.content.Context;
import android.text.TextUtils;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

@EBean
public class FileUploadModel {

    public static final String FILE_SAPERATOR = "/";

    public String getFileName(String path) {
        int separatorIndex = path.lastIndexOf(FILE_SAPERATOR);
        return (separatorIndex < 0) ? path : path.substring(separatorIndex + 1, path.length());
    }

    public List<FormattedEntity> getEntityInfoWithoutMe(Context context) {
        EntityManager entityManager = EntityManager.getInstance(context);
        List<FormattedEntity> unsharedEntities = entityManager.retrieveExclusivedEntities(Arrays.asList(entityManager.getMe().getId()));

        Iterator<FormattedEntity> enabledEntities = Observable.from(unsharedEntities)
                .filter(entity -> !entity.isUser() || TextUtils.equals(entity.getUser().status, "enabled")).toBlocking()
                .getIterator();

        List<FormattedEntity> formattedEntities = new ArrayList<>();

        while (enabledEntities.hasNext()) {
            formattedEntities.add(enabledEntities.next());
        }

        return formattedEntities;
    }

    public String getEntityString(Context context, int selectedEntityIdToBeShared) {

        EntityManager entityManager = EntityManager.getInstance(context);
        FormattedEntity entity = entityManager.getEntityById(selectedEntityIdToBeShared);

        String originEntityName = entity.getName();

        String prefix;

        if (entity.isUser()) {
            prefix = "@";
        } else {
            prefix = "#";

        }

        return String.format("%s%s", prefix, originEntityName);
    }
}
