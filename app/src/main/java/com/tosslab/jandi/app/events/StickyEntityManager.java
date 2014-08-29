package com.tosslab.jandi.app.events;

import com.tosslab.jandi.app.lists.entities.EntityManager;

/**
 * Created by justinygchoi on 2014. 8. 13..
 */
public class StickyEntityManager {
    public EntityManager entityManager;
    public StickyEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
}
