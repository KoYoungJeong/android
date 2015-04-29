package com.tosslab.jandi.app.ui.maintab.more;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.dialogs.TextViewDialogFragment;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.invites.InviteActivity_;
import com.tosslab.jandi.app.ui.maintab.more.view.IconWithTextView;
import com.tosslab.jandi.app.ui.member.TeamInfoActivity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.ui.settings.SettingsActivity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.JandiNetworkException;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
@EFragment(R.layout.fragment_main_more)
public class MainMoreFragment extends Fragment {

    public static final String SUPPORT_URL = "http://support.jandi.com";
    protected Context mContext;

    IconWithTextView profileIconView;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @SystemService
    ClipboardManager clipboardManager;

    @ViewById(R.id.txt_more_jandi_version)
    TextView textViewJandiVersion;

    private EntityManager mEntityManager;

    private ResTeamDetailInfo.InviteTeam resTeamDetailInfo;

    @AfterInject
    void init() {
        mContext = getActivity();
        mEntityManager = EntityManager.getInstance(getActivity());
    }

    @AfterViews
    void initView() {

        profileIconView = (IconWithTextView) getView().findViewById(R.id.ly_more_profile);

        showJandiVersion();
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        showUserProfile();

    }

    private void showUserProfile() {
        if (mEntityManager != null) {
            FormattedEntity me = mEntityManager.getMe();
            Ion.with(profileIconView.getImageView())
                    .placeholder(R.drawable.jandi_profile)
                    .error(R.drawable.jandi_profile)
                    .transform(new IonCircleTransform())
                    .load(me.getUserSmallProfileUrl());
        }
    }

    private void showJandiVersion() {

        try {
            String packageName = getActivity().getPackageName();
            String versionName = getActivity().getPackageManager().getPackageInfo(packageName, 0).versionName;
            textViewJandiVersion.setText("v." + versionName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Click(R.id.ly_more_profile)
    public void moveToProfileActivity() {
        MemberProfileActivity_.intent(mContext)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();
    }

    @Click(R.id.ly_more_team_member)
    public void moveToTeamMemberActivity() {
        TeamInfoActivity_.intent(mContext)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();
    }

    @Click(R.id.ly_more_invite)
    @Background
    public void invitationDisableCheck() {
        try {
            resTeamDetailInfo = teamDomainInfoModel.getTeamInfo(mEntityManager.getTeamId());

            if (TextUtils.equals(resTeamDetailInfo.getInvitationStatus(), "enabled")) {
                moveToInvitationActivity();
            } else {
                DialogFragment textViewDialog = new TextViewDialogFragment("You are a disabled member of this team");
                textViewDialog.show(getFragmentManager(), "textViewDialog");

            }
        } catch (JandiNetworkException e) {
            e.printStackTrace();
        }
    }

    @UiThread
    public void moveToInvitationActivity() {


        DialogFragment invitationDialog = new InvitationDialogFragment();
        invitationDialog.show(getFragmentManager(), "invitationsDialog");

    }

    @Click(R.id.ly_more_go_to_main)
    public void moveToAccountActivity() {
        AccountHomeActivity_.intent(mContext)
                .start();
    }

    @Click(R.id.ly_more_setting)
    public void moveToSettingActivity() {
        SettingsActivity_.intent(mContext)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();
    }

    @Click(R.id.ly_more_help)
    public void launchHelpPageOnBrowser() {

        InternalWebActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .url(SUPPORT_URL)
                .hideActionBar(true)
                .start();

    }

    public void kakaoLineWechatInvitation(Intent intent, String publicLink, String appPackageName) {
        try {
            intent.setPackage(appPackageName);
            intent.putExtra(Intent.EXTRA_TEXT, publicLink);
            intent.setType("text/plain");
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            copyLink(publicLink);
        }

    }

    public void copyLink(String publicLink) {
        ClipData clipData = ClipData.newPlainText("", publicLink);
        clipboardManager.setPrimaryClip(clipData);

        ColoredToast.show(mContext, getResources().getString(R.string.jandi_invite_succes_copy_link));
    }


    public void onEvent(TeamInvitationsEvent event) {
        String publicLink = resTeamDetailInfo.getInvitationUrl();

        Intent intent = new Intent(Intent.ACTION_SEND);

        switch (event.type) {
            case JandiConstants.TYPE_INVITATION_EMAIL:
                InviteActivity_.intent(MainMoreFragment.this)
                        .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .start();
                break;
            case JandiConstants.TYPE_INVITATION_KAKAO:
                kakaoLineWechatInvitation(intent, publicLink, "com.kakao.talk");
                break;
            case JandiConstants.TYPE_INVITATION_LINE:
                kakaoLineWechatInvitation(intent, publicLink, "jp.naver.line.android");
                break;
            case JandiConstants.TYPE_INVITATION_WECHAT:
                kakaoLineWechatInvitation(intent, publicLink, "com.tencent.mm");
                break;
            case JandiConstants.TYPE_INVITATION_FACEBOOK_MESSENGER:
                String EXTRA_PROTOCOL_VERSION = "com.facebook.orca.extra.PROTOCOL_VERSION";
                String EXTRA_APP_ID = "com.facebook.orca.extra.APPLICATION_ID";
                int PROTOCOL_VERSION = 20150314;
                String YOUR_APP_ID = "432811923545730";

                //String mimeType = "text/plain";
                String mimeType = "image/*";


                intent.setPackage("com.facebook.orca");
                intent.setType(mimeType);
                //intent.putExtra(Intent.EXTRA_STREAM, contentUri);
                intent.putExtra(Intent.EXTRA_TEXT, "[TEST] ABCEFGHI  www.jandi.com");
                intent.putExtra(EXTRA_PROTOCOL_VERSION, PROTOCOL_VERSION);
                intent.putExtra(EXTRA_APP_ID, YOUR_APP_ID);

                startActivity(intent);
                break;
            case JandiConstants.TYPE_INVITATION_COPY_LINK:
                copyLink(publicLink);
                break;
            default:
                break;

        }
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}
