package com.tosslab.jandi.app.ui.team.info;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
@EActivity(R.layout.activity_team_domain_info)
public class TeamDomainInfoActivity extends Activity {

    @Extra()
    String mode = "Info";

    @ViewById(R.id.et_team_detail_info_team_name)
    TextView teamNameView;

    @ViewById(R.id.et_team_detail_info_team_domain)
    TextView teamDomainView;

    @AfterViews
    void initView() {

        Mode activityMode = Mode.valueOf(mode);

        switch (activityMode) {
            case Create:
                teamNameView.setEnabled(true);
                teamDomainView.setEnabled(true);
                break;
            case Info:
                teamNameView.setEnabled(false);
                teamDomainView.setEnabled(false);
                break;
        }
        setUpActionBar(activityMode);
    }

    private void setUpActionBar(Mode mode) {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));

        switch (mode) {
            case Create:
                actionBar.setTitle(R.string.team_create);
                break;
            case Info:
                actionBar.setTitle(R.string.team_info);
                break;
        }
    }


    public enum Mode {
        Create, Info
    }

}
