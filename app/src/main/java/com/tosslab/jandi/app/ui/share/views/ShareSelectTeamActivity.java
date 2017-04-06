package com.tosslab.jandi.app.ui.share.views;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.RankRepository;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.network.models.team.rank.Ranks;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.share.views.adapter.ShareTeamsAdapter;
import com.tosslab.jandi.app.ui.share.views.dagger.DaggerShareSelectTeamComponent;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class ShareSelectTeamActivity extends BaseAppCompatActivity implements ShareTeamsAdapter.OnItemClickListener {

    @Bind(R.id.lv_select_team)
    RecyclerView lvSelectTeam;

    @Nullable
    @InjectExtra
    long selectedTeamId = -1;

    @Inject
    Lazy<StartApi> startApi;
    @Inject
    Lazy<TeamApi> teamApi;

    ShareTeamsAdapter adapter;

    ProgressWheel progressWheel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_team);
        ButterKnife.bind(this);
        Dart.inject(this);

        DaggerShareSelectTeamComponent
                .builder().build()
                .inject(this);

        adapter = new ShareTeamsAdapter();
        initViews();
    }

    void initViews() {
        setupActionbar();
        lvSelectTeam.setLayoutManager(new LinearLayoutManager(this));
        lvSelectTeam.setAdapter(adapter);
        initTeams();
        adapter.setOnItemClickListener(this);
    }

    void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle(R.string.jandi_share_to_jandi);
    }

    void initTeams() {
        Observable.from(AccountRepository.getRepository().getAccountTeams())
                .filter(userTeam -> !TextUtils.equals(userTeam.getStatus(), "pending"))
                .map((userTeam1) -> {
                    Team team = Team.createTeam(userTeam1);
                    if (selectedTeamId != -1) {
                        team.setSelected(userTeam1.getTeamId() == selectedTeamId);
                    }
                    return team;
                })
                .collect((Func0<ArrayList<Team>>) ArrayList::new, ArrayList::add)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showTeamList);

    }

    void showTeamList(List<Team> teams) {
        adapter.setItems(teams);
        adapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemClick(long teamId, String teamName) {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        progressWheel = new ProgressWheel(ShareSelectTeamActivity.this);
        progressWheel.show();

        Completable.fromCallable(() -> {
            String initializeInfo = startApi.get().getRawInitializeInfo(teamId);
            InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));

            if (!RankRepository.getInstance().hasRanks(teamId)) {
                Ranks ranks = teamApi.get().getRanks(teamId);
                RankRepository.getInstance().addRanks(ranks.getRanks());
            }
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    if (progressWheel != null && progressWheel.isShowing()) {
                        progressWheel.dismiss();
                    }
                    ShareSelectTeamEvent event = new ShareSelectTeamEvent();
                    event.setTeamId(teamId);
                    event.setTeamName(teamName);
                    EventBus.getDefault().post(event);
                    finish();
                }, t -> {
                    if (progressWheel != null && progressWheel.isShowing()) {
                        progressWheel.dismiss();
                    }
                });
    }

}
