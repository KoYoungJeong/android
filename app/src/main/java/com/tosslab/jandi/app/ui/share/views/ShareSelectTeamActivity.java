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

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.local.orm.repositories.AccountRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.share.views.adapter.ShareTeamsAdapter;
import com.tosslab.jandi.app.ui.share.views.dagger.DaggerShareSelectRoomComponent;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.ProgressWheel;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import dagger.Lazy;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ShareSelectTeamActivity extends BaseAppCompatActivity implements ShareTeamsAdapter.OnItemClickListener {

    @Bind(R.id.lv_select_team)
    RecyclerView lvSelectTeam;

    @Inject
    Lazy<StartApi> startApi;
    ShareTeamsAdapter adapter;

    ProgressWheel progressWheel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_team);
        ButterKnife.bind(this);
        DaggerShareSelectRoomComponent.create().inject(this);

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
                .map(Team::createTeam)
                .collect(() -> new ArrayList<Team>(), ArrayList::add)
                .doOnNext(teams -> {
                    ResAccountInfo.UserTeam selectedTeam = AccountRepository.getRepository().getSelectedTeamInfo();
                    if (selectedTeam == null) {
                        return;
                    } else {
                        Observable.from(teams)
                                .filter(team -> team.getTeamId() == selectedTeam.getTeamId())
                                .subscribe(team -> {
                                    team.setSelected(true);
                                });
                    }
                })
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

        Observable.defer(() -> {
            try {
                return Observable.just(startApi.get().getInitializeInfo(teamId));
            } catch (RetrofitException e) {
                return Observable.empty();
            }
        })
                .doOnNext(initialInfo -> {
                    InitialInfoRepository.getInstance().upsertInitialInfo(initialInfo);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(it -> {
                    if (progressWheel != null && progressWheel.isShowing()) {
                        progressWheel.dismiss();
                    }
                    ShareSelectTeamEvent event = new ShareSelectTeamEvent();
                    event.setTeamId(teamId);
                    event.setTeamName(teamName);
                    EventBus.getDefault().post(event);
                    finish();
                });
    }

}
