package com.tosslab.jandi.app.ui.starred.presentor;

import com.tosslab.jandi.app.ui.starred.StarredListActivity;
import com.tosslab.jandi.app.ui.starred.model.StarredListModel;
import com.tosslab.jandi.app.ui.starred.vo.StarMentionVO;

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

    StarredListPresenterImpl starMentionListPresentor;
    StarredListPresenterImpl.View mockView;
    StarredListModel mockStarredListModel;

//    @Before
//    public void setUp() throws Exception {
//        starMentionListPresentor = new StarredListPresenterImpl();
//        mockView = mock(StarredListPresenterImpl.View.class);
//        starMentionListPresentor.setView(mockView);
//        mockStarredListModel = mock(StarredListModel.class);
//    }
//
//    @Test
//    public void testAddStarMentionMessagesToList() throws Exception {
//
//        {
//            when(mockStarredListModel.isFirst()).thenReturn(true);
//            starMentionListPresentor.starredListModel = mockStarredListModel;
//            starMentionListPresentor.addStarMentionMessagesToList(StarredListActivity.TYPE_MENTION_LIST);
//            verify(mockView, never()).onShowMoreProgressBar();
//            verify(mockView).onDismissMoreProgressBar();
//        }
//
//        {
//            when(mockStarredListModel.isFirst()).thenReturn(false);
//            starMentionListPresentor.starredListModel = mockStarredListModel;
//            starMentionListPresentor.addStarMentionMessagesToList(StarredListActivity.TYPE_MENTION_LIST);
//            verify(mockView).onShowMoreProgressBar();
//            verify(mockView, times(2)).onDismissMoreProgressBar();
//        }
//
//        {
//            when(mockStarredListModel.hasMore()).thenReturn(true);
//            starMentionListPresentor.starredListModel = mockStarredListModel;
//            starMentionListPresentor.addStarMentionMessagesToList(StarredListActivity.TYPE_MENTION_LIST);
//            verify(mockView).onSetReadyMoreState();
//        }
//
//        {
//            when(mockStarredListModel.hasMore()).thenReturn(false);
//            starMentionListPresentor.starredListModel = mockStarredListModel;
//            starMentionListPresentor.addStarMentionMessagesToList(StarredListActivity.TYPE_MENTION_LIST);
//            verify(mockView, times(3)).onSetNoMoreState();
//            verify(mockView, times(4)).onAddAndShowList(anyList());
//        }
//
//    }
//
//    @Test
//    public void testEecuteLongClickEvent() throws Exception {
//        StarMentionVO starMentionVO = mock(StarMentionVO.class);
//        starMentionListPresentor.executeLongClickEvent(starMentionVO, 1);
//        verify(mockView).onShowDialog(anyInt(), anyInt(), anyInt());
//    }
//
//    @Test
//    public void testUnregistStarredMessage() throws Exception {
//        starMentionListPresentor.starredListModel = mockStarredListModel;
//        starMentionListPresentor.unregistStarredMessage(1, 1, 1);
//        verify(mockStarredListModel).unregistStarredMessage(anyInt(), anyInt());
//        verify(mockView).showSuccessToast(anyString());
//        verify(mockView).onRemoveItem(anyInt());
//    }
//
//    @Test
//    public void testRefreshList() throws Exception {
//        StarredListPresenterImpl starMentionListPresentor1 = spy(starMentionListPresentor);
//        doNothing().when(starMentionListPresentor1).addStarMentionMessagesToList(anyString());
//        starMentionListPresentor1.starredListModel = mockStarredListModel;
//        starMentionListPresentor1.refreshList("");
//        verify(starMentionListPresentor1).addStarMentionMessagesToList(anyString());
//    }
//
//    @Test
//    public void testReloadList() throws Exception {
//        StarredListPresenterImpl starMentionListPresentor1 = spy(starMentionListPresentor);
//        doNothing().when(starMentionListPresentor1).addStarMentionMessagesToList(anyString(), anyInt());
//        starMentionListPresentor1.starredListModel = mockStarredListModel;
//        starMentionListPresentor1.reloadStartList("", 10);
//        verify(starMentionListPresentor1).addStarMentionMessagesToList(anyString(), anyInt());
//    }

}