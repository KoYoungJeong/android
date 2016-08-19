package com.tosslab.jandi.app.ui.maintab.team.filter.dept;


import android.content.Context;
import android.content.Intent;
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
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.adapter.DeptJobAdapter;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.adapter.DeptJobDataView;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.adapter.DeptJobHeaderAdapter;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.dagger.DaggerDeptJobComponent;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.dagger.DeptJobModule;
import com.tosslab.jandi.app.ui.maintab.team.filter.dept.presenter.DeptJobPresenter;
import com.tosslab.jandi.app.ui.maintab.team.filter.search.KeywordObservable;
import com.tosslab.jandi.app.ui.maintab.team.filter.search.TeamMemberSearchActivity;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;

public class DeptJobFragment extends Fragment implements DeptJobPresenter.View, KeywordObservable {

    public static final int EXTRA_TYPE_DEPT = 1;
    public static final int EXTRA_TYPE_JOB = 2;
    public static final String EXTRA_TYPE = "type";
    private static final int REQ_MEMBERS_OF_GROUP = 101;

    @Nullable
    @InjectExtra(EXTRA_TYPE)
    int type = EXTRA_TYPE_DEPT;
    @Nullable
    @InjectExtra(TeamMemberSearchActivity.EXTRA_KEY_SELECT_MODE)
    boolean selectMode = false;
    @Bind(R.id.list_team_dept_job)
    RecyclerView lvMember;

    @Inject
    DeptJobDataView deptJobDataView;

    @Inject
    DeptJobPresenter deptJobPresenter;

    public static Fragment create(Context context, int type) {
        Bundle args = new Bundle(1);
        args.putInt(EXTRA_TYPE, type);
        return Fragment.instantiate(context, DeptJobFragment.class.getName(), args);
    }

    public static Fragment create(Context context, int type, boolean selectMode) {
        Bundle args = new Bundle(1);
        args.putInt(EXTRA_TYPE, type);
        args.putBoolean(TeamMemberSearchActivity.EXTRA_KEY_SELECT_MODE, selectMode);
        return Fragment.instantiate(context, DeptJobFragment.class.getName(), args);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team_dept_job, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Dart.inject(this, getArguments());

        DeptJobAdapter adapter = new DeptJobAdapter();
        lvMember.setLayoutManager(new LinearLayoutManager(getActivity()));
        lvMember.addItemDecoration(new SimpleDividerItemDecoration());
        if (!selectMode) {
            adapter.setHasStableIds(true);
            lvMember.addItemDecoration(new StickyHeadersBuilder()
                    .setAdapter(adapter)
                    .setRecyclerView(lvMember)
                    .setSticky(true)
                    .setStickyHeadersAdapter(new DeptJobHeaderAdapter(adapter), false)
                    .build());
        }

        lvMember.setAdapter(adapter);
        DaggerDeptJobComponent.builder()
                .deptJobModule(new DeptJobModule(this, adapter, type))
                .build()
                .inject(this);

        deptJobDataView.setOnItemClick((view, adapter1, position) -> {
            startActivityForResult(Henson.with(getActivity())
                    .gotoDeptJobGroupActivity()
                    .keyword(((DeptJobAdapter) adapter1).getItem(position).first)
                    .type(type)
                    .selectMode(selectMode)
                    .build(), REQ_MEMBERS_OF_GROUP);

        });

        deptJobPresenter.onCreate();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (REQ_MEMBERS_OF_GROUP == requestCode) {
            // TODO: 2016. 8. 17. 후처리 넣기
        }
    }

    @Override
    public void onDestroy() {
        deptJobPresenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void refreshDataView() {
        deptJobDataView.refresh();
    }

    @Override
    public void setKeywordObservable(Observable<String> keywordObservable) {
        keywordObservable.subscribe(text -> {
            if (deptJobPresenter != null) {
                deptJobPresenter.onSearchKeyword(text);
            }
        });
    }
}
