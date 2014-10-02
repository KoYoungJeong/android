package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.nhaarman.supertooltips.ToolTip;
import com.nhaarman.supertooltips.ToolTipRelativeLayout;
import com.nhaarman.supertooltips.ToolTipView;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedDummyEntity;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.team.TeamMemberListAdapter;
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

/**
 * Created by justinygchoi on 2014. 9. 18..
 */
@EActivity(R.layout.activity_team_info)
public class TeamInfoActivity extends Activity {
    private final Logger log = Logger.getLogger(TeamInfoActivity.class);

    @ViewById(R.id.list_team_users)
    ListView listViewInvitation;
    @Bean
    TeamMemberListAdapter teamUserListAdapter;

    @RestService
    JandiRestClient jandiRestClient;

    private Context mContext;
    private ProgressWheel mProgressWheel;
    private InputMethodManager imm;     // 메시지 전송 버튼 클릭시, 키보드 내리기를 위한 매니저.
    private JandiEntityClient mJandiEntityClient;
    private String mMyToken;

    private EditText mEditTextEmailAddress;
    private Button mButtonInvitation;
    private ToolTipRelativeLayout mToolTipRelativeLayout;
    private ToolTipView mToolTipView;

    @AfterViews
    public void initForm() {
        mContext = getApplicationContext();
        setUpToolTip();
        setUpActionBar();
        initProgressWheel();
        initNetworkClientForInvitation();
        addInvitationViewAsListviewFooter();

        imm = (InputMethodManager)this.getSystemService(Context.INPUT_METHOD_SERVICE);

        EntityManager entityManager = ((JandiApplication)getApplication()).getEntityManager();
        retrieveTeamUserList(entityManager.getFormattedUsers());
    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    private void setUpToolTip() {
        mToolTipRelativeLayout = (ToolTipRelativeLayout) findViewById(R.id.activity_main_tooltipRelativeLayout);
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
        mButtonInvitation = (Button) footer.findViewById(R.id.btn_invitation_confirm);

        // 텍스트에 글이 있으면 버튼 색상 변경
        mEditTextEmailAddress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) { }

            @Override
            public void afterTextChanged(Editable editable) {
                if (editable.toString().trim().length() > 0) {
                    mButtonInvitation.setBackgroundResource(R.drawable.btn_send_selector);
                } else {
                    mButtonInvitation.setBackgroundResource(R.color.jandi_inactive_button);
                }
            }
        });

        mButtonInvitation.setOnClickListener(new View.OnClickListener() {
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

        // 스크롤의 맨 아래로 내려가면 안내 tooltip 보이기
        listViewInvitation.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) { }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem,
                                 int visibleItemCount, int totalItemCount) {
                if ((visibleItemCount == (totalItemCount - firstVisibleItem))) {
                    showToolTip();
                } else {
                    hideToolTip();
                }
            }
        });
    }

    @Override
    protected void onStop() {
        if (mProgressWheel != null)
            mProgressWheel.dismiss();
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.team_info_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            case R.id.action_invitation:
                listViewInvitation.setSelection(teamUserListAdapter.getCount());
                showToolTip();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    void retrieveTeamUserList(List<FormattedEntity> users) {
        teamUserListAdapter.retrieveList(users);
    }

    private void showToolTip() {
        // Button에 툴팁 넣기
        if (mToolTipView == null) {
            ToolTip toolTip = new ToolTip()
                    .withText(getString(R.string.jandi_invitation_help))
                    .withTextColor(getResources().getColor(R.color.jandi_text_white))
                    .withColor(getResources().getColor(R.color.jandi_main))
                    .withAnimationType(ToolTip.AnimationType.NONE);

            mToolTipView = mToolTipRelativeLayout.showToolTipForViewResId(this, toolTip, R.id.btn_invitation_tooltipBase);

        }
    }

    private void hideToolTip() {
        if (mToolTipView != null) {
            mToolTipView.remove();
            mToolTipView = null;
        }
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
            inviteTeamMemberSucceed(resInvitation, email);
        } catch (JandiNetworkException e) {
            log.error("Invitation failed", e);
            inviteTeamMemberFailed(getString(R.string.err_invitation_failed));
        } catch (Exception e) {
            log.error("Invitation failed", e);
            inviteTeamMemberFailed(getString(R.string.err_invitation_failed));
        }
    }

    @UiThread
    public void inviteTeamMemberSucceed(ResInvitation resInvitation, String succeedEmail) {
        mProgressWheel.dismiss();
        if (resInvitation.sendMailFailCount == 0) {
            ColoredToast.show(this, getString(R.string.jandi_invitation_succeed));
            teamUserListAdapter.addMember(new FormattedDummyEntity(succeedEmail));
        }
    }

    @UiThread
    public void inviteTeamMemberFailed(String message) {
        mProgressWheel.dismiss();
        ColoredToast.showError(this, message);
    }
}
