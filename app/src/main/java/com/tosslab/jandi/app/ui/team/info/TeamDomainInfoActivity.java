package com.tosslab.jandi.app.ui.team.info;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.mixpanel.MixpanelMemberAnalyticsClient;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.team.info.presenter.TeamDomainInfoPresenter;
import com.tosslab.jandi.app.ui.team.info.presenter.TeamDomainInfoPresenterImpl;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.lib.sprinkler.Sprinkler;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

@EActivity(R.layout.activity_team_domain_info)
@OptionsMenu(R.menu.teamdomain_info)
public class TeamDomainInfoActivity extends BaseAppCompatActivity implements TeamDomainInfoPresenter.View {

    @ViewById(R.id.et_team_detail_info_team_name)
    TextView tvTeamName;

    @ViewById(R.id.et_team_detail_info_team_domain)
    TextView tvTeamDomain;

    ProgressWheel progressWheel;

    @Bean(TeamDomainInfoPresenterImpl.class)
    TeamDomainInfoPresenter teamDomainInfoPresenter;

    @AfterViews
    void initView() {

        setUpActionBar();

        teamDomainInfoPresenter.setView(this);
        teamDomainInfoPresenter.checkEmailInfo();
        progressWheel = new ProgressWheel(TeamDomainInfoActivity.this);

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.CreateaTeam);
        Sprinkler.with(JandiApplication.getContext())
                .track(new FutureTrack.Builder()
                        .event(Event.ScreenView)
                        .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                        .property(PropertyKey.ScreenView, ScreenViewProperty.TEAM_CREATE)
                        .build());

        MixpanelMemberAnalyticsClient.getInstance(TeamDomainInfoActivity.this, null)
                .pageViewTeamCreate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
    }

    @OptionsItem(android.R.id.home)
    void goHomeUpMenu() {
        finish();
    }


    @OptionsItem(R.id.action_confirm)
    void confirmTeamDomain() {
        String teamName = tvTeamName.getText().toString();
        String teamDomain = tvTeamDomain.getText().toString();

        if (TextUtils.isEmpty(teamDomain)) {
            showFailToast(getString(R.string.err_invalid_team_domain));
            return;
        }

        teamDomainInfoPresenter.createTeam(teamName, teamDomain.toLowerCase());
    }

    private void setUpActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

    }


    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void failCreateTeam(int statusCode) {
        ColoredToast.showWarning(getString(R.string.fail_to_create_team));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void successCreateTeam(String name) {
        ColoredToast.show(getString(R.string.jandi_message_create_entity, name));

        setResult(Activity.RESULT_OK);
        finish();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

    }


    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showFailToast(String message) {
        ColoredToast.showWarning(message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finishView() {
        finish();
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }
}
