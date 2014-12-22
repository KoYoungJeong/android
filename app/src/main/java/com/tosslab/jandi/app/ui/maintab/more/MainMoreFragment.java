package com.tosslab.jandi.app.ui.maintab.more;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.text.Html;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.ui.ProfileActivity_;
import com.tosslab.jandi.app.ui.TeamInfoActivity_;
import com.tosslab.jandi.app.ui.maintab.more.view.IconWithTextView;
import com.tosslab.jandi.app.ui.settings.SettingsActivity_;
import com.tosslab.jandi.app.ui.team.select.TeamSelectionActivity;
import com.tosslab.jandi.app.ui.team.select.TeamSelectionActivity_;
import com.tosslab.jandi.app.utils.GlideCircleTransform;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
@EFragment(R.layout.fragment_main_more)
public class MainMoreFragment extends Fragment {

    private final static Logger logger = Logger.getLogger(MainMoreFragment.class);

    protected Context mContext;

    IconWithTextView profileIconView;


    @ViewById(R.id.txt_more_jandi_version)
    TextView textViewJandiVersion;
    @Bean
    MainMoreFragPresenter mainMoreFragPresenter;
    private EntityManager mEntityManager;

    @AfterInject
    void init() {
        mContext = getActivity();
        mEntityManager = ((JandiApplication) getActivity().getApplication()).getEntityManager();
    }

    @AfterViews
    void initView() {

        profileIconView = (IconWithTextView) getView().findViewById(R.id.ly_more_profile);
        TextView logoutView = (TextView) getView().findViewById(R.id.txt_more_jandi_logout);
        logoutView.setText(Html.fromHtml(String.format("<u>%s</u>", logoutView.getText().toString())));

        showUserProfile();
        showJandiVersion();
    }

    private void showUserProfile() {
        if (mEntityManager != null) {
            FormattedEntity me = mEntityManager.getMe();
            Glide.with(mContext)
                    .load(me.getUserSmallProfileUrl())
                    .placeholder(R.drawable.jandi_profile)
                    .transform(new GlideCircleTransform(mContext))
                    .into(profileIconView.getImageView());
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

    @Click(R.id.ly_more_account)
    public void moveToAccountInfoActivity() {
        runActivityWithDelay(new Runnable() {
            @Override
            public void run() {
                logger.debug("Move to Account Info");
            }
        });
    }

    @Click(R.id.ly_more_profile)
    public void moveToProfileActivity() {
        runActivityWithDelay(new Runnable() {
            @Override
            public void run() {
                ProfileActivity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .start();
            }
        });
    }

    @Click(R.id.ly_more_setting)
    public void moveToSettingActivity() {
        runActivityWithDelay(new Runnable() {
            @Override
            public void run() {
                SettingsActivity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .start();
            }
        });
    }

    @Click(R.id.ly_more_team)
    public void moveToTeamSelectActivity() {
        runActivityWithDelay(new Runnable() {
            @Override
            public void run() {
                TeamSelectionActivity_.intent(mContext)
                        .calledType(TeamSelectionActivity.CALLED_CHANGE_TEAM)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .start();
            }
        });
    }

    @Click(R.id.ly_more_team_member)
    public void moveToTeamMemberActivity() {
        runActivityWithDelay(new Runnable() {
            @Override
            public void run() {
                TeamInfoActivity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .start();
            }
        });
    }

    @Click(R.id.ly_more_invite)
    public void moveToInvitationActivity() {
        runActivityWithDelay(new Runnable() {
            @Override
            public void run() {
                logger.debug("Move to Invitation Activity");
            }
        });

    }

    @Click(R.id.ly_more_help)
    public void launchHelpPageOnBrowser() {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW,
                Uri.parse("http://support.jandi.com"));
        startActivity(browserIntent);
    }


    private void runActivityWithDelay(Runnable runnable) {
        Handler handler = new Handler();
        handler.postDelayed(runnable, 250);
    }


    @Click(R.id.txt_more_jandi_logout)
    public void signOut() {

        mainMoreFragPresenter.showLogoutDialog();

    }

}
