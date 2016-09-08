package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.model;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import setup.BaseInitUtil;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

/**
 * Created by tee on 15. 12. 22..
 */
public class StarMentionListModelTest {

    public StarredListModel starredListModel;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {
//        starredListModel = new StarredListModel();
//        starredListModel.initObject();
    }

//    @Test
//    public void testGetRawDatas() throws RetrofitException {
//        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);
//
//        //given
//        StarredListModel spyStarredListModel = spy(starredListModel);
//        ResStarMentioned mockResStarMentioned = mock(ResStarMentioned.class);
//        when(spyStarredListModel.getMentionRawDatas(anyLong(), anyInt())).thenReturn(mockResStarMentioned);
//        when(spyStarredListModel.getStarredRawDatas(anyString(), anyLong(), anyInt())).thenReturn(mockResStarMentioned);
//        spyStarredListModel.isFirstDatas = true;
//
//        //when
//        spyStarredListModel.getRawDatas(StarredListActivity.TYPE_MENTION_LIST, 10);
//        spyStarredListModel.getRawDatas(StarredListActivity.TYPE_STAR_LIST, 10);
//
//        //given
//        spyStarredListModel.isFirstDatas = false;
//
//        //when
//        spyStarredListModel.getRawDatas(StarredListActivity.TYPE_MENTION_LIST, 10);
//        spyStarredListModel.getRawDatas(StarredListActivity.TYPE_STAR_LIST, 10);
//
//        //then
//        verify(spyStarredListModel, times(3)).getMentionRawDatas(argumentCaptor.capture(), anyInt());
//        verify(spyStarredListModel, times(3)).getStarredRawDatas(anyString(), argumentCaptor.capture(), anyInt());
//
//        List<Long> argumentValues = argumentCaptor.getAllValues();
//        assertThat(argumentValues.size(), is(equalTo(6)));
//    }
//
//    @Test
//    public void testGetMetionRawDatas() {
//        try {
//            ResStarMentioned resStarMentioned = starredListModel.getMentionRawDatas(-1, 10);
//            assertNotNull(resStarMentioned);
//        } catch (RetrofitException e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void testGetStarredRawDatas() {
//        try {
//            ResStarMentioned resStarMentioned1 = starredListModel.getStarredRawDatas(StarredListActivity.TYPE_STAR_LIST_OF_ALL, -1, 10);
//            ResStarMentioned resStarMentioned2 = starredListModel.getStarredRawDatas(StarredListActivity.TYPE_STAR_LIST_OF_FILES, -1, 10);
//            assertNotNull(resStarMentioned1);
//            assertNotNull(resStarMentioned2);
//        } catch (RetrofitException e) {
//            fail();
//        }
//    }
//
//    @Test
//    public void testMakeStarMentionList() {
//        try {
//            ResStarMentioned resStarMentioned = starredListModel.getMentionRawDatas(-1, 10);
//
//            List<StarMentionVO> starMentionVOList1 = starredListModel
//                    .makeStarMentionList(StarredListActivity.TYPE_MENTION_LIST, resStarMentioned.getRecords());
//            Assert.assertNotNull(starMentionVOList1);
//
//            resStarMentioned = starredListModel.getStarredRawDatas(StarredListActivity.TYPE_STAR_LIST_OF_ALL, -1, 10);
//            List<StarMentionVO> starMentionVOList2 = starredListModel
//                    .makeStarMentionList(StarredListActivity.TYPE_STAR_LIST_OF_ALL, resStarMentioned.getRecords());
//            Assert.assertNotNull(starMentionVOList2);
//
//            resStarMentioned = starredListModel.getStarredRawDatas(StarredListActivity.TYPE_STAR_LIST_OF_FILES, -1, 10);
//            List<StarMentionVO> starMentionVOList3 = starredListModel
//                    .makeStarMentionList(StarredListActivity.TYPE_STAR_LIST_OF_FILES, resStarMentioned.getRecords());
//            Assert.assertNotNull(starMentionVOList3);
//        } catch (RetrofitException e) {
//            fail();
//        }
//    }

}
