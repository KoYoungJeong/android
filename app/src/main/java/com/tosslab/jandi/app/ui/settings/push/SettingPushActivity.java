package com.tosslab.jandi.app.ui.settings.push;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

public class SettingPushActivity extends BaseAppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_push);
        initView();
    }

    void initView() {
        setUpActionBar();

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
