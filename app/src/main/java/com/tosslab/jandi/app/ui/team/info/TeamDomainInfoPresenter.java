package com.tosslab.jandi.app.ui.team.info;

import android.app.Activity;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
@EBean
public class TeamDomainInfoPresenter {

    @ViewById(R.id.et_team_detail_info_team_name)
    TextView teamNameView;

    @ViewById(R.id.et_team_detail_info_team_domain)
    TextView teamDomainView;


    @RootContext
    Activity activity;

    @SystemService
    InputMethodManager inputMethodManager;

    ProgressWheel progressWheel;

    @AfterViews
    void initView() {
        progressWheel = new ProgressWheel(activity);
        progressWheel.init();
    }

    public void setTeamCreatable(boolean isTeamCreatable) {

        teamNameView.setEnabled(isTeamCreatable);
        teamDomainView.setEnabled(isTeamCreatable);

    }

    public String getTeamName() {
        return teamNameView.getText().toString();
    }

    public void setTeamName(String teamName) {
        teamNameView.setText(teamName);
    }

    public String getTeamDomain() {
        return teamDomainView.getText().toString();
    }

    public void setTeamDomain(String domain) {
        teamDomainView.setText(domain);
    }


    @UiThread
    public void failCreateTeam(int statusCode) {
        ColoredToast.showWarning(activity, activity.getString(R.string.fail_to_create_team));
    }

    @UiThread
    public void successCreateTeam(String name) {
        ColoredToast.show(activity, activity.getString(R.string.jandi_message_create_entity, name));

        activity.setResult(Activity.RESULT_OK);
        activity.finish();

    }


    @UiThread
    public void successJoinTeam() {

        activity.setResult(Activity.RESULT_OK);
        activity.finish();

    }

    @UiThread
    public void failJoinTeam() {
        ColoredToast.showWarning(activity, activity.getString(R.string.jandi_team_join_fail));
    }

    @UiThread
    public void showProgressWheel() {
        dismissProgressWheel();

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

    }


    @UiThread
    public void showFailToast(String message) {
        ColoredToast.showWarning(activity, message);
    }
}
