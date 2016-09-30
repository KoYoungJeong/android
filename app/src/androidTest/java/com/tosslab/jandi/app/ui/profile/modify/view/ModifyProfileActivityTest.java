package com.tosslab.jandi.app.ui.profile.modify.view;

import android.app.Fragment;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.property.dept.DeptPositionActivity;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view.NameStatusActivity;

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

        await().until(() -> activity.tvProfileUserEmail.getContentLength() > 0);
    }

    @Test
    public void testEditStatusMessage() throws Throwable {
        rule.runOnUiThread(() -> activity.editStatusMessage());
        intending(IntentMatchers.hasComponent(NameStatusActivity.class.getName()));
        intending(IntentMatchers.hasExtra("type", NameStatusActivity.EXTRA_TYPE_STATUS));
    }

    @Test
    public void testEditPhoneNumber() throws Throwable {
        rule.runOnUiThread(() -> activity.editPhoneNumber());
        onView(withText(R.string.jandi_profile_phone_number))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testEditName() throws Throwable {
        rule.runOnUiThread(() -> activity.editName());
        intending(IntentMatchers.hasComponent(NameStatusActivity.class.getName()));
        intending(IntentMatchers.hasExtra("type", NameStatusActivity.EXTRA_TYPE_NAME_FOR_TEAM_PROFILE));
    }

    @Test
    public void testEditDivision() throws Throwable {
        rule.runOnUiThread(() -> activity.editDivision());

        intending(IntentMatchers.hasComponent(DeptPositionActivity.class.getName()));
        intending(IntentMatchers.hasExtra(DeptPositionActivity.EXTRA_INPUT_MODE, DeptPositionActivity.EXTRA_DEPARTMENT_MODE));
    }

    @Test
    public void testEditPosition() throws Throwable {
        rule.runOnUiThread(() -> activity.editPosition());

        intending(IntentMatchers.hasComponent(DeptPositionActivity.class.getName()));
        intending(IntentMatchers.hasExtra(DeptPositionActivity.EXTRA_INPUT_MODE, DeptPositionActivity.EXTRA_JOB_TITLE_MODE));

    }

    @Test
    public void testEditEmail() throws Throwable {
        rule.runOnUiThread(() -> activity.editEmail());
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

        assertThat(activity.tvProfileStatusMessage.getContent(), is(equalTo(user.getStatusMessage())));
        assertThat(activity.tvProfileRealName.getContent(), is(equalTo(user.getName())));
        assertThat(activity.tvProfileUserDivision.getContent(), is(equalTo(user.getDivision())));
        assertThat(activity.tvProfileUserEmail.getContent(), is(equalTo(user.getEmail())));
        assertThat(activity.tvProfileUserPhone.getContent(), is(equalTo(user.getPhoneNumber())));
        assertThat(activity.tvProfileUserPosition.getContent(), is(equalTo(user.getPosition())));
    }

    @Test
    public void testDismissProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.dismissProgressWheel());
        assertThat(activity.progressWheel.isShowing(), is(false));
    }

    @Test
    public void testLaunchEditDialog() throws Throwable {
        rule.runOnUiThread(() -> activity.launchEditDialog(EditTextDialogFragment.ACTION_MODIFY_PROFILE_PHONE, activity.tvProfileUserPhone.getContent()));

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

        assertThat(activity.tvProfileUserEmail.getContent(), is(equalTo(email)));
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