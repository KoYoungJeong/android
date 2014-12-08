package com.tosslab.jandi.app.ui.settings;

import android.app.ActionBar;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.MenuItem;

import org.androidannotations.annotations.EActivity;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 7. 18..
 */
@EActivity
public class SettingsActivity extends PreferenceActivity {
    private final Logger log = Logger.getLogger(SettingsActivity.class);


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpActionBar();

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment_()).commit();
    }

    private void setUpActionBar() {
        // Set up the action bar.
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
