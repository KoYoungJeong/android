package com.tosslab.jandi.app.ui.team.create;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.team.create.teaminfo.InsertTeamInfoFragment;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

/**
 * Created by tee on 2016. 9. 29..
 */

public class CreateTeamForNewAccountActivity extends BaseAppCompatActivity {

    public static final String TAG_FRAGMENT = "CREATE_TEAM_FRAGMENT";

    @InjectExtra
    boolean isFirstExecution = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this);
        Bundle bundle = new Bundle();
        bundle.putInt(InsertTeamInfoFragment.MODE, InsertTeamInfoFragment.MODE_FROM_ACCOUNT_HOME);
        bundle.putBoolean(InsertTeamInfoFragment.IS_FIRST_EXECUTION, isFirstExecution);
        Fragment fragment = new InsertTeamInfoFragment();
        fragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, fragment, TAG_FRAGMENT)
                .commit();

        if (isFirstExecution) {
            AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.ForceTeamCreation);
        }
    }
}
