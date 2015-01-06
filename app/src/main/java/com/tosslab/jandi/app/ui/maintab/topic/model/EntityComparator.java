package com.tosslab.jandi.app.ui.maintab.topic.model;

import com.tosslab.jandi.app.lists.FormattedEntity;

import java.util.Comparator;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class EntityComparator implements Comparator<FormattedEntity> {

    @Override
    public int compare(FormattedEntity lhs, FormattedEntity rhs) {

        if (lhs.isStarred && rhs.isStarred) {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        } else if (lhs.isStarred) {
            return -1;
        } else if (rhs.isStarred) {
            return 1;
        } else {
            return lhs.getName().compareToIgnoreCase(rhs.getName());
        }
    }
}
