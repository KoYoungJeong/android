package com.tosslab.jandi.app.ui.login.login.viewmodel;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.inputmethod.InputMethodManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.login.login.IntroLoginFragment;
import com.tosslab.jandi.app.ui.login.login.IntroLoginFragment_;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

/**
 * Created by jsuch2362 on 15. 11. 10..
 */
@RunWith(AndroidJUnit4.class)
public class IntroLoginViewModelTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<>(BaseAppCompatActivity.class);
    private IntroLoginViewModel introLoginViewModel;

    @Before
    public void setUp() throws Exception {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        android.app.FragmentManager fragmentManager = rule.getActivity().getFragmentManager();
        IntroLoginFragment fragment = IntroLoginFragment_.builder().build();
        fragmentManager.beginTransaction()
                .add(android.R.id.content, fragment)
                .commit();

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        introLoginViewModel = fragment.introLoginViewModel;
    }

    @Test
    public void testShowProgressDialog() throws Exception {

        introLoginViewModel.showProgressDialog();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(introLoginViewModel.mProgressWheel.isShowing(), is(true));
    }

    @Test
    public void testInitObject() throws Exception {
        assertThat(introLoginViewModel.mProgressWheel, is(notNullValue()));
    }

    @Test
    public void testDissmissProgressDialog() throws Exception {

        introLoginViewModel.dissmissProgressDialog();
        assertThat(introLoginViewModel.mProgressWheel.isShowing(), is(false));
    }

    @Test
    public void testCreateTeamSucceed() throws Exception {

        // When
        introLoginViewModel.createTeamSucceed();

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // Then
        assertThat(introLoginViewModel.mProgressWheel.isShowing(), is(false));
        assertThat(introLoginViewModel.activity.isFinishing(), is(true));

    }

    @Test
    public void testCreateTeamFailed() throws Exception {

        // When
        introLoginViewModel.createTeamFailed(R.string.app_name);

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // Then
        assertThat(introLoginViewModel.mProgressWheel.isShowing(), is(false));
    }

    @Test
    public void testLoginSuccess() throws Exception {
        introLoginViewModel.loginSuccess(BaseInitUtil.TEST_ID);

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(introLoginViewModel.mProgressWheel.isShowing(), is(false));
        assertThat(introLoginViewModel.activity.isFinishing(), is(true));

    }

    @Test
    public void testMoveToTeamSelectionActivity() throws Throwable {
        rule.runOnUiThread(() -> introLoginViewModel.moveToTeamSelectionActivity(BaseInitUtil.TEST_ID));

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(introLoginViewModel.activity.isFinishing(), is(true));
    }

    @Test
    public void testLoginFail() throws Exception {
        introLoginViewModel.loginFail(R.string.app_name);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(introLoginViewModel.mProgressWheel.isShowing(), is(false));
    }

    @Test
    public void testGetEmailText() throws Throwable {
        String emailText = introLoginViewModel.getEmailText();
        assertThat(emailText, isEmptyString());

        rule.runOnUiThread(() -> introLoginViewModel.etEmail.setText("hahaha"));

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        emailText = introLoginViewModel.getEmailText();
        assertThat(emailText, is(equalTo("hahaha")));
    }

    @Test
    public void testSetEmailText() throws Throwable {
        rule.runOnUiThread(() -> introLoginViewModel.setEmailText("hahahaha"));

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        assertThat(introLoginViewModel.getEmailText(), is(equalTo("hahahaha")));
    }

    @Test
    public void testGetPasswordText() throws Throwable {
        String passwordText = introLoginViewModel.getPasswordText();
        assertThat(passwordText, isEmptyString());

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        rule.runOnUiThread(() -> introLoginViewModel.etPassword.setText("password"));
        assertThat(introLoginViewModel.getPasswordText(), is(equalTo("password")));
    }

    @Test
    public void testHideKeypad() throws Exception {
        introLoginViewModel.hideKeypad();

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        InputMethodManager imm = (InputMethodManager) JandiApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);

        assertThat(imm.isAcceptingText(), is(false));
    }

    @Test
    public void testSetSignInButtonEnable() throws Throwable {

        rule.runOnUiThread(() -> introLoginViewModel.setSignInButtonEnable(false));
        onView(withText(R.string.jandi_action_login))
                .check(matches(not(isEnabled())));

        rule.runOnUiThread(() -> introLoginViewModel.setSignInButtonEnable(true));
        onView(withText(R.string.jandi_action_login))
                .check(matches(isEnabled()));

    }
}