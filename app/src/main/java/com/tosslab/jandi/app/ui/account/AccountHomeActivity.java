package com.tosslab.jandi.app.ui.account;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.account.presenter.AccountHomePresenter;
import com.tosslab.jandi.app.ui.account.presenter.AccountHomePresenterImpl;
import com.tosslab.jandi.app.ui.maintab.MainTabActivity_;
import com.tosslab.jandi.app.ui.profile.email.EmailChooseActivity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.ui.team.info.TeamDomainInfoActivity_;
import com.tosslab.jandi.app.ui.team.select.to.Team;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.views.AccountPendingTeamRowView;
import com.tosslab.jandi.app.views.AccountTeamRowView;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 3. 2..
 */
@EActivity(R.layout.activity_account_main)
@OptionsMenu(R.menu.account_home)
public class AccountHomeActivity extends AppCompatActivity implements AccountHomePresenter.View {

    private static final int REQ_TEAM_CREATE = 101;
    private static final int REQ_EMAIL_CHOOSE = 201;
    private static final int REQ_TEAM_JOIN = 301;

    @Bean(AccountHomePresenterImpl.class)
    AccountHomePresenter accountHomePresenter;
    @ViewById(R.id.txt_account_main_name)
    TextView accountNameTextView;
    @ViewById(R.id.txt_account_main_id_email)
    TextView emailTextView;
    @ViewById(R.id.ll_account_main_team_choose)
    LinearLayout teamLayout;
    ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        accountHomePresenter.setView(this);
    }

    @AfterViews
    void initView() {
        progressWheel = new ProgressWheel(AccountHomeActivity.this);
        progressWheel.init();
        setUpActionBar();
    }

    private void setUpActionBar() {

        Toolbar toolbar = (Toolbar) findViewById(R.id.my_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.jadi_account_home);

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @OptionsItem(R.id.action_help)
    void onHelpOptionSelect() {
        accountHomePresenter.onHelpOptionSelect();
    }

    @Click(R.id.txt_account_main_name)
    void onNameEditClick() {
        accountHomePresenter.onAccountNameEditClick(accountNameTextView.getText().toString());
    }

    @Click(R.id.ll_account_main_email)
    void onEmailEditClick() {
        accountHomePresenter.onAccountEmailEditClick();
    }

    @UiThread
    @Override
    public void showErrorToast(String message) {
        ColoredToast.showError(AccountHomeActivity.this, message);
    }

    @UiThread
    @Override
    public void setTeamInfo(ArrayList<Team> allTeamInfos, ResAccountInfo.UserTeam selectedTeamInfo) {

        teamLayout.removeAllViews();

        for (Team team : allTeamInfos) {
            View view = null;

            switch (team.getStatus()) {
                case JOINED:

                    AccountTeamRowView accountTeamRowView = new AccountTeamRowView(AccountHomeActivity.this);
                    accountTeamRowView.setBadgeCount(team.getUnread());
                    accountTeamRowView.setTeamName(team.getName());

                    if (selectedTeamInfo != null && team.getTeamId() == selectedTeamInfo.getTeamId()) {
                        accountTeamRowView.setSelected(true);
                    }

                    accountTeamRowView.setOnClickListener(v -> {
                        Team clickedTeam = (Team) v.getTag();
                        accountHomePresenter.onJoinedTeamSelect(clickedTeam.getTeamId(), false);
                    });
                    view = accountTeamRowView;
                    break;
                case PENDING:
                    AccountPendingTeamRowView accountPendingTeamRowView = new AccountPendingTeamRowView(AccountHomeActivity.this);
                    accountPendingTeamRowView.setTeamName(team.getName());
                    accountPendingTeamRowView.setOnJoinClickListener(new AccountPendingTeamRowView.OnJoinClickListener() {
                        @Override
                        public void onJoinClick(View view, boolean join) {
                            Team selectedTeam = (Team) view.getTag();
                            if (join) {
                                accountHomePresenter.onRequestJoin(selectedTeam);
                            } else {
                                accountHomePresenter.onRequestIgnore(selectedTeam);
                            }
                        }
                    });
                    view = accountPendingTeamRowView;
                    break;
                case CREATE:
                    AccountTeamRowView accountTeamRowView1 = new AccountTeamRowView(AccountHomeActivity.this);
                    accountTeamRowView1.setTeamName(getString(R.string.jandi_team_select_create_a_team));
                    accountTeamRowView1.setIcon(R.drawable.jandi_icon_teamlist_add);
                    accountTeamRowView1.setBadgeCount(0);
                    accountTeamRowView1.setNameTextColor(getResources().getColorStateList(R.color.jandi_accent_color));
                    accountTeamRowView1.setOnClickListener(v -> accountHomePresenter.onCreateTeamSelect());

                    view = accountTeamRowView1;
                    break;
            }

            if (view != null) {
                view.setTag(team);
                teamLayout.addView(view);
            }
        }
    }

    @Override
    public void loadTeamCreateActivity() {
        TeamDomainInfoActivity_.intent(AccountHomeActivity.this)
                .startForResult(REQ_TEAM_CREATE);
    }

    @Override
    public void showNameEditDialog(String oldName) {
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                EditTextDialogFragment.ACTION_MODIFY_PROFILE_ACCOUNT_NAME, oldName);
        newFragment.show(getFragmentManager(), "dialog");
    }

    @UiThread
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(AccountHomeActivity.this, message);
    }

    @UiThread
    @Override
    public void setAccountName(String newName) {
        accountNameTextView.setText(newName);
    }

    @UiThread
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread
    @Override
    public void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread
    @Override
    public void moveSelectedTeam(boolean firstJoin) {
        JandiSocketService.stopService(AccountHomeActivity.this);
//        JandiSocketService.startSocketServiceIfStop(AccountHomeActivity.this);
//        sendBroadcast(new Intent(SocketServiceBroadcastReceiver.START_SOCKET_SERVICE));

        MainTabActivity_.intent(AccountHomeActivity.this)
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .start();

        if (firstJoin) { // 초대 수락 또는 팀 생성 후
            MemberProfileActivity_.intent(AccountHomeActivity.this)
                    .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                    .start();
        }

        finish();
    }

    @Override
    public void moveEmailEditClick() {
        EmailChooseActivity_.intent(AccountHomeActivity.this)
                .startForResult(REQ_EMAIL_CHOOSE);
    }

    @Override
    public void setUserEmailText(String email) {
        emailTextView.setText(email);
    }

    @Override
    public void moveCreatedTeamDomain(Team selectedTeam) {
        TeamDomainInfoActivity_.intent(AccountHomeActivity.this)
                .teamId(selectedTeam.getTeamId())
                .teamName(selectedTeam.getName())
                .domain(selectedTeam.getTeamDomain())
                .token(selectedTeam.getToken())
                .startForResult(REQ_TEAM_JOIN);
    }

    @UiThread
    @Override
    public void removePendingTeamView(Team selectedTeam) {

        for (int idx = teamLayout.getChildCount() - 2; idx >= 0; --idx) {
            Team team = (Team) teamLayout.getChildAt(idx).getTag();
            if (team.getStatus() == Team.Status.PENDING && team.getTeamId() == selectedTeam.getTeamId()) {
                teamLayout.removeViewAt(idx);
                break;
            }
        }
    }

    @Override
    public void showHelloDialog() {

        View customView = LayoutInflater.from(AccountHomeActivity.this).inflate(R.layout.dialog_account_home_help, null);
        AlertDialog alertDialog = new AlertDialog.Builder(AccountHomeActivity.this)
                .setView(customView)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create();

        if (customView.getParent() != null) {
            ((View) customView.getParent()).setPadding(0, 0, 0, 0);
        }
        customView.setPadding(0, 0, 0, 0);
        alertDialog.show();
        View buttonPanel = alertDialog.getWindow().getDecorView().findViewById(android.support.v7.appcompat.R.id.buttonPanel);
        if (buttonPanel != null) {
            buttonPanel.setBackgroundColor(getResources().getColor(R.color.white));
        }
    }

    @Override
    @UiThread
    public void showTextAlertDialog(String msg, DialogInterface.OnClickListener clickListener) {
        new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        clickListener)
                .create().show();
    }

    @Override
    public void moveAfterinvitaionAccept() {
        accountHomePresenter.onTeamCreateAcceptResult();
    }


    public void onEvent(ConfirmModifyProfileEvent event) {
        accountHomePresenter.onChangeName(event.inputMessage);
    }

    @OnActivityResult(REQ_TEAM_CREATE)
    void onTeamCreateResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            accountHomePresenter.onTeamCreateAcceptResult();
        }
    }

    @OnActivityResult(REQ_EMAIL_CHOOSE)
    void onEmailChooseeResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            accountHomePresenter.onEmailChooseResult();
        }
    }

}
