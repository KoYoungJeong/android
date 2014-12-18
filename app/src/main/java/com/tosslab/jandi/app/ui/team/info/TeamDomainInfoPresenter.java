package com.tosslab.jandi.app.ui.team.info;

import android.app.Activity;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 17..
 */
@EBean
public class TeamDomainInfoPresenter {

    @ViewById(R.id.et_team_detail_info_team_name)
    TextView teamNameView;

    @ViewById(R.id.et_team_detail_info_team_domain)
    TextView teamDomainView;

    @ViewById(R.id.et_team_detail_info_member_name)
    TextView myNameView;

    @ViewById(R.id.spinner_team_detail_info_member_email)
    Spinner myEmailView;

    @RootContext
    Activity activity;

    public void setTeamCreatable(boolean isTeamCreatable) {

        teamNameView.setEnabled(isTeamCreatable);
        teamDomainView.setEnabled(isTeamCreatable);

    }

    public String getTeamName() {
        return teamNameView.getText().toString();
    }

    public String getTeamDomain() {
        return teamDomainView.getText().toString();
    }

    public String getMyName() {
        return myNameView.getText().toString();
    }

    public String getMyEmail() {
        return myEmailView.getPrompt().toString();
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
    public void setEmails(List<ResAccountInfo.UserEmail> userEmails) {

        List<String> emails = new ArrayList<String>();

        for (ResAccountInfo.UserEmail userEmail : userEmails) {
            emails.add(userEmail.getId());
        }

        myEmailView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, emails));
        myEmailView.setPrompt(emails.get(0));
    }
}
