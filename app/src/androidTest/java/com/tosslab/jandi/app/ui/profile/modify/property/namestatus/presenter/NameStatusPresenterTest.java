package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.presenter;

import android.support.test.runner.AndroidJUnit4;
import android.util.Pair;

import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.models.start.Human;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.dagger.NameStatusModule;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;

import javax.inject.Inject;

import dagger.Component;
import rx.Observer;
import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class NameStatusPresenterTest {

    NameStatusPresenter.View mockView;

    @Inject
    NameStatusPresenter presenter;
    private boolean finish;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {

        mockView = Mockito.mock(NameStatusPresenter.View.class);

        DaggerNameStatusPresenterTest_TestComponent.builder()
                .nameStatusModule(new NameStatusModule(mockView))
                .build()
                .inject(this);

        finish = false;

    }

    @Test
    public void onDestroy() throws Exception {
        presenter.onDestroy();

        assertThat(presenter.nameSubject.hasCompleted()).isTrue();
        assertThat(presenter.subscription.isUnsubscribed()).isTrue();
    }

    @Ignore
    @Test
    public void onTextChange() throws Exception {

        doAnswer(mock -> {
            finish = true;
            return mock;
        }).when(mockView).setTextCount(anyInt());

        presenter.onTextChange("ha");

        await().until(() -> finish);

        verify(mockView).setTextCount(eq(2));
    }

    @Test
    public void updateName() throws Exception {

        doAnswer(mock -> {
            finish = true;
            return mock;
        }).when(mockView).successUpdate();

        User user = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
        presenter.updateName(user.getName(), TeamInfoLoader.getInstance().getMyId());

        await().until(() -> finish);

        verify(mockView).successUpdate();
        verify(mockView, never()).dismissProgress();
    }

    @Test
    public void updateStatus() throws Exception {
        doAnswer(mock -> {
            finish = true;
            return mock;
        }).when(mockView).successUpdate();

        User user = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
        presenter.updateStatus(user.getStatusMessage(), TeamInfoLoader.getInstance().getMyId());

        await().until(() -> finish);

        verify(mockView).successUpdate();
        verify(mockView, never()).dismissProgress();
    }

    @Test
    public void updateFinish() throws Exception {
        Observer<Pair<Boolean, Human>> pairObserver = presenter.updateFinish();

        assertThat(pairObserver).isNotNull();

        pairObserver.onError(new Exception());
        verify(mockView).dismissProgress();

        pairObserver.onNext(Pair.create(false, null));
        verify(mockView).successUpdate();
    }

    @Test
    public void onInitUserInfo() throws Exception {

        final Object[] o = new Object[1];
        doAnswer(mock -> {
            o[0] = mock.getArguments()[0];
            return mock;
        }).when(mockView).setContent(any());
        presenter.onInitUserInfo(TeamInfoLoader.getInstance().getMyId());

        await().until(() -> o[0] != null);

        String user = o[0].toString();
        assertThat(user).isEqualTo(TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId()).getStatusMessage());

    }

    @Component(modules = {NameStatusModule.class, ApiClientModule.class})
    interface TestComponent {
        void inject(NameStatusPresenterTest test);
    }
}