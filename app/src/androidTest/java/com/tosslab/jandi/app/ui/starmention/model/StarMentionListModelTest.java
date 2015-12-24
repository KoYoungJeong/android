package com.tosslab.jandi.app.ui.starmention.model;

import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import retrofit.RetrofitError;

import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tee on 15. 12. 22..
 */
public class StarMentionListModelTest {

    public StarMentionListModel starMentionListModel;

    @Before
    public void setUp() throws Exception {
        starMentionListModel = new StarMentionListModel();
    }

    @Test
    public void testGetRawDatas() {
        ArgumentCaptor<Integer> argumentCaptor = ArgumentCaptor.forClass(Integer.class);

        //given
        StarMentionListModel spyStarMentionListModel = spy(starMentionListModel);
        ResStarMentioned mockResStarMentioned = mock(ResStarMentioned.class);
        when(spyStarMentionListModel.getMentionRawDatas(anyInt(), anyInt())).thenReturn(mockResStarMentioned);
        when(spyStarMentionListModel.getStarredRawDatas(anyString(), anyInt(), anyInt())).thenReturn(mockResStarMentioned);
        spyStarMentionListModel.isFirstDatas = true;

        //when
        spyStarMentionListModel.getRawDatas(StarMentionListActivity.TYPE_MENTION_LIST, 10);
        spyStarMentionListModel.getRawDatas(StarMentionListActivity.TYPE_STAR_LIST, 10);

        //given
        spyStarMentionListModel.isFirstDatas = false;

        //when
        spyStarMentionListModel.getRawDatas(StarMentionListActivity.TYPE_MENTION_LIST, 10);
        spyStarMentionListModel.getRawDatas(StarMentionListActivity.TYPE_STAR_LIST, 10);

        //then
        verify(spyStarMentionListModel, times(3)).getMentionRawDatas(argumentCaptor.capture(), anyInt());
        verify(spyStarMentionListModel, times(3)).getStarredRawDatas(anyString(), argumentCaptor.capture(), anyInt());

        List<Integer> argumentValues = argumentCaptor.getAllValues();
        assertTrue(argumentValues.get(1) == null);
        assertTrue(argumentValues.get(2) != null);
        assertTrue(argumentValues.get(4) == null);
        assertTrue(argumentValues.get(5) != null);
    }

    @Test
    public void testGetMetionRawDatas() {
        try {
            ResStarMentioned resStarMentioned = starMentionListModel.getMentionRawDatas(null, 10);
            assertNotNull(resStarMentioned);
        } catch (RetrofitError e) {
            fail();
        }
    }

    @Test
    public void testGetStarredRawDatas() {
        try {
            ResStarMentioned resStarMentioned1 = starMentionListModel.getStarredRawDatas(StarMentionListActivity.TYPE_STAR_LIST_OF_ALL, null, 10);
            ResStarMentioned resStarMentioned2 = starMentionListModel.getStarredRawDatas(StarMentionListActivity.TYPE_STAR_LIST_OF_FILES, null, 10);
            assertNotNull(resStarMentioned1);
            assertNotNull(resStarMentioned2);
        } catch (RetrofitError e) {
            fail();
        }
    }

    @Test
    public void testMakeStarMentionList() {
        try {
            ResStarMentioned resStarMentioned = starMentionListModel.getMentionRawDatas(null, 10);

            List<StarMentionVO> starMentionVOList1 = starMentionListModel
                    .makeStarMentionList(StarMentionListActivity.TYPE_MENTION_LIST, resStarMentioned.getRecords());
            Assert.assertNotNull(starMentionVOList1);

            resStarMentioned = starMentionListModel.getStarredRawDatas(StarMentionListActivity.TYPE_STAR_LIST_OF_ALL, null, 10);
            List<StarMentionVO> starMentionVOList2 = starMentionListModel
                    .makeStarMentionList(StarMentionListActivity.TYPE_STAR_LIST_OF_ALL, resStarMentioned.getRecords());
            Assert.assertNotNull(starMentionVOList2);

            resStarMentioned = starMentionListModel.getStarredRawDatas(StarMentionListActivity.TYPE_STAR_LIST_OF_FILES, null, 10);
            List<StarMentionVO> starMentionVOList3 = starMentionListModel
                    .makeStarMentionList(StarMentionListActivity.TYPE_STAR_LIST_OF_FILES, resStarMentioned.getRecords());
            Assert.assertNotNull(starMentionVOList3);
        } catch (RetrofitError e) {
            fail();
        }
    }

}
