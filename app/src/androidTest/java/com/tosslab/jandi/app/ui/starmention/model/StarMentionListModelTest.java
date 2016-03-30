package com.tosslab.jandi.app.ui.starmention.model;

import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResStarMentioned;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import setup.BaseInitUtil;

import static junit.framework.Assert.fail;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyLong;
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

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        starMentionListModel = new StarMentionListModel();
    }

    @Test
    public void testGetRawDatas() throws RetrofitException {
        ArgumentCaptor<Long> argumentCaptor = ArgumentCaptor.forClass(Long.class);

        //given
        StarMentionListModel spyStarMentionListModel = spy(starMentionListModel);
        ResStarMentioned mockResStarMentioned = mock(ResStarMentioned.class);
        when(spyStarMentionListModel.getMentionRawDatas(anyLong(), anyInt())).thenReturn(mockResStarMentioned);
        when(spyStarMentionListModel.getStarredRawDatas(anyString(), anyLong(), anyInt())).thenReturn(mockResStarMentioned);
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

        List<Long> argumentValues = argumentCaptor.getAllValues();
        assertThat(argumentValues.size(), is(equalTo(6)));
    }

    @Test
    public void testGetMetionRawDatas() {
        try {
            ResStarMentioned resStarMentioned = starMentionListModel.getMentionRawDatas(-1, 10);
            assertNotNull(resStarMentioned);
        } catch (RetrofitException e) {
            fail();
        }
    }

    @Test
    public void testGetStarredRawDatas() {
        try {
            ResStarMentioned resStarMentioned1 = starMentionListModel.getStarredRawDatas(StarMentionListActivity.TYPE_STAR_LIST_OF_ALL, -1, 10);
            ResStarMentioned resStarMentioned2 = starMentionListModel.getStarredRawDatas(StarMentionListActivity.TYPE_STAR_LIST_OF_FILES, -1, 10);
            assertNotNull(resStarMentioned1);
            assertNotNull(resStarMentioned2);
        } catch (RetrofitException e) {
            fail();
        }
    }

    @Test
    public void testMakeStarMentionList() {
        try {
            ResStarMentioned resStarMentioned = starMentionListModel.getMentionRawDatas(-1, 10);

            List<StarMentionVO> starMentionVOList1 = starMentionListModel
                    .makeStarMentionList(StarMentionListActivity.TYPE_MENTION_LIST, resStarMentioned.getRecords());
            Assert.assertNotNull(starMentionVOList1);

            resStarMentioned = starMentionListModel.getStarredRawDatas(StarMentionListActivity.TYPE_STAR_LIST_OF_ALL, -1, 10);
            List<StarMentionVO> starMentionVOList2 = starMentionListModel
                    .makeStarMentionList(StarMentionListActivity.TYPE_STAR_LIST_OF_ALL, resStarMentioned.getRecords());
            Assert.assertNotNull(starMentionVOList2);

            resStarMentioned = starMentionListModel.getStarredRawDatas(StarMentionListActivity.TYPE_STAR_LIST_OF_FILES, -1, 10);
            List<StarMentionVO> starMentionVOList3 = starMentionListModel
                    .makeStarMentionList(StarMentionListActivity.TYPE_STAR_LIST_OF_FILES, resStarMentioned.getRecords());
            Assert.assertNotNull(starMentionVOList3);
        } catch (RetrofitException e) {
            fail();
        }
    }

}
