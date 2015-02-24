package com.tosslab.jandi.app.ui.maintab.file;

import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;

/**
 * Created by justinygchoi on 2014. 10. 13..
 */
@EActivity(R.layout.activity_file_list)
public class FileListActivity extends ActionBarActivity {

    @Extra
    int entityId;

    @Extra
    String entityName;

    @AfterViews
    void attatchFragment() {
        setUpActionBar();

        getSupportFragmentManager()
                .beginTransaction()
                .add(
                        R.id.fl_content,
                        FileListFragment_
                                .builder()
                                .entityIdForCategorizing(entityId)
                                .mCurrentEntityCategorizingAccodingBy(entityName)
                                .build())
                .commit();
    }

    private void setUpActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
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

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
    }
}
