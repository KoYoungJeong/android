package com.tosslab.jandi.app.ui.profile.email;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.profile.DeleteEmailEvent;
import com.tosslab.jandi.app.events.profile.RetryNewEmailEvent;
import com.tosslab.jandi.app.ui.profile.email.adapter.EmailChooseAdapter;
import com.tosslab.jandi.app.ui.profile.email.to.AccountEmail;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 1. 12..
 */
@EBean
public class EmailChoosePresenter {


    @ViewById(R.id.lv_email_choose)
    ListView emailListView;

    @RootContext
    Activity activity;

    EmailChooseAdapter emailChooseAdapter;
    private ProgressWheel progressWheel;

    @AfterInject
    void initObject() {

        emailChooseAdapter = new EmailChooseAdapter(activity);

        progressWheel = new ProgressWheel(activity);
        progressWheel.init();
    }

    @AfterViews
    void initView() {

        emailListView.setAdapter(emailChooseAdapter);
    }

    public AccountEmail getSelectedEmail() {

        int count = emailChooseAdapter.getCount();
        for (int idx = 0; idx < count; ++idx) {
            AccountEmail item = emailChooseAdapter.getItem(idx);
            if (item.isSelected()) {
                return item;
            }
        }

        return null;

    }

    public void setEmails(List<AccountEmail> accountEmails) {
        emailChooseAdapter.setAccountEmails(accountEmails);
        emailChooseAdapter.notifyDataSetChanged();
    }

    @UiThread
    public void refreshEmails(List<AccountEmail> accountEmails) {
        emailChooseAdapter.setAccountEmails(accountEmails);
        emailChooseAdapter.notifyDataSetChanged();

    }

    public void refreshListView() {
        emailChooseAdapter.notifyDataSetChanged();
    }

    public void showNewEmailDialog() {

        EditTextDialogFragment editTextDialogFragment = EditTextDialogFragment.newInstance(EditTextDialogFragment.ACTION_NEW_EMAIL, "");
        editTextDialogFragment.show(activity.getFragmentManager(), "dialog");

    }

    public boolean hasSameEmail(String email) {

        int count = emailChooseAdapter.getCount();
        for (int idx = 0; idx < count; ++idx) {
            AccountEmail item = emailChooseAdapter.getItem(idx);
            if (TextUtils.equals(email, item.getEmail())) {
                return true;
            }
        }

        return false;
    }

    @UiThread
    public void showProgressWheel() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

        if (progressWheel != null) {
            progressWheel.show();
        }
    }

    @UiThread
    public void dismissProgressWheel() {

        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }

    }

    @Deprecated
    public void showRetryEmailDialog(final String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder
//                .setMessage(R.string.jandi_retry_auth_email)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getDefault().post(new RetryNewEmailEvent(email));
                    }
                }).create().show();
    }

    @UiThread
    public void showSuccessToast(String message) {
        ColoredToast.show(activity, message);
    }

    @UiThread
    public void showFailToast(String message) {
        ColoredToast.showError(activity, message);
    }

    public void showWarning(String message) {
        ColoredToast.showWarning(activity, message);
    }

    public void showDeleteEmail(final String email) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.jandi_action_delete)
                .setMessage(R.string.jandi_message_ask_about_deleting)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EventBus.getDefault().post(new DeleteEmailEvent(email));
                    }
                }).create().show();

    }

    @UiThread
    public void finish() {
        activity.finish();
    }
}
