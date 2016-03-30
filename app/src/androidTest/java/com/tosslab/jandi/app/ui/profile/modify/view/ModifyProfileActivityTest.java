package com.tosslab.jandi.app.ui.profile.modify.view;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.graphics.Color;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ReqUpdateProfile;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;

import org.junit.Before;
import org.junit.BeforeClass;
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
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.fail;

@RunWith(AndroidJUnit4.class)
public class ModifyProfileActivityTest {

    @Rule
    public ActivityTestRule<ModifyProfileActivity_> rule = new ActivityTestRule<>(ModifyProfileActivity_.class, false, false);
    private ModifyProfileActivity activity;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {
        rule.launchActivity(null);
        activity = rule.getActivity();
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
        rule.runOnUiThread(() -> activity.editDivision(activity.tvProfileUserDivision));
        onView(withText(R.string.jandi_profile_division))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testEditPosition() throws Throwable {
        rule.runOnUiThread(() -> activity.editPosition(activity.tvProfileUserPosition));
        onView(withText(R.string.jandi_profile_position))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
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
    public void testDisplayProfile() throws Throwable {
        ResLeftSideMenu.User user = EntityManager.getInstance().getMe().getUser();
        rule.runOnUiThread(() -> activity.displayProfile(user));

        assertThat(activity.tvProfileStatusMessage.getText(), is(equalTo(user.u_statusMessage)));
        assertThat(activity.tvProfileRealName.getText(), is(equalTo(user.name)));
        assertThat(activity.tvProfileUserDivision.getText(), is(equalTo(user.u_extraData.department)));
        assertThat(activity.tvProfileUserEmail.getText(), is(equalTo(user.u_email)));
        assertThat(activity.tvProfileUserPhone.getText(), is(equalTo(user.u_extraData.phoneNumber)));
        assertThat(activity.tvProfileUserPosition.getText(), is(equalTo(user.u_extraData.position)));
    }

    @Test
    public void testDismissProgressWheel() throws Throwable {
        rule.runOnUiThread(() -> activity.dismissProgressWheel());
        assertThat(activity.progressWheel.isShowing(), is(false));
    }

    @Test
    public void testGetUpdateProfile() throws Exception {
        ReqUpdateProfile updateProfile = activity.getUpdateProfile();
        assertThat(updateProfile.position, is(equalTo(activity.tvProfileUserPosition.getText())));
        assertThat(updateProfile.statusMessage, is(equalTo(activity.tvProfileStatusMessage.getText())));
        assertThat(updateProfile.department, is(equalTo(activity.tvProfileUserDivision.getText())));
        assertThat(updateProfile.phoneNumber, is(equalTo(activity.tvProfileUserPhone.getText())));

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
    public void testShowPhotoUploadProgressDialog() throws Throwable {
        rule.runOnUiThread(() -> {
            ProgressDialog progressDialog = new ProgressDialog(activity);
            activity.showPhotoUploadProgressDialog(progressDialog);
        });

        onView(withText(R.string.jandi_file_uploading))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        pressBack();
    }

    @Test
    public void testDismissProgressDialog() throws Throwable {
        final ProgressDialog[] progressDialog = new ProgressDialog[1];
        rule.runOnUiThread(() -> {
            progressDialog[0] = new ProgressDialog(activity);
            progressDialog[0].show();
            activity.dismissProgressDialog(progressDialog[0]);
        });

        assertThat(progressDialog[0].isShowing(), is(false));
    }

    @Test
    public void testSuccessUpdateNameColor() throws Throwable {
        rule.runOnUiThread(() -> activity.tvProfileRealName.setTextColor(Color.BLUE));
        rule.runOnUiThread(() -> activity.successUpdateNameColor());

        int currentTextColor = activity.tvProfileRealName.getTextColors().getDefaultColor();
        assertThat(currentTextColor, is(equalTo(activity.getResources().getColor(R.color.jandi_text))));
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
    public void testSuccessUpdateEmailColor() throws Throwable {
        rule.runOnUiThread(() -> activity.tvProfileUserEmail.setTextColor(Color.BLUE));
        rule.runOnUiThread(() -> activity.successUpdateEmailColor());

        assertThat(activity.tvProfileUserEmail.getTextColors().getDefaultColor(), is(equalTo(activity.getResources().getColor(R.color.jandi_text))));
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