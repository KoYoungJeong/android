package com.tosslab.jandi.app.ui.profile.account;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SignOutEvent;
import com.tosslab.jandi.app.events.profile.AccountEmailChangeEvent;
import com.tosslab.jandi.app.network.models.ResAccountInfo;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.GlideCircleTransform;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 14. 12. 23..
 */
@EBean
public class AccountProfilePresenter {


    @RootContext
    Context context;

    @ViewById(R.id.img_account_profile_photo)
    ImageView profileImageView;


    @ViewById(R.id.txt_account_profile_user_name)
    TextView nameTextView;

    @ViewById(R.id.txt_account_profile_user_email)
    TextView emailTextView;
    private ProgressWheel progressWheel;


    public void setProfileImage(Uri uri) {
        Glide.with(context)
                .load(uri)
                .placeholder(R.drawable.jandi_profile)
                .transform(new GlideCircleTransform(context))
                .skipMemoryCache(true)              // 메모리 캐시를 쓰지 않는다.
                .into(profileImageView);

    }

    public void setEmail(String email) {
        emailTextView.setText(email);
    }

    public void showEmailsDialog(final List<ResAccountInfo.UserEmail> accountEmails) {

        int emailSize = accountEmails.size();
        String[] emailItems = new String[emailSize];
        int primaryPosition = 0;
        for (int idx = 0; idx < emailSize; idx++) {
            emailItems[idx] = accountEmails.get(idx).getId();
            if (accountEmails.get(idx).isPrimary()) {
                primaryPosition = idx;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setSingleChoiceItems(emailItems, primaryPosition, null)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int checkedItemPosition = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                        ResAccountInfo.UserEmail userEmail = accountEmails.get(checkedItemPosition);
                        EventBus.getDefault().post(new AccountEmailChangeEvent(userEmail));
                    }
                })
                .create().show();
    }

    public String getPrimaryEmail() {
        return emailTextView.getText().toString();
    }

    public String getName() {
        return nameTextView.getText().toString();
    }

    public void setName(String name) {
        nameTextView.setText(name);
    }

    @UiThread
    public void showSuccessToModifyProfile() {
        ColoredToast.show(context, context.getString(R.string.jandi_profile_update_succeed));
    }

    @UiThread
    public void showFailToModifyProfile() {
        ColoredToast.showWarning(context, context.getString(R.string.err_profile_update));

    }

    public void showSignoutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(R.string.jandi_setting_sign_out)
                .setMessage(R.string.jandi_sign_out_message)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_setting_sign_out, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // MainTabActivity 에서 로그아웃 처리.
                        EventBus.getDefault().post(new SignOutEvent());

                    }
                })
                .create().show();
    }

    @UiThread
    public void showProgressDialog() {

        if (progressWheel == null) {
            progressWheel = new ProgressWheel(context);
            progressWheel.init();
        }

        progressWheel.show();

    }

    @UiThread
    public void dismissProgressDialog() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

    }

    public void returnToLoginActivity() {
        IntroActivity_.intent(context)
                .flags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK)
                .start();
    }

    @UiThread
    public void showIncrementProgressDialog(ProgressDialog progressDialog) {
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(context.getString(R.string.jandi_file_uploading));
        progressDialog.show();
    }

    @UiThread
    public void dismissIncrementProgressDialog(ProgressDialog progressDialog) {

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }

    }
}
