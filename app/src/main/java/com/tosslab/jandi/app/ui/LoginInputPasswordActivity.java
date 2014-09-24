package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.view.MenuItem;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMyTeam;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;

/**
 * Created by justinygchoi on 2014. 9. 25..
 */
@EActivity(R.layout.activity_intro_final)
public class LoginInputPasswordActivity extends Activity {
    private final Logger log = Logger.getLogger(LoginInputPasswordActivity.class);
    @Extra
    String myId;
    @Extra
    String jsonExtraTeam;

    @AfterViews
    void initView() {
        setUpActionBar();
        try {
            ResMyTeam.Team myTeam = convertJsonToPojo();
            getActionBar().setTitle(myTeam.name);
        } catch (IOException e) {
            ColoredToast.showError(this, "Parsing Error");
        }
    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    private ResMyTeam.Team convertJsonToPojo() throws IOException {
        log.debug("JSON Extra : " + jsonExtraTeam);
        return new ObjectMapper().readValue(jsonExtraTeam, ResMyTeam.Team.class);
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.pull_in_left, R.anim.push_out_right);
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
