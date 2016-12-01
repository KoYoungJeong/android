package com.tosslab.jandi.app.local.orm.repositories.socket;

import android.support.annotation.NonNull;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.domain.SocketEvent;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class SocketEventRepositoryTest {
    private Dao<SocketEvent, String> dao;

    @Before
    public void setUp() throws Exception {
        OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        dao = helper.getDao(SocketEvent.class);
        dao.deleteBuilder().delete();
    }

    @Test
    public void deleteBeforeTime() throws Exception {
        SocketEventRepository.getInstance().addEvent(createMockEventHistory(1));
        SocketEventRepository.getInstance().addEvent(createMockEventHistory(2));
        SocketEventRepository.getInstance().addEvent(createMockEventHistory(3));

        assertThat(SocketEventRepository.getInstance().deleteBeforeTime(1, 3)).isTrue();
        assertThat(SocketEventRepository.getInstance().hasEvent(createMockEventHistory(1))).isFalse();
        assertThat(SocketEventRepository.getInstance().hasEvent(createMockEventHistory(2))).isFalse();
        assertThat(SocketEventRepository.getInstance().hasEvent(createMockEventHistory(3))).isTrue();
    }

    protected EventHistoryInfo createMockEventHistory(long time) {
        EventHistoryInfo event = mock(EventHistoryInfo.class);
        doReturn(1L).when(event).getTeamId();
        doReturn("event").when(event).getEvent();
        doReturn(String.valueOf(time)).when(event).getUnique();
        doReturn(time).when(event).getTs();
        return event;
    }

    @Test
    public void addEvent() throws Exception {
        EventHistoryInfo event = getEventHistoryInfo();

        SocketEventRepository.getInstance().addEvent(event);
        SocketEvent socketEvent = dao.queryForId("test-unique");

        assertThat(socketEvent).isNotNull();
        assertThat(socketEvent.getEvent()).isEqualTo("test");
        assertThat(socketEvent.getTeamId()).isEqualTo(1L);
        assertThat(socketEvent.getTs()).isEqualTo(event.getTs());
    }

    @Test
    public void hasEvent() throws Exception {
        EventHistoryInfo eventHistoryInfo = getEventHistoryInfo();

        {
            boolean hasEvent = SocketEventRepository.getInstance().hasEvent(eventHistoryInfo);
            assertThat(hasEvent).isFalse();
        }

        {
            SocketEventRepository.getInstance().addEvent(eventHistoryInfo);
            boolean hasEvent = SocketEventRepository.getInstance().hasEvent(eventHistoryInfo);
            assertThat(hasEvent).isTrue();
        }

    }

    @NonNull
    protected EventHistoryInfo getEventHistoryInfo() {
        EventHistoryInfo event = mock(EventHistoryInfo.class);
        when(event.getEvent()).thenReturn("test");
        when(event.getTs()).thenReturn(System.currentTimeMillis());
        when(event.getUnique()).thenReturn("test-unique");
        when(event.getTeamId()).thenReturn(1L);
        return event;
    }


}