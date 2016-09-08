package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.presenter;

import android.util.Pair;

import com.tosslab.jandi.app.local.orm.domain.MemberRecentKeyword;
import com.tosslab.jandi.app.local.orm.repositories.search.MemberRecentKeywordRepository;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.adapter.DeptJobDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.dept.model.DeptJobModel;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;

import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class DeptJobPresenterImplTest {

    private DeptJobPresenterImpl presenter;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        presenter = new DeptJobPresenterImpl(mock(DeptJobPresenter.View.class),
                mock(DeptJobDataModel.class),
                new DeptJobModel());

        presenter.onCreate();
    }

    @Test
    public void onCreate() throws Exception {

        assertThat(presenter.subscription).isNotNull();
        assertThat(presenter.subscription.hasSubscriptions()).isTrue();
        assertThat(presenter.deptJobSubject).isNotNull();
        assertThat(presenter.deptJobSubject.getValue()).hasSize(0);
    }

    @Test
    public void addDatas_empty() throws Exception {
        presenter.addDatas(new ArrayList<>());

        verify(presenter.deptJobDataModel).clear();
        verify(presenter.view).showEmptyView(anyString());
        verify(presenter.view).refreshDataView();

    }

    @Test
    public void addDatas_more_than_one() throws Exception {
        presenter.addDatas(Arrays.asList(Pair.create("1", "1")));

        verify(presenter.deptJobDataModel).clear();
        verify(presenter.view).dismissEmptyView();
        verify(presenter.deptJobDataModel).addAll(anyList());
        verify(presenter.view).refreshDataView();

    }

    @Test
    public void onDestroy() throws Exception {
        presenter.onDestroy();
        assertThat(presenter.deptJobSubject.hasCompleted()).isTrue();
        assertThat(presenter.subscription.isUnsubscribed()).isTrue();
    }

    @Test
    public void onSearchKeyword() throws Exception {
        presenter.onSearchKeyword("1");

        assertThat(presenter.deptJobSubject.getValue()).isEqualToIgnoringCase("1");
    }

    @Test
    public void onPickUser() throws Exception {
        long userId = TeamInfoLoader.getInstance().getJandiBot().getId();
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long roomId = TeamInfoLoader.getInstance().getChatId(userId);

        presenter.onPickUser(userId);
        verify(presenter.view).moveDirectMessage(eq(teamId), eq(userId), eq(roomId), anyLong());
    }

    @Test
    public void onItemClick() throws Exception {
        presenter.deptJobSubject.onNext("1");
        presenter.onItemClick(0);
        assertThat(MemberRecentKeywordRepository.getInstance().getKeywords())
                .extracting(MemberRecentKeyword::getKeyword)
                .contains("1");
    }


}