package com.tosslab.jandi.app.ui.maintab.more;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.TextView;

import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.InvitationDisableCheckEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.ui.account.AccountHomeActivity_;
import com.tosslab.jandi.app.ui.maintab.more.view.IconWithTextView;
import com.tosslab.jandi.app.ui.members.MembersListActivity;
import com.tosslab.jandi.app.ui.members.MembersListActivity_;
import com.tosslab.jandi.app.ui.profile.member.MemberProfileActivity_;
import com.tosslab.jandi.app.ui.settings.SettingsActivity_;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity;
import com.tosslab.jandi.app.ui.starmention.StarMentionListActivity_;
import com.tosslab.jandi.app.ui.team.info.model.TeamDomainInfoModel;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;
import com.tosslab.jandi.app.utils.IonCircleTransform;
import com.tosslab.jandi.app.utils.LanguageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
@EFragment(R.layout.fragment_main_more)
public class MainMoreFragment extends Fragment {

    protected static final String SUPPORT_URL_KO = "https://jandi.zendesk.com/hc/ko";
    protected static final String SUPPORT_URL_JA = "https://jandi.zendesk.com/hc/ja";
    protected static final String SUPPORT_URL_ZH_CH = "https://jandi.zendesk.com/hc/zh-cn";
    protected static final String SUPPORT_URL_ZH_TW = "https://jandi.zendesk.com/hc/zh-tw";
    protected static final String SUPPORT_URL_EN = "https://jandi.zendesk.com/hc/en-us";

    protected Context mContext;

    IconWithTextView profileIconView;

    @Bean
    TeamDomainInfoModel teamDomainInfoModel;

    @ViewById(R.id.txt_more_jandi_version)
    TextView textViewJandiVersion;

    private EntityManager mEntityManager;

    @AfterInject
    void init() {
        LogUtil.d("MainMoreFragment");
        mContext = getActivity();
        mEntityManager = EntityManager.getInstance();
    }

    @AfterViews
    void initView() {
        LogUtil.d("initView MainMoreFragment");

        profileIconView = (IconWithTextView) getView().findViewById(R.id.ly_more_profile);

        showJandiVersion();
    }

    @Override
    public void onResume() {
        LogUtil.d("MainMoreFragment onResume");
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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.clear();
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
        MembersListActivity_.intent(mContext)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .type(MembersListActivity.TYPE_MEMBERS_LIST_TEAM)
                .start();
    }

    @Click(R.id.ly_more_invite)
    public void onInvitationDisableCheck() {
        EventBus.getDefault().post(new InvitationDisableCheckEvent());
    }

    @Click(R.id.ly_more_go_to_main)
    public void moveToAccountActivity() {
        AccountHomeActivity_.intent(mContext)
                .start();
    }

    @Click(R.id.rl_more_setting)
    public void moveToSettingActivity() {
        SettingsActivity_.intent(mContext)
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .start();
    }

    @Click(R.id.rl_more_help)
    public void launchHelpPageOnBrowser() {
        InternalWebActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .url(getSupportUrlEachLanguage())
                .hideActionBar(true)
                .helpSite(true)
                .start();
    }

    @Click(R.id.ly_more_mentioned)
    public void launchHelpPageOnMentioned() {
        StarMentionListActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .extra("type", StarMentionListActivity.TYPE_MENTION_LIST)
                .start();
    }

    @Click(R.id.ly_more_starred)
    public void launchHelpPageOnStarred() {
        StarMentionListActivity_.intent(getActivity())
                .flags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
                .extra("type", StarMentionListActivity.TYPE_STAR_LIST)
                .start();
    }

    String getSupportUrlEachLanguage() {
        String language = LanguageUtil.getLanguage(mContext);
        String supportUrl;
        if (TextUtils.equals(language, LanguageUtil.LANG_KO)) {
            supportUrl = SUPPORT_URL_KO;
        } else if (TextUtils.equals(language, LanguageUtil.LANG_JA)) {
            supportUrl = SUPPORT_URL_EN; //일본어 컨텐츠가 없어서 영어버전 사용
        } else if (TextUtils.equals(language, LanguageUtil.LANG_ZH_CN)) {
            supportUrl = SUPPORT_URL_ZH_CH;
        } else if (TextUtils.equals(language, LanguageUtil.LANG_ZH_TW)) {
            supportUrl = SUPPORT_URL_ZH_TW;
        } else {
            supportUrl = SUPPORT_URL_EN;
        }
        return supportUrl;
    }
}