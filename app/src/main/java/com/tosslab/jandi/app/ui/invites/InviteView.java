package com.tosslab.jandi.app.ui.invites;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

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
public class InviteView {

    @ViewById(R.id.btn_invite_send)
    Button inviteButton;

    @ViewById(R.id.edit_invite_email)
    EditText emailTextView;

    @ViewById(R.id.lv_invite)
    ListView inviteListView;

    @ViewById(R.id.invite_succes_text_display)
    TextView displaySendEmailSuccesText;

    @ViewById(R.id.invite_footer_text)
    TextView manyPeopleInviteText;


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

        String inviteText = activity.getString(R.string.jandi_invite_many_people_explain);
        int index = inviteText.indexOf("support");
        SpannableStringBuilder builder = new SpannableStringBuilder(inviteText);
        URLSpan urlSpan = new URLSpan("mailto:" + inviteText.substring(index).toString()) {
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setColor(activity.getResources().getColor(R.color.jandi_accent_color));
            }
        };
        builder.setSpan(urlSpan, index, builder.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        manyPeopleInviteText.setText(builder);
        manyPeopleInviteText.setMovementMethod(LinkMovementMethod.getInstance());
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

    public void addEmail(EmailTO emailTO) {
        adapter.add(0, emailTO);
        adapter.notifyDataSetChanged();
    }

    @UiThread
    public void addSendEmailSuccessText() {
        displaySendEmailSuccesText.setVisibility(View.VISIBLE);
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

    @UiThread
    public void showSuccessToast(String message) {
        ColoredToast.show(activity, message);
    }

    @UiThread
    public void showWarnToast(String message) {
        ColoredToast.showWarning(activity, message);
    }

    @UiThread
    public void updateSuccessInvite(EmailTO o) {
        for (int idx = adapter.getCount() - 1; idx >= 0; --idx) {
            EmailTO item = adapter.getItem(idx);
            if (item == o) {
                item.setSuccess(true);
            }
        }

        adapter.notifyDataSetChanged();
    }

    public void moveToSelection(int position) {
        inviteListView.smoothScrollToPosition(position);
    }
}
