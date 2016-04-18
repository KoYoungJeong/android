package com.tosslab.jandi.app.ui.starmention.presentor;

import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.model.StarMentionListModel;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tee on 15. 12. 23..
 */
public class StarMentionListPresentorTest {

    StarMentionListPresentor starMentionListPresentor;
    StarMentionListPresentor.View mockView;
    StarMentionListModel mockStarMentionListModel;

    @Before
    public void setUp() throws Exception {
        starMentionListPresentor = new StarMentionListPresentor();
        mockView = mock(StarMentionListPresentor.View.class);
        starMentionListPresentor.setView(mockView);
        mockStarMentionListModel = mock(StarMentionListModel.class);
    }

    @Test
    public void testAddStarMentionMessagesToList() throws Exception {

        {
            when(mockStarMentionListModel.isFirst()).thenReturn(true);
            starMentionListPresentor.starMentionListModel = mockStarMentionListModel;
            starMentionListPresentor.addStarMentionMessagesToList(StarMentionListActivity.TYPE_MENTION_LIST);
            verify(mockView, never()).onShowMoreProgressBar();
            verify(mockView).onDismissMoreProgressBar();
        }

        {
            when(mockStarMentionListModel.isFirst()).thenReturn(false);
            starMentionListPresentor.starMentionListModel = mockStarMentionListModel;
            starMentionListPresentor.addStarMentionMessagesToList(StarMentionListActivity.TYPE_MENTION_LIST);
            verify(mockView).onShowMoreProgressBar();
            verify(mockView, times(2)).onDismissMoreProgressBar();
        }

        {
            when(mockStarMentionListModel.hasMore()).thenReturn(true);
            starMentionListPresentor.starMentionListModel = mockStarMentionListModel;
            starMentionListPresentor.addStarMentionMessagesToList(StarMentionListActivity.TYPE_MENTION_LIST);
            verify(mockView).onSetReadyMoreState();
        }

        {
            when(mockStarMentionListModel.hasMore()).thenReturn(false);
            starMentionListPresentor.starMentionListModel = mockStarMentionListModel;
            starMentionListPresentor.addStarMentionMessagesToList(StarMentionListActivity.TYPE_MENTION_LIST);
            verify(mockView, times(3)).onSetNoMoreState();
            verify(mockView, times(4)).onAddAndShowList(anyList());
        }

    }

    @Test
    public void testEecuteLongClickEvent() throws Exception {
        StarMentionVO starMentionVO = mock(StarMentionVO.class);
        starMentionListPresentor.executeLongClickEvent(starMentionVO, 1);
        verify(mockView).onShowDialog(anyInt(), anyInt(), anyInt());
    }

    @Test
    public void testUnregistStarredMessage() throws Exception {
        starMentionListPresentor.starMentionListModel = mockStarMentionListModel;
        starMentionListPresentor.unregistStarredMessage(1, 1, 1);
        verify(mockStarMentionListModel).unregistStarredMessage(anyInt(), anyInt());
        verify(mockView).showSuccessToast(anyString());
        verify(mockView).onRemoveItem(anyInt());
    }

    @Test
    public void testRefreshList() throws Exception {
        StarMentionListPresentor starMentionListPresentor1 = spy(starMentionListPresentor);
        doNothing().when(starMentionListPresentor1).addStarMentionMessagesToList(anyString());
        starMentionListPresentor1.starMentionListModel = mockStarMentionListModel;
        starMentionListPresentor1.refreshList("");
        verify(starMentionListPresentor1).addStarMentionMessagesToList(anyString());
    }

    @Test
    public void testReloadList() throws Exception {
        StarMentionListPresentor starMentionListPresentor1 = spy(starMentionListPresentor);
        doNothing().when(starMentionListPresentor1).addStarMentionMessagesToList(anyString(), anyInt());
        starMentionListPresentor1.starMentionListModel = mockStarMentionListModel;
        starMentionListPresentor1.reloadStartList("", 10);
        verify(starMentionListPresentor1).addStarMentionMessagesToList(anyString(), anyInt());
    }

}