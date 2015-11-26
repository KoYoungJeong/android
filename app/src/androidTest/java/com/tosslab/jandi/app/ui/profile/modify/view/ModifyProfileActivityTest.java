package com.tosslab.jandi.app.ui.profile.modify.view;

import android.app.Fragment;
import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import setup.BaseInitUtil;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ModifyProfileActivityTest {

    @Rule
    public ActivityTestRule<ModifyProfileActivity> rule = new ActivityTestRule<ModifyProfileActivity>(ModifyProfileActivity.class, false, false);
    private ModifyProfileActivity activity;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        rule.launchActivity(new Intent());
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        activity = rule.getActivity();
    }

    @Test
    public void testEditStatusMessage() throws Throwable {
        rule.runOnUiThread(() -> activity.editStatusMessage(activity.textViewProfileStatusMessage));
        onView(withText(R.string.jandi_profile_status_message))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEditPhoneNumber() throws Throwable {
        rule.runOnUiThread(() -> activity.editPhoneNumber(activity.textViewProfileUserPhone));
        onView(withText(R.string.jandi_profile_phone_number))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEditName() throws Throwable {
        rule.runOnUiThread(() -> activity.editName(activity.textViewProfileRealName));
        onView(withText(R.string.jandi_title_name))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEditDivision() throws Throwable {
        rule.runOnUiThread(() -> activity.editDivision(activity.textViewProfileUserDivision));
        onView(withText(R.string.jandi_profile_division))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEditPosition() throws Throwable {
        rule.runOnUiThread(() -> activity.editPosition(activity.textViewProfileUserPosition));
        onView(withText(R.string.jandi_profile_position))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void testEditEmail() throws Throwable {
        rule.runOnUiThread(() -> activity.editEmail(activity.textViewProfileUserEmail));
        onView(withText(R.string.jandi_user_id))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }


    @Test
    public void testCloseDialogFragment() throws Throwable {
        rule.runOnUiThread(() -> activity.closeDialogFragment());
        Fragment dialog = activity.getFragmentManager().findFragmentByTag("dialog");
        if (dialog == null || dialog.isVisible()) {
            fail("보이면 안되는데..");
        }
    }

    @Test
    public void testShowFailProfile() throws Throwable {
        rule.runOnUiThread(() -> activity.showFailProfile());
        assertThat(activity.isFinishing(), is(true));
    }

    @Test
    public void testShowProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.showProgressWheel());
        assertThat(activity.progressWheel.isShowing(), is(true));
    }

    @Test
    public void testDisplayProfile() throws Exception {

    }

    @Test
    public void testDisplayProfileImage() throws Exception {

    }

    @Test
    public void testDismissProgressWheel() throws Exception {

    }

    @Test
    public void testGetUpdateProfile() throws Exception {

    }

    @Test
    public void testUpdateProfileTextColor() throws Exception {

    }

    @Test
    public void testSetTextAndChangeColor() throws Exception {

    }

    @Test
    public void testUpdateLocalProfileImage() throws Exception {

    }

    @Test
    public void testLaunchEditDialog() throws Exception {

    }

    @Test
    public void testShowToastNoUpdateProfile() throws Exception {

    }

    @Test
    public void testShowPhotoUploadProgressDialog() throws Exception {

    }

    @Test
    public void testDismissProgressDialog() throws Exception {

    }

    @Test
    public void testSuccessPhotoUpload() throws Exception {

    }

    @Test
    public void testFailPhotoUpload() throws Exception {

    }

    @Test
    public void testUpdateProfileSucceed() throws Exception {

    }

    @Test
    public void testUpdateProfileFailed() throws Exception {

    }

    @Test
    public void testGetName() throws Exception {

    }

    @Test
    public void testSuccessUpdateNameColor() throws Exception {

    }

    @Test
    public void testShowEmailChooseDialog() throws Exception {

    }

    @Test
    public void testGetEmail() throws Exception {

    }

    @Test
    public void testUpdateEmailTextColor() throws Exception {

    }

    @Test
    public void testSuccessUpdateEmailColor() throws Exception {

    }

    @Test
    public void testShowCheckNetworkDialog() throws Exception {

    }
}