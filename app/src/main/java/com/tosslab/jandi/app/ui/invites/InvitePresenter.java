package com.tosslab.jandi.app.ui.invites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.invites.adapter.InviteListAdapter;
import com.tosslab.jandi.app.ui.invites.to.EmailTO;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Steve SeongUg Jung on 14. 12. 27..
 */
@EBean
public class InvitePresenter {

    @ViewById(R.id.btn_invitation_confirm)
    Button inviteButton;

    @ViewById(R.id.et_invitation_email)
    EditText emailTextView;

    @ViewById(R.id.lv_invite)
    ListView inviteListView;

    @RootContext
    Activity activity;

    private InviteListAdapter adapter;
    private ProgressWheel progressWheel;

    @AfterViews
    void initViews() {
        adapter = new InviteListAdapter(activity);
        inviteListView.setAdapter(adapter);
        progressWheel = new ProgressWheel(activity);
        progressWheel.init();
    }


    public String getEmailText() {
        return emailTextView.getText().toString();
    }

    public void setEmailText(String email) {
        emailTextView.setText(email);
        emailTextView.setSelection(email.length());
    }

    public void setEnableAddButton(boolean invalidEmail) {
        inviteButton.setEnabled(invalidEmail);
    }

    @UiThread
    public void addEmailAtFirst(EmailTO emailTO) {
        adapter.add(0, emailTO);
        adapter.notifyDataSetChanged();
    }

    @UiThread
    public void clearEmailTextView() {
        emailTextView.setText("");

    }

    public void notifyDatasetChanged() {

        adapter.notifyDataSetChanged();

    }

    @UiThread
    public void showProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
        progressWheel.show();
    }

    @UiThread
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    public List<String> getInvites() {

        List<String> invites = new ArrayList<String>();

        String email;
        for (int index = 0; index < adapter.getCount(); index++) {
            email = adapter.getItem(index).getEmail();
            invites.add(email);
        }

        return invites;
    }

    @UiThread
    public void showNoEmailDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.jandi_no_invite_email)
                .setPositiveButton(R.string.jandi_confirm, null)
                .create().show();
    }

    @UiThread
    public void showSuccessDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(R.string.jandi_invite_success)
                .setPositiveButton(R.string.jandi_confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                })
                .create().show();

    }

    @UiThread
    public void clearItems() {
        adapter.clear();
        notifyDatasetChanged();
    }

    @UiThread
    public void showErrorToast(String message) {
        ColoredToast.showError(activity, message);
    }
}
