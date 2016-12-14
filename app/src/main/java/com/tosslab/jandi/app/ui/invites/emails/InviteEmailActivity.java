package com.tosslab.jandi.app.ui.invites.emails;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.invites.emails.adapter.InviteEmailListAdapter;
import com.tosslab.jandi.app.ui.invites.emails.adapter.InviteEmailListAdapterViewModel;
import com.tosslab.jandi.app.ui.invites.emails.dagger.DaggerInviteEmailComponent;
import com.tosslab.jandi.app.ui.invites.emails.dagger.InviteEmailModule;
import com.tosslab.jandi.app.ui.invites.emails.presenter.InviteEmailPresenter;
import com.tosslab.jandi.app.ui.search.filter.room.RoomFilterActivity;
import com.tosslab.jandi.app.utils.ProgressWheelForInvitation;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 2016. 12. 9..
 */

public class InviteEmailActivity extends BaseAppCompatActivity
        implements InviteEmailPresenter.View {

    public static final int REQ_SELECT_TOPIC = 0x011;
    public static final String EXTRA_AUTH_MODE = "auth_mode";
    public static final int EXTRA_INVITE_MEMBER_MODE = 0x01;
    public static final int EXTRA_INVITE_ASSOCIATE_MODE = 0x02;

    @Inject
    InviteEmailPresenter inviteEmailPresenter;

    @Inject
    InviteEmailListAdapterViewModel adapterViewModel;

    @Bind(R.id.vg_root)
    ViewGroup vgRoot;

    @Bind(R.id.vg_select_topic)
    ViewGroup vgSelectTopic;

    @Bind(R.id.et_input_email)
    EditText etInputEmail;

    @Bind(R.id.vg_email_list)
    LinearLayout vgEmailList;

    @Bind(R.id.tv_send_invitation_email_button)
    TextView tvSendInvitationEmailButton;

    @Bind(R.id.tv_add_email_button)
    TextView tvAddEmailButton;

    @Bind(R.id.v_scroll)
    ScrollView vScroll;

    @Bind(R.id.tv_topic_title)
    TextView tvTopicTitle;

    @Bind(R.id.tv_email_input_description)
    TextView tvEmailInputDescription;

    private long selectedTopicId = -1;

    private int mode = EXTRA_INVITE_MEMBER_MODE;
    private ProgressWheelForInvitation progressWheel;

    private boolean sendEmailButtonStateConfirm = false;


    public static void startActivityForMember(Context context) {
        Intent intent = new Intent(context, InviteEmailActivity.class);
        intent.putExtra(EXTRA_AUTH_MODE, EXTRA_INVITE_MEMBER_MODE);
        context.startActivity(intent);
    }

    public static void startActivityForAssociate(Context context) {
        Intent intent = new Intent(context, InviteEmailActivity.class);
        intent.putExtra(EXTRA_AUTH_MODE, EXTRA_INVITE_ASSOCIATE_MODE);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invitaion_email);
        ButterKnife.bind(this);
        Intent intent = getIntent();
        mode = intent.getIntExtra(EXTRA_AUTH_MODE, EXTRA_INVITE_MEMBER_MODE);
        initViews();
    }

    private void initViews() {
        setActionBar();

        InviteEmailListAdapter listAdapter = new InviteEmailListAdapter(vgEmailList, this);

        DaggerInviteEmailComponent.builder()
                .inviteEmailModule(new InviteEmailModule(this, listAdapter))
                .build()
                .inject(this);

        if (mode == EXTRA_INVITE_MEMBER_MODE) {
            vgSelectTopic.setVisibility(View.GONE);
            tvEmailInputDescription.setText(
                    getString(R.string.invite_member_email_desc));
            getSupportActionBar().setTitle(
                    JandiApplication.getContext().getString(R.string.invite_member_option_member_title));
        } else {
            vgSelectTopic.setVisibility(View.VISIBLE);
            tvEmailInputDescription.setText(
                    getString(R.string.invite_associate_email_desc));
            getSupportActionBar().setTitle(
                    JandiApplication.getContext().getString(R.string.invite_member_option_associate_title));
        }

        vgRoot.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            int heightDiff = vgRoot.getRootView().getHeight() - vgRoot.getHeight();
            if (heightDiff > 150) {
                // keyboard is up
                if (mode == EXTRA_INVITE_ASSOCIATE_MODE) {
                    vgSelectTopic.setVisibility(View.GONE);
                }
                tvSendInvitationEmailButton.setVisibility(View.GONE);
                vScroll.smoothScrollTo(0, 0);
            } else {
                // keyboard is down
                if (mode == EXTRA_INVITE_ASSOCIATE_MODE) {
                    vgSelectTopic.setVisibility(View.VISIBLE);
                }
                tvSendInvitationEmailButton.setVisibility(View.VISIBLE);
            }
        });

        adapterViewModel.setInviteCancelListener(view -> {
            adapterViewModel.removeItemView(view);
            inviteEmailPresenter.onInvitedUsersChanged();
        });

        etInputEmail.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                super.onTextChanged(s, start, before, count);
                if (etInputEmail.getCurrentHintTextColor() != 0xff333333) {
                    etInputEmail.setHintTextColor(0xff333333);
                }
                inviteEmailPresenter.setStatusByEmailValid(s.toString());
            }
        });

    }

    @OnClick(R.id.tv_add_email_button)
    void onClickAddEmailButton() {
        String email = etInputEmail.getText().toString();
        if (!TextUtils.isEmpty(email)) {
            inviteEmailPresenter.addEmail(email);
            etInputEmail.setText("");
        }
    }

    @OnClick(R.id.vg_select_topic)
    void onCLickSelectTopic() {
        RoomFilterActivity.startForResultForInvitation(this, REQ_SELECT_TOPIC);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQ_SELECT_TOPIC:
                if (resultCode == RESULT_OK) {
                    long topicId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_ROOM_ID, -1);
                    selectedTopicId = topicId;
                    String topicName = TeamInfoLoader.getInstance().getTopic(topicId).getName();
                    tvTopicTitle.setText(topicName);
                    if (tvTopicTitle.getCurrentTextColor() != 0xff333333) {
                        tvTopicTitle.setTextColor(0xff333333);
                    }
                    inviteEmailPresenter.onTopicSelected();
                }
                break;
        }
    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (vgEmailList.getChildCount() > 0) {
                    showCancelDialog();
                } else {
                    finish();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void enableAddButton(boolean isEnable) {
        if (isEnable) {
            tvAddEmailButton.setBackground(
                    getResources().getDrawable(R.drawable.bg_round_rect_2pt_00ace9));
            tvAddEmailButton.setClickable(true);
        } else {
            tvAddEmailButton.setBackground(
                    getResources().getDrawable(R.drawable.bg_round_rect_2pt_999999));
            tvAddEmailButton.setClickable(false);
        }
    }

    @SuppressLint("StringFormatInvalid")
    @Override
    public void changeContentInvitationButton(int cnt) {
        sendEmailButtonStateConfirm = false;
        if (cnt > 0) {
            tvSendInvitationEmailButton.setText(
                    getString(R.string.invite_email_sendinvitation_withcounts, cnt + ""));
            if (mode == EXTRA_INVITE_ASSOCIATE_MODE && selectedTopicId == -1) {
                tvSendInvitationEmailButton.setBackgroundColor(0xff99def6);
            } else {
                tvSendInvitationEmailButton.setBackgroundColor(0xff00ace9);
            }
        } else {
            tvSendInvitationEmailButton.setText(
                    getString(R.string.invite_email_sendinvitation_disable));
            tvSendInvitationEmailButton.setBackgroundColor(0xff99def6);
        }
    }

    @Override
    public void changeInvitationButtonIfPatiallyFailed() {
        sendEmailButtonStateConfirm = true;
        tvSendInvitationEmailButton.setText(
                getString(R.string.jandi_confirm));
        tvSendInvitationEmailButton.setBackgroundColor(0xff00ace9);
    }

    @OnClick(R.id.tv_send_invitation_email_button)
    public void onClickSendInvitationEmails() {
        if (sendEmailButtonStateConfirm) {
            finish();
            return;
        }

        if (mode == EXTRA_INVITE_MEMBER_MODE) {
            inviteEmailPresenter.startInvitation();
        } else if (mode == EXTRA_INVITE_ASSOCIATE_MODE) {
            inviteEmailPresenter.startInvitationForAssociate(selectedTopicId);
        }
    }

    @Override
    public void setErrorInputSelectedEmail() {
        etInputEmail.setHint(getString(R.string.invite_email_placeholder));
        etInputEmail.setHintTextColor(0xfff15544);
    }

    @Override
    public void setErrorSelectedTopic() {
        tvTopicTitle.setTextColor(0xfff15544);
    }

    @Override
    public void showProgressWheel() {
        dismissProgressWheel();

        if (progressWheel == null) {
            progressWheel = new ProgressWheelForInvitation(this);
        }

        if (!progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showUnkownFailedDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(getString(R.string.invite_email_error_unknown))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> {
                        })
                .create().show();
    }

    @Override
    public void showPartiallyFailedDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(getString(R.string.invite_email_error_partially))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> {
                        })
                .create().show();
    }

    @Override
    public void showSuccessDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(getString(R.string.invite_email_success))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> {
                            finish();
                        })
                .create().show();
    }

    private void showCancelDialog() {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(getString(R.string.invite_email_cancelinvite))
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> {
                            finish();
                        }).setNegativeButton(getResources().getString(R.string.jandi_cancel),
                (dialog, id) -> {
                }).create().show();
    }

}
