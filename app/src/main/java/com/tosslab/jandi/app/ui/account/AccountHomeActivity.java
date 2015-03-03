package com.tosslab.jandi.app.ui.account;

import android.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.ui.account.presenter.AccountHomePresenter;
import com.tosslab.jandi.app.ui.account.presenter.AccountHomePresenterImpl;
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
public class AccountHomeActivity extends ActionBarActivity implements AccountHomePresenter.View {

    public static final int REQ_TEAM_CREATE = 101;
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
        actionBar.setTitle(R.string.jandi_account);

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
        // TODO show Help Dailog
    }

    @Click(R.id.img_account_main_edit)
    void onNameEditClick() {
        accountHomePresenter.onAccountNameEditClick(accountNameTextView.getText().toString());
    }

    @UiThread
    @Override
    public void showErrorToast(String message) {
        ColoredToast.showError(AccountHomeActivity.this, message);
    }

    @UiThread
    @Override
    public void setTeamInfo(ArrayList<Team> result) {

        for (Team team : result) {
            View view = null;

            switch (team.getStatus()) {
                case JOINED:
                    AccountTeamRowView accountTeamRowView = new AccountTeamRowView(AccountHomeActivity.this);
                    accountTeamRowView.setBadgeCount(team.getUnread());
                    accountTeamRowView.setTeamName(team.getName());
                    accountTeamRowView.setOnClickListener(v -> {
                        Team clickedTeam = (Team) v.getTag();
                        accountHomePresenter.onJoinedTeamSelect(clickedTeam);
                    });
                    view = accountTeamRowView;
                    break;
                case PENDING:
                    AccountPendingTeamRowView accountPendingTeamRowView = new AccountPendingTeamRowView(AccountHomeActivity.this);
                    view = accountPendingTeamRowView;
                    break;
                case CREATE:
                    AccountTeamRowView accountTeamRowView1 = new AccountTeamRowView(AccountHomeActivity.this);
                    accountTeamRowView1.setTeamName(getString(R.string.team_create));
                    accountTeamRowView1.setIcon(R.drawable.jandi_icon_teamlist_add);
                    accountTeamRowView1.setNameTextColor(getResources().getColorStateList(R.color.text_color_green));
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

    public void onEvent(ConfirmModifyProfileEvent event) {
        accountHomePresenter.onChangeName(event.inputMessage);
    }
}
