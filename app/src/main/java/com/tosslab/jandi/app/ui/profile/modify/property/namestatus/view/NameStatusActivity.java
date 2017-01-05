package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

public class NameStatusActivity extends BaseAppCompatActivity {

    public static final int EXTRA_TYPE_NAME_FOR_TEAM_PROFILE = 1;
    public static final int EXTRA_TYPE_NAME_FOR_MAIN_ACCOUNT = 2;
    public static final int EXTRA_TYPE_STATUS = 3;

    @Nullable
    @InjectExtra
    long memberId = -1;

    @Nullable
    @InjectExtra
    int type = EXTRA_TYPE_NAME_FOR_TEAM_PROFILE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dart.inject(this);

        Fragment fragment;
        if (type == EXTRA_TYPE_NAME_FOR_TEAM_PROFILE) {
            fragment = Fragment.instantiate(this, NameChangeFragment.class.getName());
            Bundle bundle = new Bundle();
            bundle.putInt(NameChangeFragment.NAME_CHANGE_MODE, NameChangeFragment.MODE_FROM_TEAM_PROFILE);
            bundle.putLong(NameChangeFragment.ARG_MEMBER_ID, memberId);
            fragment.setArguments(bundle);
        } else if (type == EXTRA_TYPE_NAME_FOR_MAIN_ACCOUNT) {
            fragment = Fragment.instantiate(this, NameChangeFragment.class.getName());
            Bundle bundle = new Bundle();
            bundle.putInt(NameChangeFragment.NAME_CHANGE_MODE, NameChangeFragment.MODE_FROM_MAIN_ACCOUNT);
            fragment.setArguments(bundle);
        } else {
            fragment = Fragment.instantiate(this, StatusChangeFragment.class.getName());
            Bundle bundle = new Bundle();
            bundle.putLong(StatusChangeFragment.ARG_MEMBER_ID, memberId);
            fragment.setArguments(bundle);
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment, fragment.getClass().getName())
                .commit();
    }

}
