package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RetrieveTeamInformation;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityItemListAdapter;
import com.tosslab.jandi.app.network.JandiEntityClient;
import com.tosslab.jandi.app.network.JandiRestClient;
import com.tosslab.jandi.app.network.models.ResInvitation;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FormatConverter;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.rest.RestService;
import org.apache.log4j.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 9. 18..
 */
@EActivity(R.layout.activity_team_info)
public class TeamInfoActivity extends Activity {
    private final Logger log = Logger.getLogger(TeamInfoActivity.class);

    @ViewById(R.id.list_team_users)
    ListView listViewInvitation;
    @Bean
    EntityItemListAdapter teamUserListAdapter;

    @RestService
    JandiRestClient jandiRestClient;

    private Context mContext;
    private ProgressWheel mProgressWheel;
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.
    private JandiEntityClient mJandiEntityClient;
    private String mMyToken;

    private EditText mEditTextEmailAddress;

    @AfterViews
    public void initForm() {
        mContext = getApplicationContext();

        setUpActionBar();
        initProgressWheel();
        initNetworkClientForInvitation();
        addInvitationViewAsListviewFooter();

        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);

    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    private void initProgressWheel() {
        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();
    }

    private void initNetworkClientForInvitation() {
        mMyToken = JandiPreference.getMyToken(this);
        mJandiEntityClient = new JandiEntityClient(jandiRestClient, mMyToken);
    }

    private void addInvitationViewAsListviewFooter() {
        View footer = getLayoutInflater().inflate(R.layout.footer_invite_user, null, false);
        mEditTextEmailAddress = (EditText) footer.findViewById(R.id.et_invitation_email);
        Button buttonInvitation = (Button) footer.findViewById(R.id.btn_invitation_confirm);
        buttonInvitation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                imm.hideSoftInputFromWindow(mEditTextEmailAddress.getWindowToken(),0);
                String email = mEditTextEmailAddress.getEditableText().toString();

                if (FormatConverter.isInvalidEmailString(email)) {
                    ColoredToast.showWarning(mContext, "올바른 이메일 주소를 입력하세요");
                    return;
                } else {
                    mEditTextEmailAddress.setText("");
                    inviteTeamMember(email);
                }
            }
        });
        listViewInvitation.addFooterView(footer);
        listViewInvitation.setAdapter(teamUserListAdapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().registerSticky(this);
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.update_profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_update_profile:
                listViewInvitation.setSelection(teamUserListAdapter.getCount());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onEvent(RetrieveTeamInformation event) {
        retrieveTeamUserList(event.users);
    }

    void retrieveTeamUserList(List<FormattedEntity> users) {
        teamUserListAdapter.retrieveList(users);
    }

    /************************************************************
     * 팀원으로 초대
     ************************************************************/
    @UiThread
    public void inviteTeamMember(String email) {
        mProgressWheel.show();
        inviteTeamMemberInBackground(email);
    }

    @Background
    public void inviteTeamMemberInBackground(String email) {
        try {
            ResInvitation resInvitation = mJandiEntityClient.inviteTeamMember(email);
            inviteTeamMemberSucceed(resInvitation);
        } catch (JandiNetworkException e) {
            log.error("Invitation failed", e);
            inviteTeamMemberFailed("Invitation failed");
        } catch (Exception e) {
            log.error("Invitation failed", e);
            inviteTeamMemberFailed("Invitation failed");
        }

    }

    @UiThread
    public void inviteTeamMemberSucceed(ResInvitation resInvitation) {
        mProgressWheel.dismiss();
        if (resInvitation.sendMailFailCount == 0) {
            ColoredToast.show(this, "Invitation is succeed");
        }
    }

    @UiThread
    public void inviteTeamMemberFailed(String message) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, message);
    }
}
