package com.tosslab.jandi.app.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.LogoutEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.utils.CircleTransform;
import com.tosslab.jandi.app.utils.JandiNetworkException;
import com.tosslab.jandi.app.utils.JandiPreference;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 10. 11..
 */
@EFragment(R.layout.fragment_main_more)
public class MainMoreFragment extends Fragment {

    @ViewById(R.id.img_more_user_profile)
    ImageView imageViewUserProfile;
    @ViewById(R.id.txt_more_jandi_version)
    TextView textViewJandiVersion;

    protected Context mContext;
    private EntityManager mEntityManager;

    @AfterInject
    void init() {
        mContext = getActivity();
        mEntityManager = ((JandiApplication)getActivity().getApplication()).getEntityManager();
    }

    @AfterViews
    void initView() {
        showUserProfile();
        showJandiVersion();
    }

    private void showUserProfile() {
        if (mEntityManager != null) {
            FormattedEntity me = mEntityManager.getMe();
            Picasso.with(mContext)
                    .load(me.getUserSmallProfileUrl())
                    .placeholder(R.drawable.jandi_profile)
                    .transform(new CircleTransform())
                    .into(imageViewUserProfile);
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
    public void moveToInvitationActivity() {
        runActivityWithDelay(new Runnable() {
            @Override
            public void run() {
                TeamInfoActivity_.intent(mContext)
                        .flags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .start();
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

    @Click(R.id.ly_more_logout)
    public void signOut() {
        // MainTabActivity 에서 로그아웃 처리.
        EventBus.getDefault().post(new LogoutEvent());
    }
}
