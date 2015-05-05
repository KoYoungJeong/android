package com.tosslab.jandi.app.ui.maintab.more;

import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Pair;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.invites.InviteUtils;
import com.tosslab.jandi.app.ui.maintab.more.view.IconWithTextView;
import com.tosslab.jandi.app.ui.member.TeamInfoActivity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.ui.settings.SettingsActivity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.IonCircleTransform;
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

    protected Context mContext;

    IconWithTextView profileIconView;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @SystemService
    ClipboardManager clipboardManager;

    private String invitationUrl;
    private String teamName;

    @ViewById(R.id.txt_more_jandi_version)
    TextView textViewJandiVersion;

    private EntityManager mEntityManager;
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

        Pair<InviteUtils.Result, ResTeamDetailInfo.InviteTeam> result =
                InviteUtils.checkInvitationDisabled(teamDomainInfoModel, mEntityManager.getTeamId());

        dismissProgressWheel();

        switch (result.first) {
            case NETWORK_ERROR:
                showErrorToast(getResources().getString(R.string.err_network));
                break;
            case ERROR:
                break;
            case INVITATION_DISABLED:
                showTextDialog(
                        getResources().getString(R.string.jandi_invite_disabled, getOwnerName()));
                break;
            case UNDEFINED_URL:
                showErrorToast(getResources().getString(R.string.err_entity_invite));
                break;
            case SUCCESS:
                moveToInvitationActivity(result.second);
                break;
            default:
                break;
        }
    }

    private String getOwnerName() {
        List<FormattedEntity> users = EntityManager.getInstance(mContext).getFormattedUsers();
        FormattedEntity tempDefaultEntity = new FormattedEntity();
        FormattedEntity owner = Observable.from(users)
                .filter(formattedEntity ->
                        TextUtils.equals(formattedEntity.getUser().u_authority, "owner"))
                .firstOrDefault(tempDefaultEntity)
                .toBlocking()
                .first();
        return owner.getUser().name;
    }

    @UiThread
    public void moveToInvitationActivity(ResTeamDetailInfo.InviteTeam inviteTeam) {
        invitationUrl = inviteTeam.getInvitationUrl();
        teamName = inviteTeam.getName();
        DialogFragment invitationDialog = new InvitationDialogFragment();
        invitationDialog.show(getFragmentManager(), "invitationsDialog");
    }

    public void onEvent(TeamInvitationsEvent event) {
        String invitationContents =
                teamName + getResources().getString(R.string.jandi_invite_contents);
        int eventType = event.type;
        if (eventType == JandiConstants.TYPE_INVITATION_COPY_LINK) {
            copyLink(invitationUrl, invitationContents);
            showTextDialog(getResources().getString(R.string.jandi_invite_succes_copy_link));
        } else {
            Intent intent = InviteUtils.getInviteIntent(
                    mContext, event, invitationUrl, invitationContents);
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                copyLink(invitationUrl, invitationContents);
                showTextDialog(getResources().getString(R.string.jandi_invite_app_not_installed));
            }
        }
    }

    public void copyLink(String publicLink, String contents) {
        ClipData clipData = ClipData.newPlainText("", contents + "\n" + publicLink);
        clipboardManager.setPrimaryClip(clipData);
    }

    @UiThread
    void showErrorToast(String message) {
        ColoredToast.showError(mContext, message);
    }

    @UiThread
    void dismissProgressWheel() {
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
        new AlertDialog.Builder(getActivity())
                .setMessage(alertText)
                .setCancelable(false)
                .setPositiveButton(getResources().getString(R.string.jandi_confirm),
                        (dialog, id) -> dialog.dismiss())
                .create().show();
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

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

}