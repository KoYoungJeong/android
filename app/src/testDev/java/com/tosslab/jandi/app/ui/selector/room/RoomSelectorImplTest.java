package com.tosslab.jandi.app.ui.selector.room;

import org.junit.runner.RunWith;
import org.robolectric.JandiRobolectricGradleTestRunner;

/**
 * Created by jsuch2362 on 15. 8. 17..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class RoomSelectorImplTest {

    //로직 변경..

//    private RoomSelectorImpl selectorImpl;
//
//    @Before
//    public void setUp() throws Exception {
//        BaseInitUtil.initData(RuntimeEnvironment.application);
//        int teamId = AccountRepository
//                .getRepository().getSelectedTeamId();
//        ResLeftSideMenu leftSideMenu = RequestApiManager.getInstance().getInfosForSideMenuByMainRest(teamId);
//        LeftSideMenuRepository.getRepository().upsertLeftSideMenu(leftSideMenu);
//
//        selectorImpl = new RoomSelectorImpl();
//    }
//
//    @After
//    public void tearDown() throws Exception {
//        BaseInitUtil.releaseDatabase();
//    }
//
//    @Test
//    public void testGetUsers() throws Exception {
//
//        List<FormattedEntity> entities = new ArrayList<>();
//        selectorImpl.getUsers().subscribe(entities::addAll);
//
//        assertThat(entities.size(), is(greaterThan(0)));
//        assertThat(entities.get(0).type, is(equalTo(FormattedEntity.TYPE_EVERYWHERE)));
//
//        Observable.from(entities.subList(1, entities.size()))
//                .subscribe(formattedEntity -> {
//                    if (!formattedEntity.isUser()) {
//                        fail("It must be User");
//                    }
//                });
//
//
//    }
//
//    @Test
//    public void testGetTopics() throws Exception {
//        List<FormattedEntity> entities = new ArrayList<>();
//        selectorImpl.getTopics().subscribe(entities::addAll);
//
//        assertThat(entities.size(), is(greaterThan(0)));
//        assertThat(entities.get(0).type, is(equalTo(FormattedEntity.TYPE_EVERYWHERE)));
//
//        Observable.from(entities.subList(1, entities.size()))
//                .subscribe(formattedEntity -> {
//                    if (formattedEntity.isUser()) {
//                        fail("It must be User");
//                    }
//                });
//
//    }
}