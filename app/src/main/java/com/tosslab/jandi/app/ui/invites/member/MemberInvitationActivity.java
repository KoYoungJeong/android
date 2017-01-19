package com.tosslab.jandi.app.ui.invites.member;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.invites.emails.InviteEmailActivity;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 2016. 12. 8..
 */

public class MemberInvitationActivity extends BaseAppCompatActivity {

    public static final String INVITE_URL_KAKAO = "com.kakao.talk";
    public static final String INVITE_URL_LINE = "jp.naver.line.android";
    public static final String INVITE_URL_WECHAT = "com.tencent.mm";
    public static final String INVITE_URL_FACEBOOK_MESSENGER = "com.facebook.orca";
    public static final String INVITE_FACEBOOK_EXTRA_PROTOCOL_VERSION = "com.facebook.orca.extra.PROTOCOL_VERSION";
    public static final String INVITE_FACEBOOK_EXTRA_APP_ID = "com.facebook.orca.extra.APPLICATION_ID";
    public static final String INVITE_FACEBOOK_REGISTRATION_APP_ID = "808900692521335";
    public static final int INVITE_FACEBOOK_PROTOCOL_VERSION = 20150314;

    @Bind(R.id.tv_invitation_url_area)
    TextView tvInvitationUrl;

    private String invitationUrl;
    private ClipboardManager clipboardManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite_member);
        ButterKnife.bind(this);
        initViews();
        clipboardManager = (ClipboardManager) getApplicationContext().getSystemService(Context.CLIPBOARD_SERVICE);
    }

    private void initViews() {
        setActionBar();
        invitationUrl = TeamInfoLoader.getInstance().getInvitationUrl();
        tvInvitationUrl.setText(invitationUrl);
    }

    private void setActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_search_bar);
        setSupportActionBar(toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void startInvitation(String packageName) {
        Intent intent = getInviteIntent(packageName);
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            copyLink();
            showTextDialog(getApplicationContext()
                    .getResources().getString(R.string.jandi_invite_app_not_installed));
        }
    }

    private Intent getInviteIntent(String packageName) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setPackage(packageName);
        intent.putExtra(Intent.EXTRA_TEXT, getInvitationContents() + "\n" + invitationUrl);
        intent.setType("text/plain");
        if (packageName.equals(INVITE_URL_FACEBOOK_MESSENGER)) {
            intent.putExtra(INVITE_FACEBOOK_EXTRA_PROTOCOL_VERSION,
                    INVITE_FACEBOOK_PROTOCOL_VERSION);
            intent.putExtra(INVITE_FACEBOOK_EXTRA_APP_ID,
                    INVITE_FACEBOOK_REGISTRATION_APP_ID);
            intent.setType("image/*");
        }
        return intent;
    }

    public String getInvitationContents() {
        StringBuffer buffer = new StringBuffer();
        buffer.append(TeamInfoLoader.getInstance().getTeamName())
                .append(" ")
                .append(getResources().getString(R.string.jandi_invite_contents));
        return buffer.toString();
    }

    @OnClick(R.id.vg_invite_email)
    void onClickInviteEmail() {
        InviteEmailActivity.startActivityForMember(this);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteMember,
                AnalyticsValue.Action.Email);
    }

    @OnClick(R.id.vg_invite_kakaotalk)
    void onClickInviteKakaoTalk() {
        startInvitation(INVITE_URL_KAKAO);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteMember,
                AnalyticsValue.Action.InviteMember_KakaoTalk);
    }

    @OnClick(R.id.vg_invite_line)
    void onClickInviteLine() {
        startInvitation(INVITE_URL_LINE);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteMember,
                AnalyticsValue.Action.InviteMember_Line);
    }

    @OnClick(R.id.vg_invite_facebook)
    void onClickInviteFacebook() {
        startInvitation(INVITE_URL_FACEBOOK_MESSENGER);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteMember,
                AnalyticsValue.Action.InviteMember_FBMessenger);
    }

    @OnClick(R.id.vg_invite_wechat)
    void onClickInviteWechat() {
        startInvitation(INVITE_URL_WECHAT);
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteMember,
                AnalyticsValue.Action.InviteMember_WeChat);
    }

    @OnClick(R.id.tv_copy_invitation_url_button)
    void onClickCopyInvitationUrl() {
        copyLink();
    }

    private void copyLink() {
        ClipData clipData = ClipData.newPlainText("", invitationUrl);
        clipboardManager.setPrimaryClip(clipData);
        showTextDialog(getResources()
                .getString(R.string.jandi_invite_succes_copy_link));
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.InviteMember,
                AnalyticsValue.Action.InviteMember_CopyLink);
    }

    public void showTextDialog(String alertText) {
        new AlertDialog.Builder(this, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton(getApplicationContext()
                                .getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> dialog.dismiss())
                .create().show();
    }

}