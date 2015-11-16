package com.tosslab.jandi.app.ui.settings.push;

import android.app.Fragment;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.settings.SettingsFragment;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;

/**
 * Created by tee on 15. 11. 5..
 */
@EActivity(R.layout.activity_setting_push)
public class SettingPushActivity extends BaseAppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        ActivityHelper.setOrientation(this);
    }

    @AfterViews
    void initView() {
        setUpActionBar();

        Fragment settingPushFragment =
                getFragmentManager().findFragmentByTag(SettingsFragment.class.getName());
        if (settingPushFragment == null) {
            getFragmentManager().beginTransaction()
                    .add(R.id.fl_content,
                            new SettingPushFragment_(), SettingPushFragment.class.getName())
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
