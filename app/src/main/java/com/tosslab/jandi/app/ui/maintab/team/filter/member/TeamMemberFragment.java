package com.tosslab.jandi.app.ui.maintab.team.filter.member;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eowise.recyclerview.stickyheaders.StickyHeadersBuilder;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberAdapter;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberDataView;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.adapter.TeamMemberHeaderAdapter;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.dagger.DaggerTeamMemberComponent;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.dagger.TeamMemberModule;
import com.tosslab.jandi.app.ui.maintab.team.filter.member.presenter.TeamMemberPresenter;
import com.tosslab.jandi.app.ui.maintab.team.filter.search.KeywordObservable;
import com.tosslab.jandi.app.ui.maintab.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;

public class TeamMemberFragment extends Fragment implements TeamMemberPresenter.View, KeywordObservable {

    @Bind(R.id.list_team_member)
    RecyclerView lvMember;

    @Inject
    TeamMemberPresenter presenter;
    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_KEY_SELECT_MODE)
    boolean selectMode;
    private TeamMemberDataView teamMemberDataView;

    public static Fragment create(Context context) {
        return Fragment.instantiate(context, TeamMemberFragment.class.getName());
    }

    public static Fragment create(Context context, boolean selectMode) {
        Bundle args = new Bundle();
        args.putBoolean(TeamMemberSearchActivity.EXTRA_KEY_SELECT_MODE, selectMode);
        return Fragment.instantiate(context, TeamMemberFragment.class.getName(), args);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_member, container, false);
        ButterKnife.bind(this, view);
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Dart.inject(this, getArguments());

        TeamMemberAdapter adapter = new TeamMemberAdapter();
        adapter.setSelectedMode(selectMode);
        teamMemberDataView = adapter;
        lvMember.setLayoutManager(new LinearLayoutManager(getActivity()));

        if (!selectMode) {
            adapter.setHasStableIds(true);
            lvMember.addItemDecoration(new StickyHeadersBuilder()
                    .setAdapter(adapter)
                    .setRecyclerView(lvMember)
                    .setSticky(true)
                    .setStickyHeadersAdapter(new TeamMemberHeaderAdapter(adapter), false)
                    .build());
        }

        lvMember.setAdapter(adapter);

        DaggerTeamMemberComponent.builder()
                .teamMemberModule(new TeamMemberModule(this, adapter, selectMode))
                .build()
                .inject(this);

        presenter.onCreate();

        teamMemberDataView.setOnItemClickListener((view, adapter1, position) -> {
            presenter.onItemClick(position);
        });
    }

    @Override
    public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void refreshDataView() {
        teamMemberDataView.refresh();
    }

    @Override
    public void moveProfile(long userId) {
        MemberProfileActivity_.intent(TeamMemberFragment.this)
                .memberId(userId)
                .from(MemberProfileActivity.EXTRA_FROM_TEAM_MEMBER)
                .start();
    }

    @Override
    public void setKeywordObservable(Observable<String> keywordObservable) {
        keywordObservable.subscribe(text -> {
            if (presenter != null) {
                presenter.onSearchKeyword(text);
            }
        });
    }

}
