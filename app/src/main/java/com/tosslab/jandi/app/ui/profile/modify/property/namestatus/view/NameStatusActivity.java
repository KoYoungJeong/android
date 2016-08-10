package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

public class NameStatusActivity extends BaseAppCompatActivity {

    public static final int EXTRA_TYPE_NAME = 1;
    public static final int EXTRA_TYPE_STATUS = 2;

    @InjectExtra
    int type = EXTRA_TYPE_NAME;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dart.inject(this);

        Fragment fragment;
        if (type == EXTRA_TYPE_NAME) {
            fragment = Fragment.instantiate(this, NameChangeFragment.class.getName());
        } else {
            fragment = Fragment.instantiate(this, StatusChangeFragment.class.getName());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, fragment, fragment.getClass().getName())
                .commit();
    }
}
