package com.tosslab.jandi.app.local.orm.repositories;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.JandiRobolectricGradleTestRunner;

import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 15. 9. 16..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class BadgeCountRepositoryTest {

    @Before
    public void setUp() throws Exception {
        BadgeCountRepository.getRepository().upsertBadgeCount(1, 28);
        BadgeCountRepository.getRepository().upsertBadgeCount(2, 1);
        BadgeCountRepository.getRepository().upsertBadgeCount(3, 152);
        BadgeCountRepository.getRepository().upsertBadgeCount(1, 0);
    }

    @Test
    public void testUpsertBadgeCount() throws Exception {
        BadgeCountRepository.getRepository().upsertBadgeCount(4, 500);
        int badgeCount = BadgeCountRepository.getRepository().findBadgeCountByTeamId(4);

        System.out.println("badgeCount = " + badgeCount);

        assertTrue(badgeCount == 500);
    }

    @Test
    public void testFindBadgeCountByTeamId() throws Exception {
        int badgeCount = BadgeCountRepository.getRepository().findBadgeCountByTeamId(1);

        System.out.println("badgeCount = " + badgeCount);

        assertTrue(badgeCount == 0);
    }

    @Test
    public void testGetTotalBadgeCount() throws Exception {
        int totalBadgeCount = BadgeCountRepository.getRepository().getTotalBadgeCount();

        System.out.println("totalBadgeCount = " + totalBadgeCount);

        assertTrue(totalBadgeCount == (1 + 152));
    }

    @Test
    public void testDeleteAll() throws Exception {
        BadgeCountRepository.getRepository().deleteAll();

        int totalBadgeCount = BadgeCountRepository.getRepository().getTotalBadgeCount();

        System.out.println("totalBadgeCount = " + totalBadgeCount);

        assertTrue(totalBadgeCount <= 0);
    }
}