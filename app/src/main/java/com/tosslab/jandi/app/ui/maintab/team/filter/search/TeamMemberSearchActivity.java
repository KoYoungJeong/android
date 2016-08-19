package com.tosslab.jandi.app.ui.maintab.team.filter.search;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.maintab.team.adapter.TeamViewPagerAdapter;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import rx.Observable;
import rx.subjects.PublishSubject;

public class TeamMemberSearchActivity extends BaseAppCompatActivity {

    public static final String EXTRA_KEY_SELECT_MODE = "selectMode";

    @Bind(R.id.actionbar_team_member_search)
    Toolbar toolbar;

    @Bind(R.id.iv_team_member_search_mic)
    ImageView ivMic;

    @Bind(R.id.et_team_member_search_keyword)
    EditText etSearch;

    @Bind(R.id.viewpager_team_member_search)
    ViewPager viewPager;
    @Bind(R.id.tabs_team_member_search)
    TabLayout tabLayout;
    @Nullable
    @InjectExtra
    int position = 0;

    @Nullable
    @InjectExtra
    boolean isSelectMode = false;

    private TeamViewPagerAdapter adapter;
    private PublishSubject<String> keywordSubject;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_member_search);

        ButterKnife.bind(this);
        Dart.inject(this);
        keywordSubject = PublishSubject.create();

        Observable<String> keywordObservable = keywordSubject.throttleLast(300, TimeUnit.MILLISECONDS)
                .distinctUntilChanged();

        viewPager.setOffscreenPageLimit(2);
        adapter = new TeamViewPagerAdapter(this, getSupportFragmentManager(), keywordObservable, isSelectMode);
        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setCurrentItem(position);

        setUpToolbar();
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    @OnTextChanged(R.id.et_team_member_search_keyword)
    void onSearchTextChanged(CharSequence text) {
        keywordSubject.onNext(text.toString());
    }

    @Override
    protected void onDestroy() {
        keywordSubject.onCompleted();
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
