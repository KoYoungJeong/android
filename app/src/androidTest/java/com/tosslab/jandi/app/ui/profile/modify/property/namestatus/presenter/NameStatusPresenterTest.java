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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

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
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(AndroidJUnit4.class)
public class NameStatusPresenterTest {

    @Mock
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

        try {
            initMocks(this);
        } catch (Exception e) {
        }

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
        presenter.updateName(user.getName());

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
        presenter.updateStatus(user.getStatusMessage());

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
        }).when(mockView).setUser(any());
        presenter.onInitUserInfo();

        await().until(() -> o[0] != null);

        User user = (User) o[0];
        assertThat(user).isEqualTo(TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId()));

    }

    @Component(modules = {NameStatusModule.class, ApiClientModule.class})
    interface TestComponent {
        void inject(NameStatusPresenterTest test);
    }
}