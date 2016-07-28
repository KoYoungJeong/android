package com.tosslab.jandi.app.ui.profile.modify.view;

import android.app.Fragment;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.inputlist.InputProfileListActivity;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.pressBack;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ModifyProfileActivityTest {

    @Rule
    public IntentsTestRule<ModifyProfileActivity> rule = new IntentsTestRule<ModifyProfileActivity>(ModifyProfileActivity.class, false, false);
    private ModifyProfileActivity activity;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {
        rule.launchActivity(null);
        activity = rule.getActivity();

        await().until(() -> activity.tvProfileUserEmail.length() > 0);
    }

    @Test
    public void testEditStatusMessage() throws Throwable {
        rule.runOnUiThread(() -> activity.editStatusMessage(activity.tvProfileStatusMessage));
        onView(withText(R.string.jandi_profile_status_message))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testEditPhoneNumber() throws Throwable {
        rule.runOnUiThread(() -> activity.editPhoneNumber(activity.tvProfileUserPhone));
        onView(withText(R.string.jandi_profile_phone_number))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testEditName() throws Throwable {
        rule.runOnUiThread(() -> activity.editName(activity.tvProfileRealName));
        onView(withText(R.string.jandi_title_name))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testEditDivision() throws Throwable {
        rule.runOnUiThread(() -> activity.editDivision());

        intending(IntentMatchers.hasComponent(InputProfileListActivity.class.getName()));
        intending(IntentMatchers.hasExtra(InputProfileListActivity.EXTRA_INPUT_MODE, InputProfileListActivity.EXTRA_DEPARTMENT_MODE));
    }

    @Test
    public void testEditPosition() throws Throwable {
        rule.runOnUiThread(() -> activity.editPosition());

        intending(IntentMatchers.hasComponent(InputProfileListActivity.class.getName()));
        intending(IntentMatchers.hasExtra(InputProfileListActivity.EXTRA_INPUT_MODE, InputProfileListActivity.EXTRA_JOB_TITLE_MODE));

    }

    @Test
    public void testEditEmail() throws Throwable {
        rule.runOnUiThread(() -> activity.editEmail(activity.tvProfileUserEmail));
        onView(withText(R.string.jandi_choose_email))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }


    @Test
    public void testCloseDialogFragment() throws Throwable {
        rule.runOnUiThread(() -> activity.closeDialogFragment());
        Fragment dialog = activity.getFragmentManager().findFragmentByTag("dialog");
        if (dialog != null && dialog.isVisible()) {
            fail("보이면 안되는데..");
        }
    }

    @Test
    public void testShowProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.showProgressWheel());
        assertThat(activity.progressWheel.isShowing(), is(true));
    }

    @Test
    public void testDisplayProfile() throws Throwable {
        User user = TeamInfoLoader.getInstance().getUser(TeamInfoLoader.getInstance().getMyId());
        rule.runOnUiThread(() -> activity.displayProfile(user));

        assertThat(activity.tvProfileStatusMessage.getText(), is(equalTo(user.getStatusMessage())));
        assertThat(activity.tvProfileRealName.getText(), is(equalTo(user.getName())));
        assertThat(activity.tvProfileUserDivision.getText(), is(equalTo(user.getDivision())));
        assertThat(activity.tvProfileUserEmail.getText(), is(equalTo(user.getEmail())));
        assertThat(activity.tvProfileUserPhone.getText(), is(equalTo(user.getPhoneNumber())));
        assertThat(activity.tvProfileUserPosition.getText(), is(equalTo(user.getPosition())));
    }

    @Test
    public void testDismissProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.dismissProgressWheel());
        assertThat(activity.progressWheel.isShowing(), is(false));
    }

    @Test
    public void testSetTextAndChangeColor() throws Throwable {
        String name = "haha";
        rule.runOnUiThread(() -> activity.setTextAndChangeColor(activity.tvProfileRealName, name));
        assertThat(activity.tvProfileRealName.getText(), is(equalTo(name)));
    }

    @Test
    public void testLaunchEditDialog() throws Throwable {
        rule.runOnUiThread(() -> activity.launchEditDialog(EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE, activity.tvProfileUserPhone));

        onView(withText(R.string.jandi_confirm))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testShowEmailChooseDialog() throws Throwable {
        String email1 = "hello1@hello.com";
        String email2 = "hello2@hello.com";
        rule.runOnUiThread(() -> activity.showEmailChooseDialog(new String[]{email1, email2}, email2));

        onView(withText(email2))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testUpdateEmailTextColor() throws Throwable {
        String email = "hello1@hello.com";
        rule.runOnUiThread(() -> activity.updateEmailTextColor(email));

        assertThat(activity.tvProfileUserEmail.getText(), is(equalTo(email)));
    }

    @Test
    public void testShowCheckNetworkDialog() throws Throwable {
        rule.runOnUiThread(() -> activity.showCheckNetworkDialog());

        onView(withText(R.string.jandi_confirm))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }
}