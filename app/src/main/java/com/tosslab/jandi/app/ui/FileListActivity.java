package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.CategorizingAsEntity;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
@EActivity(R.layout.activity_file_list)
public class FileListActivity extends Activity {
    @Extra
    int entityId;

    @AfterViews
    void attatchFragment() {
        setUpActionBar();

        getFragmentManager().beginTransaction()
                .add(R.id.container_file_list, FileListFragment_.builder().build())
                .commit();
        EventBus.getDefault().post(new CategorizingAsEntity(entityId));
    }

    private void setUpActionBar() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
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
