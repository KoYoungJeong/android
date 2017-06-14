package com.tosslab.jandi.app.ui.settings.push;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialAccountInfoRepository;
import com.tosslab.jandi.app.network.models.start.Absence;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SettingPushActivity extends BaseAppCompatActivity {

    @Bind(R.id.fl_content)
    ViewGroup flContent;

    @Bind(R.id.tv_unavailable)
    TextView tvUnavailable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_push);
        ButterKnife.bind(this);

        initView();

    }

    void initView() {
        setUpActionBar();

        Absence absence = InitialAccountInfoRepository.getInstance().getAbsenceInfo();

        long todayInMillis = System.currentTimeMillis();

        if (absence != null &&
                (absence.getStartAt().getTime() < todayInMillis) &&
                (absence.getEndAt().getTime() > todayInMillis) &&
                absence.getStatus().equals("enabled")) {
            flContent.setVisibility(View.GONE);
            tvUnavailable.setVisibility(View.VISIBLE);
        } else {
            Fragment settingPushFragment =
                    getFragmentManager().findFragmentByTag(SettingsPushFragment.class.getName());
            if (settingPushFragment == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fl_content,
                                new SettingsPushFragment(),
                                SettingsPushFragment.class.getName())
                        .commit();
            }
        }
    }

    private void setUpActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);

        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setTitle(R.string.jandi_setting_notification);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

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

}
