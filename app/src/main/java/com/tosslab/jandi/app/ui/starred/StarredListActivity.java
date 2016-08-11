package com.tosslab.jandi.app.ui.starred;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import butterknife.ButterKnife;

/**
 * Created by tee on 15. 7. 28..
 */
public class StarredListActivity extends BaseAppCompatActivity {

    public static void start(Activity activity) {
        Intent intent = new Intent(activity, StarredListActivity.class);
        activity.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_starred_list);

        ButterKnife.bind(this);

        setupActionBar();

        String tag = StarredListFragment.class.getSimpleName();
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
        if (fragment == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new StarredListFragment(), tag)
                    .commit();
        }

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.Stars);
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_starred_list);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        getSupportActionBar().setTitle(R.string.jandi_starred_stars);
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
