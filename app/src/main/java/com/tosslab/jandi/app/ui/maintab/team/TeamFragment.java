package com.tosslab.jandi.app.ui.maintab.team;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestInviteMemberEvent;
import com.tosslab.jandi.app.events.TabClickEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.events.team.TeamLeaveEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.base.adapter.MultiItemRecyclerAdapter;
import com.tosslab.jandi.app.ui.maintab.MainTabPagerAdapter;
import com.tosslab.jandi.app.ui.maintab.team.adapter.TeamMemberListAdapter;
import com.tosslab.jandi.app.ui.maintab.team.component.DaggerTeamComponent;
import com.tosslab.jandi.app.ui.maintab.team.module.TeamModule;
import com.tosslab.jandi.app.ui.maintab.team.presenter.TeamPresenter;
import com.tosslab.jandi.app.ui.maintab.team.view.TeamView;
import com.tosslab.jandi.app.ui.maintab.team.vo.Team;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.KeyboardVisibleChangeDetectView;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tonyjs on 16. 3. 15..
 */
public class TeamFragment extends Fragment implements TeamView, UiUtils.KeyboardHandler {

    @Inject
    TeamPresenter presenter;

    InputMethodManager inputMethodManager;

    @Bind(R.id.lv_team)
    RecyclerView lvTeam;
    @Bind(R.id.v_team_keyboard_visible_change_detector)
    KeyboardVisibleChangeDetectView vgKeyboardVisibleChangeDetectView;
    @Bind(R.id.progress_team)
    ProgressBar pbTeam;
    @Bind(R.id.tv_team_info_name)
    TextView tvTeamName;
    @Bind(R.id.tv_team_info_domain)
    TextView tvTeamDomain;
    @Bind(R.id.tv_team_info_owner)
    TextView tvTeamOwner;
    @Bind(R.id.vg_team_info)
    View vgTeamInfo;
    @Bind(R.id.vg_team_member_search)
    View vgTeamMemberSearch;
    @Bind(R.id.et_team_member_search)
    EditText etSearch;

