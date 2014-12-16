package com.tosslab.jandi.app.ui.signup;

import android.app.ActionBar;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;

/**
 * Created by justinygchoi on 14. 12. 11..
 */
@EActivity(R.layout.activity_signup)
@OptionsMenu(R.menu.confirm_signup_menu)
public class SignUpActivity extends Activity {
    @Bean
    public SignUpViewModel signUpViewModel;

    @AfterViews
    void init() {
        setUpActionBar();
    }

    private void setUpActionBar() {
        // Set up the action bar.
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @OptionsItem(android.R.id.home)
    void homeSelected() {
        finish();
    }
}
