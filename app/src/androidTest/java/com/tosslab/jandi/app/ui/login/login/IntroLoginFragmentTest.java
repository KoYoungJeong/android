package com.tosslab.jandi.app.ui.login.login;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.SpannableStringBuilder;
import android.view.inputmethod.InputMethodManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withHint;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by jsuch2362 on 15. 11. 10..
 */
@RunWith(AndroidJUnit4.class)
public class IntroLoginFragmentTest {
    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<BaseAppCompatActivity>(BaseAppCompatActivity.class);
    private IntroLoginFragment fragment;

    @Before
    public void setUp() throws Exception {

        android.app.FragmentManager fragmentManager = rule.getActivity().getFragmentManager();
        fragment = IntroLoginFragment_.builder().build();
        fragmentManager.beginTransaction()
                .add(android.R.id.content, fragment)
                .commit();
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }


    @Test
    public void testCheckValidEmail() throws Throwable {
        rule.runOnUiThread(() -> fragment.checkValidEmail(new SpannableStringBuilder("steve@")));
        onView(withId(R.id.btn_intro_action_signin_start))
                .check(matches(not(isEnabled())));

        rule.runOnUiThread(() -> fragment.checkValidEmail(new SpannableStringBuilder(BaseInitUtil.TEST_EMAIL)));
        onView(withId(R.id.btn_intro_action_signin_start))
                .check(matches(isEnabled()));
    }

    @Test
    public void testCheckValidPassword() throws Throwable {
        rule.runOnUiThread(() -> fragment.checkValidPassword(new SpannableStringBuilder("1234")));
        onView(withId(R.id.btn_intro_action_signin_start))
                .check(matches(isEnabled()));

        rule.runOnUiThread(() -> fragment.checkValidPassword(new SpannableStringBuilder("")));
        onView(withId(R.id.btn_intro_action_signin_start))
                .check(matches(not(isEnabled())));
    }

    @Test
    public void testOnClickSignUp() throws Exception {
        fragment.onClickSignUp();

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        onView(withText(R.string.jandi_signup_title))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testOnClickForgotPassword() throws Throwable {
        rule.runOnUiThread(() -> fragment.onClickForgotPassword());

        onView(withHint(R.string.jandi_user_id))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testHideKeyboard() throws Throwable {
        rule.runOnUiThread(() -> fragment.hideKeyboard());


        InputMethodManager imm = (InputMethodManager) JandiApplication.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        assertThat(imm.isAcceptingText(), is(false));
    }
}