    private TeamMemberListAdapter adapter;
    private LinearLayoutManager layoutManager;
    private UiMode uiMode = UiMode.NORMAL;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        injectComponent();
    }

    private void injectComponent() {
        DaggerTeamComponent.builder()
                .teamModule(new TeamModule(this))
                .build()
                .inject(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_team, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        EventBus.getDefault().register(this);

        initTeamMemberListView();

        initKeyboardActions();

        presenter.onInitializeTeam();
    }

    private void initTeamMemberListView() {
        layoutManager = new LinearLayoutManager(getActivity().getBaseContext());
        lvTeam.setLayoutManager(layoutManager);
        lvTeam.setAdapter(adapter = new TeamMemberListAdapter());
        lvTeam.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (uiMode == UiMode.SEARCH) {
                    return;
                }

                float translationY = Math.min(0, vgTeamInfo.getTranslationY() - dy);
                vgTeamInfo.setTranslationY(translationY);
                if (vgTeamInfo.getTranslationY() <= -vgTeamInfo.getMeasuredHeight()) {
                    vgTeamMemberSearch.setTranslationY(-vgTeamInfo.getMeasuredHeight());
                } else {
                    float translateY = Math.min(0, vgTeamMemberSearch.getTranslationY() - dy);
                    vgTeamMemberSearch.setTranslationY(translateY);
                }
            }
        });

        adapter.setOnMemberClickListener(member -> {
            showUserProfile(member.getId());
        });
    }

    private void showUserProfile(long userId) {
        MemberProfileActivity_.intent(getActivity())
                .memberId(userId)
                .start();
    }

    private void initKeyboardActions() {
        inputMethodManager = JandiApplication.getService(Context.INPUT_METHOD_SERVICE);

        vgKeyboardVisibleChangeDetectView.setOnKeyboardVisibleChangeListener((isShow, height) -> {

            if (!isShow) {

                changeToNormalMode();

            } else {

                changeToSearchMode();

            }
        });
    }

    private void changeToNormalMode() {
        int teamInfoHeight = vgTeamInfo.getMeasuredHeight();
        int searchBarHeight = vgTeamMemberSearch.getMeasuredHeight();

        vgTeamInfo.setTranslationY(0);
        vgTeamMemberSearch.setTranslationY(0);

        lvTeam.setPadding(0, teamInfoHeight + searchBarHeight, 0, 0);
        lvTeam.invalidate();

        uiMode = UiMode.NORMAL;

        lvTeam.smoothScrollBy(0, -teamInfoHeight);
    }

    private void changeToSearchMode() {
        int teamInfoHeight = vgTeamInfo.getMeasuredHeight();
        int searchBarHeight = vgTeamMemberSearch.getMeasuredHeight();

        vgTeamInfo.setTranslationY(-teamInfoHeight);
        vgTeamMemberSearch.setTranslationY(-teamInfoHeight);

        lvTeam.setPadding(0, searchBarHeight, 0, 0);
        lvTeam.invalidate();

        uiMode = UiMode.SEARCH;
    }

    @OnFocusChange(R.id.et_team_member_search)
    void onFocusToSearch(boolean hasFocused) {
        if (hasFocused) {
            focusToSearch();
        }
    }

    @OnClick({R.id.vg_team_member_search, R.id.et_team_member_search})
    void focusToSearch() {
        etSearch.requestFocus();
        inputMethodManager.showSoftInput(etSearch, 0);
    }

    @OnTextChanged(R.id.et_team_member_search)
    void onSearchMember(CharSequence text) {
        presenter.onSearchMember(text.toString());
    }

    @OnClick(R.id.btn_team_info_invite)
    void inviteMember() {
        EventBus.getDefault().post(new RequestInviteMemberEvent());
    }

    @Override
    public void onDestroyView() {
        presenter.stopSearchQueue();
        EventBus.getDefault().unregister(this);
        ButterKnife.unbind(this);
        super.onDestroyView();
    }

    @Override
    public void showProgress() {
        pbTeam.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideProgress() {
        if(isFinishing()) {
            return;
        }
        pbTeam.setVisibility(View.GONE);
    }

    public void onEventMainThread(TabClickEvent event) {
        if (event.getIndex() == MainTabPagerAdapter.TAB_TEAM) {
            changeToNormalMode();
            lvTeam.scrollToPosition(0);
        }
    }

    public void onEvent(TeamLeaveEvent event) {
        presenter.reInitializeTeam();
    }

    public void onEvent(ProfileChangeEvent profileChangeEvent) {
        if(isFinishing()) {
            return;
        }
        // adapter index 를 알아내야 돼서...
        long id = profileChangeEvent.getMember().id;

        for (int i = 0; i < adapter.getItemCount(); i++) {
            if (!(adapter.getItem(i) instanceof FormattedEntity)) {
                continue;
            }

            FormattedEntity entity = adapter.getItem(i);
            if (entity.getId() == id) {
                final int index = i;
                FormattedEntity user = EntityManager.getInstance().getEntityById(id);
                Observable.just(user)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(newUser -> {
                            adapter.setRow(index, new MultiItemRecyclerAdapter.Row<>(
                                    newUser, TeamMemberListAdapter.VIEW_TYPE_MEMBER));
                            adapter.notifyItemChanged(index);
                        });
                break;
            }
        }
    }

    @Override
    public void initTeamInfo(Team team) {
        if(isFinishing()) {
            return;
        }
        tvTeamName.setText(team.getName());
        tvTeamDomain.setText(team.getDomain());
        String owner = JandiApplication.getContext()
                .getResources()
                .getString(R.string.jandi_team_owner_with_format, team.getOwner().name);
        tvTeamOwner.setText(owner);

        List<FormattedEntity> members = team.getMembers();
        if (members == null || members.isEmpty()) {
            return;
        }

        adapter.setRow(0, new MultiItemRecyclerAdapter.Row<>(
                members.size(), TeamMemberListAdapter.VIEW_TYPE_MEMBER_COUNT));

        Observable.from(members)
                .subscribe(entity -> {
                    adapter.addRow(new MultiItemRecyclerAdapter.Row<>(
                            entity, TeamMemberListAdapter.VIEW_TYPE_MEMBER));
                });

        adapter.notifyDataSetChanged();
    }

    @Override
    public void setSearchedMembers(String query, List<FormattedEntity> searchedMembers) {
        if(isFinishing()) {
            return;
        }
        adapter.clear();

        boolean isEmpty = searchedMembers == null || searchedMembers.isEmpty();
        if (isEmpty) {
            adapter.setRow(0, new MultiItemRecyclerAdapter.Row<>(
                    query, TeamMemberListAdapter.VIEW_TYPE_EMPTY_QUERY));
            adapter.notifyDataSetChanged();
            return;
        } else {
            int memberCount = searchedMembers.size();
            adapter.setRow(0, new MultiItemRecyclerAdapter.Row<>(
                    memberCount, TeamMemberListAdapter.VIEW_TYPE_MEMBER_COUNT));
        }

        Observable.from(searchedMembers)
                .subscribe(entity -> {
                    adapter.addRow(new MultiItemRecyclerAdapter.Row<>(
                            entity, TeamMemberListAdapter.VIEW_TYPE_MEMBER));
                });

        adapter.notifyDataSetChanged();
    }

    @Override
    public void showTeamLayout() {
        vgTeamInfo.post(this::changeToNormalMode);
    }

    @Override
    public void clearMembers() {
        adapter.clear();
        adapter.notifyDataSetChanged();
    }

    @Override
    public void hideKeyboard() {
        if (etSearch == null) {
            return;
        }
        inputMethodManager.hideSoftInputFromWindow(etSearch.getWindowToken(), 0);
    }

    private boolean isFinishing() {
        return getActivity() == null || getActivity().isFinishing();
    }

    enum UiMode {
        NORMAL, SEARCH
    }
}
