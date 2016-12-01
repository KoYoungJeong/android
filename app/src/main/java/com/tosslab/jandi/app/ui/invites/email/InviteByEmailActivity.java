package com.tosslab.jandi.app.ui.invites.email;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.invites.email.adapter.InvitedEmailListAdapter;
import com.tosslab.jandi.app.ui.invites.email.component.DaggerInviteByEmailComponent;
import com.tosslab.jandi.app.ui.invites.email.module.InviteByEmailModule;
import com.tosslab.jandi.app.ui.invites.email.presenter.InviteByEmailPresenter;
import com.tosslab.jandi.app.ui.invites.email.view.InviteByEmailView;
import com.tosslab.jandi.app.ui.invites.email.view.InvitedEmailView;
import com.tosslab.jandi.app.utils.ColoredToast;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
public class InviteByEmailActivity extends BaseAppCompatActivity implements InviteByEmailView {

    @Inject
    InviteByEmailPresenter presenter;
    @Inject
    InvitedEmailView invitedEmailView;

    @Bind(R.id.btn_invite_send)
    Button btnInvite;
    @Bind(R.id.et_invite_email)
    EditText tvEmail;
    @Bind(R.id.lv_invite)
    RecyclerView lvInvitedEmails;
    @Bind(R.id.tv_invite_large_group_support)
    TextView tvInviteLargeGroupSupport;
    @Bind(R.id.vg_invite_success)
    View vSuccessLayout;
    @Bind(R.id.tv_invite_success)
    TextView tvSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedUnLockPassCode(false);

        InvitedEmailListAdapter adapter = new InvitedEmailListAdapter();

        DaggerInviteByEmailComponent.builder()
                .inviteByEmailModule(new InviteByEmailModule(this, adapter))
                .build()
                .inject(this);

        setContentView(R.layout.activity_invite);
        ButterKnife.bind(this);

        setUpActionbar();

        setLargeGroupInviteSupportText();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getBaseContext());
        layoutManager.setAutoMeasureEnabled(true);
        lvInvitedEmails.setLayoutManager(layoutManager);
        lvInvitedEmails.setAdapter(adapter);
    }

    private void setUpActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
        // Set up the action bar.
        ActionBar actionBar = getSupportActionBar();
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @OnTextChanged(R.id.et_invite_email)
    void emailTextChanged(CharSequence text) {
        String email = text.toString();
        presenter.onEmailTextChanged(email);
    }

    @OnClick(R.id.btn_invite_send)
    void inviteListAddClick() {
        presenter.onInviteListAddClick(tvEmail.getText().toString());
    }

    @Override
    public void setEmailTextView(String email) {
        tvEmail.setText(email);
    }

    @Override
    public void setEnableAddButton(boolean isValidEmail) {
        btnInvite.setEnabled(isValidEmail);
    }

    @Override
    public void showInviteFailToast() {
        ColoredToast.showError(R.string.err_invitation_failed);
    }

    @Override
    public void showKickedMemberFailDialog() {
        new AlertDialog.Builder(InviteByEmailActivity.this)
                .setTitle(R.string.team_invite_email_disabledmember_title)
                .setMessage(R.string.team_invite_email_disabledmember_desc)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create()
                .show();
    }

    @Override
    public void showInviteAgainDialog(String email) {
        new AlertDialog.Builder(InviteByEmailActivity.this)
                .setMessage(R.string.jandi_invite_to_dummy_account_again)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    presenter.invite(email);
                })
                .create()
                .show();
    }

    @Override
    public void showAlreadyInTeamToast(String teamName) {
        ColoredToast.showError(getString(R.string.jandi_already_in_team, teamName));
    }

    @Override
    public void notifyDataSetChanged() {
        invitedEmailView.notifyDataSetChanged();
    }

    @Override
    public void notifyItemChanged(int position) {
        invitedEmailView.notifyItemChanged(position);
    }

    @Override
    public void notifyItemInserted(int position) {
        invitedEmailView.notifyItemInserted(position);
    }

    @Override
    public void showInviteSuccessToast() {
        ColoredToast.show(R.string.jandi_invitation_succeed);
    }

    @Override
    public void clearEmailInput() {
        tvEmail.setText("");
    }

    @Override
    public void showSendEmailSuccessView() {
        vSuccessLayout.setVisibility(View.VISIBLE);
    }

    @Override
    public void moveToPosition(int position) {
        lvInvitedEmails.smoothScrollToPosition(position);
    }

    private void setLargeGroupInviteSupportText() {
        String inviteText = getApplicationContext().getString(R.string.jandi_invite_many_people_explain);
        int index = inviteText.indexOf("support");
        SpannableStringBuilder builder = new SpannableStringBuilder(inviteText);
        URLSpan urlSpan = new URLSpan("mailto:" + inviteText.substring(index).toString()) {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(getApplicationContext().getResources().getColor(R.color.jandi_accent_color));
            }
        };
        builder.setSpan(urlSpan, index, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvInviteLargeGroupSupport.setText(builder);
        tvInviteLargeGroupSupport.setMovementMethod(LinkMovementMethod.getInstance());
    }

}
