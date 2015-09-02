package com.tosslab.jandi.app.ui.web.model;

import com.tosslab.jandi.app.lists.FormattedEntity;

import java.util.Comparator;

/**
 * Created by Steve SeongUg Jung on 15. 1. 6..
 */
public class EntityComparator implements Comparator<FormattedEntity> {

    @Override
    public int compare(FormattedEntity lhs, FormattedEntity rhs) {

        return lhs.getName().compareToIgnoreCase(rhs.getName());
    }
}
