package com.tosslab.jandi.app.services.socket.model;

import com.tosslab.jandi.app.network.client.events.EventsApi;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.models.EventHistoryInfo;
import com.tosslab.jandi.app.network.models.ResEventHistory;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.inject.Inject;

import dagger.Component;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class SocketEventHistoryUpdatorTest {

    @Inject
    SocketEventHistoryUpdator updator;

    @Inject
    EventsApi eventsApi;

    private boolean restart;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        DaggerSocketEventHistoryUpdatorTest_TestComponent.create().inject(this);
    }

    @Test
    public void checkEventHistory() throws Exception {
        {
            restart = false;
            updator.checkEventHistory(-1, () -> restart = true)
                    .subscribe();

            assertThat(restart).isTrue();
        }

        {
            restart = false;
            TestSubscriber<EventHistoryInfo> subscriber = TestSubscriber.create();
            long socketConnectedLastTime = System.currentTimeMillis() - (1000 * 60 * 60 * 24 * 3);
            ResEventHistory eventHistory = eventsApi.getEventHistory(socketConnectedLastTime, TeamInfoLoader.getInstance().getMyId());
            updator.checkEventHistory(socketConnectedLastTime,
                    () -> restart = true)
                    .subscribe(subscriber);

            if (eventHistory.getTotal() < 10000) {
                subscriber.assertNoErrors();
                subscriber.assertCompleted();
                assertThat(subscriber.getValueCount()).isLessThanOrEqualTo(eventHistory.getRecords().size());
                assertThat(restart).isFalse();
            } else {
                subscriber.assertValueCount(0);
                assertThat(restart).isTrue();
            }
        }

    }

    @Component(modules = ApiClientModule.class)
    interface TestComponent {
        void inject(SocketEventHistoryUpdatorTest test);
    }

}