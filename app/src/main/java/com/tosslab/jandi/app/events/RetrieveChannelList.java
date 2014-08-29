package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.lists.entities.EntityManager;

/**
 * Created by justinygchoi on 2014. 8. 11..
 */
public class RetrieveChannelList {
    public EntityManager entityManager;

    public RetrieveChannelList(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
