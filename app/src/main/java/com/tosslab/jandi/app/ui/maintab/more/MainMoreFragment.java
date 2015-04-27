package com.tosslab.jandi.app.ui.maintab.more;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.InvitationDialogFragment;
import com.tosslab.jandi.app.events.team.invite.TeamInvitationsEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.invites.InviteActivity_;
import com.tosslab.jandi.app.ui.maintab.more.view.IconWithTextView;
import com.tosslab.jandi.app.ui.member.TeamInfoActivity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.ui.settings.SettingsActivity_;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.IonCircleTransform;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.SystemService;
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

    @SystemService
    ClipboardManager clipboardManager;

    @ViewById(R.id.txt_more_jandi_version)
    TextView textViewJandiVersion;

    private EntityManager mEntityManager;

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
    public void moveToInvitationActivity() {
        /*InviteActivity_.intent(MainMoreFragment.this)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();*/

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


    public void onEvent(TeamInvitationsEvent event) {
        switch (event.type) {
            case JandiConstants.TYPE_INVITATION_EMAIL:
                logger.info("TeamInvitationsEvent : from email");
                InviteActivity_.intent(MainMoreFragment.this)
                        .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                        .start();
                //messageListPresenter.openAlbumForActivityResult(MainMoreFragment.this);
                break;
            case JandiConstants.TYPE_INVITATION_KAKAO:
                logger.info("TeamInvitationsEvent : from kakao");
                //messageListPresenter.openCameraForActivityResult(MessageListFragment.this);
                break;
            case JandiConstants.TYPE_INVITATION_LINE:
                logger.info("TeamInvitationsEvent : from LINE");
                //messageListPresenter.openExplorerForActivityResult(MessageListFragment.this);
                break;
            case JandiConstants.TYPE_INVITATION_WECHAT:
                logger.info("TeamInvitationsEvent : from WeChat");
                //messageListPresenter.openExplorerForActivityResult(MessageListFragment.this);
                break;
            case JandiConstants.TYPE_INVITATION_FACEBOOK_MESSENGER:
                logger.info("TeamInvitationsEvent : from facebook messenger");
                //messageListPresenter.openExplorerForActivityResult(MessageListFragment.this);
                break;
            case JandiConstants.TYPE_INVITATION_COPY_LINK:
                logger.info("TeamInvitationsEvent : from copy link , teamName : " + mEntityManager.getTeamName());
                //messageListPresenter.openExplorerForActivityResult(MessageListFragment.this);
                ClipData clipData = ClipData.newPlainText("", "abcdefg");
                clipboardManager.setPrimaryClip(clipData);

                ColoredToast.show(mContext, getResources().getString(R.string.jandi_invite_succes_copy_link));
                break;
            default:
                break;

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }
}
