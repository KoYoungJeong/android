package com.tosslab.jandi.app.ui.maintab.tabs.team.filter.deptgroup.presenter;

import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.TeamMemberDataModel;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.adapter.ToggleCollector;
import com.tosslab.jandi.app.ui.maintab.tabs.team.filter.member.domain.TeamMemberItem;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;

import setup.BaseInitUtil;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@org.junit.runner.RunWith(android.support.test.runner.AndroidJUnit4.class)
public class DeptJobGroupPresenterImplTest {
    private DeptJobGroupPresenterImpl presenter;
    private TeamMemberDataModel teamMemberDataModel;
    private ToggleCollector toggledUser;
    private DeptJobGroupPresenter.View view;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        teamMemberDataModel = mock(TeamMemberDataModel.class);
        toggledUser = mock(ToggleCollector.class);
        view = mock(DeptJobGroupPresenter.View.class);
        presenter = new DeptJobGroupPresenterImpl(view, teamMemberDataModel, toggledUser);

    }

    @Test
    public void onMemberClick_pick() throws Exception {
        doReturn(new TeamMemberItem(TeamInfoLoader.getInstance().getJandiBot(), "", myId))
                .when(teamMemberDataModel)
                .getItem(eq(0));
        presenter.setPickMode(true);
        presenter.onMemberClick(0);

        verify(view).pickUser(eq(TeamInfoLoader.getInstance().getJandiBot().getId()));
    }

    @Test
    public void onMemberClick_multi_not_checked() throws Exception {
        doReturn(new TeamMemberItem(TeamInfoLoader.getInstance().getJandiBot(), "", myId))
                .when(teamMemberDataModel)
                .getItem(eq(0));
        doReturn(false).when(toggledUser).containsId(eq(TeamInfoLoader.getInstance().getJandiBot().getId()));

        presenter.setSelectMode(true);


        presenter.onMemberClick(0);

        verify(toggledUser).containsId(eq(TeamInfoLoader.getInstance().getJandiBot().getId()));
        verify(toggledUser).addId(eq(TeamInfoLoader.getInstance().getJandiBot().getId()));
        verify(view).refreshDataView();
        verify(view).updateToggledUser(anyInt());
    }

    @Test
    public void onMemberClick_multi_checked() throws Exception {
        doReturn(new TeamMemberItem(TeamInfoLoader.getInstance().getJandiBot(), "", myId))
                .when(teamMemberDataModel)
                .getItem(eq(0));
        doReturn(true).when(toggledUser).containsId(eq(TeamInfoLoader.getInstance().getJandiBot().getId()));

        presenter.setSelectMode(true);


        presenter.onMemberClick(0);

        verify(toggledUser).containsId(eq(TeamInfoLoader.getInstance().getJandiBot().getId()));
        verify(toggledUser).removeId(eq(TeamInfoLoader.getInstance().getJandiBot().getId()));
        verify(view).refreshDataView();
        verify(view).updateToggledUser(anyInt());
    }

    @Test
    public void onUnselectClick() throws Exception {
        presenter.onUnselectClick();
        verify(toggledUser).clearIds();
        verify(view).updateToggledUser(anyInt());
        verify(view).refreshDataView();
    }

    @Test
    public void onAddClick() throws Exception {
        doReturn(Arrays.asList(TeamInfoLoader.getInstance().getJandiBot().getId())).when(toggledUser).getIds();
        doReturn(1).when(toggledUser).count();
        presenter.onAddClick();
        verify(view).comeWithResult(any());
    }

}