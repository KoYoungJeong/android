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
import com.tosslab.jandi.app.dialogs.TextDialog;
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
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;
import rx.Observable;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
@EFragment(R.layout.fragment_main_more)
public class MainMoreFragment extends Fragment {

    public static final String SUPPORT_URL = "http://support.jandi.com";
    public static final String PACKAGE_NAME_KAKAO = "com.kakao.talk";
    public static final String PACKAGE_NAME_LINE = "jp.naver.line.android";
    public static final String PACKAGE_NAME_WECHAT = "com.tencent.mm";
    public static final String FACEBOOK_EXTRA_PROTOCOL_VERSION = "com.facebook.orca.extra.PROTOCOL_VERSION";
    public static final String FACEBOOK_EXTRA_APP_ID = "com.facebook.orca.extra.APPLICATION_ID";
    public static final int FACEBOOK_PROTOCOL_VERSION = 20150314;
    public static final String FACEBOOK_REGISTRATION_APP_ID = "808900692521335";
    public static final String PACKAGE_NAME_FACEBOOK_MESSENGER = "com.facebook.orca";
    protected Context mContext;

    IconWithTextView profileIconView;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @SystemService
    ClipboardManager clipboardManager;

    @ViewById(R.id.txt_more_jandi_version)
    TextView textViewJandiVersion;

    private EntityManager mEntityManager;

    private String invitationUrl;
    private String teamName;
    private ProgressWheel progressWheel;

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
    public void onInvitationDisableCheck() {

        showProgressWheel();

        List<FormattedEntity> users = EntityManager.getInstance(getActivity()).getFormattedUsers();
        FormattedEntity tempDefaultEntity = new FormattedEntity();
        FormattedEntity owner = Observable.from(users)
                .filter(formattedEntity -> TextUtils.equals(formattedEntity.getUser().u_authority, "owner"))
                .firstOrDefault(tempDefaultEntity)
                .toBlocking()
                .first();

        try {
            ResTeamDetailInfo.InviteTeam resTeamDetailInfo = teamDomainInfoModel.getTeamInfo(mEntityManager.getTeamId());

            String invitationStatus = resTeamDetailInfo.getInvitationStatus();
            invitationUrl = resTeamDetailInfo.getInvitationUrl();
            teamName = resTeamDetailInfo.getName();

            if (TextUtils.equals(invitationStatus, "enabled")) {
                moveToInvitationActivity();
            } else {
                showTextDialog(getResources().getString(R.string.jandi_invite_disabled, owner.getUser().name));
            }
        } catch (JandiNetworkException e) {
            e.printStackTrace();
            ColoredToast.showError(mContext, getResources().getString(R.string.jandi_invite_succes_copy_link));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            dismissProgresWheel();
        }
    }

    @UiThread
    void dismissProgresWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread
    void showProgressWheel() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
            progressWheel.init();
        }

        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @UiThread
    public void showTextDialog(String alertText) {
        TextDialog textDialog = new TextDialog(mContext);
        textDialog.showDialog(alertText);
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

    public void snsAndMessengerInvitation(Intent intent, String publicLink, String invitationContents, String appPackageName, boolean facebookMessenger) {
        if (facebookMessenger) {
            intent.putExtra(FACEBOOK_EXTRA_PROTOCOL_VERSION, FACEBOOK_PROTOCOL_VERSION);
            intent.putExtra(FACEBOOK_EXTRA_APP_ID, FACEBOOK_REGISTRATION_APP_ID);
        }

        intent.setPackage(appPackageName);
        intent.putExtra(Intent.EXTRA_TEXT, invitationContents + "\n" + publicLink);
        intent.setType("text/plain");

        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
            copyLink(publicLink, invitationContents);
            showTextDialog(getResources().getString(R.string.jandi_invite_app_not_installed));
        }
    }

    public void copyLink(String publicLink, String invitationContents) {
        ClipData clipData = ClipData.newPlainText("", invitationContents + "\n" + publicLink);
        clipboardManager.setPrimaryClip(clipData);
    }


    public void onEvent(TeamInvitationsEvent event) {
        String publicLink = invitationUrl;
        String invitationContents = teamName + getResources().getString(R.string.jandi_invite_contents);
        Intent intent = new Intent(Intent.ACTION_SEND);

        switch (event.type) {
            case JandiConstants.TYPE_INVITATION_EMAIL:
                InviteActivity_.intent(MainMoreFragment.this)
                        .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .start();
                break;
            case JandiConstants.TYPE_INVITATION_KAKAO:
                snsAndMessengerInvitation(intent, publicLink, invitationContents, PACKAGE_NAME_KAKAO, false);
                break;
            case JandiConstants.TYPE_INVITATION_LINE:
                snsAndMessengerInvitation(intent, publicLink, invitationContents, PACKAGE_NAME_LINE, false);
                break;
            case JandiConstants.TYPE_INVITATION_WECHAT:
                snsAndMessengerInvitation(intent, publicLink, invitationContents, PACKAGE_NAME_WECHAT, false);
                break;
            case JandiConstants.TYPE_INVITATION_FACEBOOK_MESSENGER:
                snsAndMessengerInvitation(intent, publicLink, invitationContents, PACKAGE_NAME_FACEBOOK_MESSENGER, true);
                break;
            case JandiConstants.TYPE_INVITATION_COPY_LINK:
                copyLink(publicLink, invitationContents);
                showTextDialog(getResources().getString(R.string.jandi_invite_succes_copy_link));
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
