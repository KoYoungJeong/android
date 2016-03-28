package com.tosslab.jandi.app.ui.settings.main;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.settings.main.view.SettingsFragment;
import com.tosslab.jandi.app.ui.settings.main.view.SettingsFragment_;


import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/**
 * Created by justinygchoi on 2014. 7. 18..
 */
@EActivity(R.layout.activity_setting)
public class SettingsActivity extends BaseAppCompatActivity {

    @AfterViews
    void initView() {
        setUpActionBar();

        Fragment settingFragment =
                getFragmentManager().findFragmentByTag(SettingsFragment.class.getName());
        if (settingFragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fl_content,
                            SettingsFragment_.builder().build(), SettingsFragment.class.getName())
                    .commit();
        }
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
