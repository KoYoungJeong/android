package com.tosslab.jandi.app.ui.profile.insert.views;

import android.app.AlertDialog;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.ui.profile.insert.presenter.SetProfileSecondPagePresenter;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

/**
 * Created by tee on 16. 3. 16..
 */

@EFragment(R.layout.fragment_insert_profile_second)
public class SetProfileSecondPageFragment extends Fragment
        implements SetProfileSecondPagePresenter.View {

    @Bean
    SetProfileSecondPagePresenter presenter;

    @ViewById(R.id.tv_email)
    TextView tvEmail;

    @ViewById(R.id.et_department)
    EditText etDepartment;

    @ViewById(R.id.et_positon)
    EditText etPosition;

    @ViewById(R.id.et_phone_number)
    EditText etPhoneNumber;

    @ViewById(R.id.et_status_message)
    EditText etStatusMessage;

    private ProgressWheel progressWheel;

    @AfterInject
    void initObject() {
        presenter.setView(this);
    }

    @AfterViews
    void initViews() {
        presenter.requestProfile();
    }

    @Click(R.id.iv_profile_check)
    void onClickProfileCheck() {
        presenter.uploadExtraInfo(
                etDepartment.getText().toString(),
                etPosition.getText().toString(),
                etPhoneNumber.getText().toString(),
                etStatusMessage.getText().toString()
        );
    }

    @Click(R.id.tv_email)
    void onClickChooseEmail() {
        presenter.chooseEmail(getEmail());
    }

    @Override
    public void showEmailChooseDialog(String[] emails, String currentEmail) {
        int checkedIdx = 0;

        for (int idx = 0; idx < emails.length; idx++) {

            if (TextUtils.equals(emails[idx], currentEmail)) {
                checkedIdx = idx;
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);
        builder.setTitle(R.string.jandi_choose_email)
                .setSingleChoiceItems(emails, checkedIdx, null)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    int checkedItemPosition =
                            ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                    presenter.uploadEmail(emails[checkedItemPosition]);
                    setEmail(emails, emails[checkedItemPosition]);
                })
                .create().show();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        dismissProgressWheel();
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
        }
        progressWheel.show();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showFailProfile() {
        ColoredToast.showError(
                JandiApplication.getContext().getString(R.string.err_profile_get_info));
    }

    private String getEmail() {
        String email = tvEmail.getText().toString();
        if (email.subSequence(email.length() - 2, email.length()).equals(" >")) {
            return email.subSequence(0, email.length() - 2).toString();
        }
        return tvEmail.getText().toString();
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setEmail(String[] accountEmails, String email) {
        if (accountEmails.length == 1) {
            tvEmail.setText(email);
            tvEmail.setClickable(false);
        } else {
            tvEmail.setText(email + " >");
            tvEmail.setClickable(true);
        }
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void displayProfileInfos(ResLeftSideMenu.User me) {
        presenter.setEmail(me.u_email);

        // 부서
        String strDivision = (me.u_extraData.department);
        if (!TextUtils.isEmpty(strDivision)) {
            etDepartment.setText(strDivision);
        }

        // 직책
        String strPosition = me.u_extraData.position;
        if (!TextUtils.isEmpty(strPosition)) {
            etPosition.setText(strPosition);
        }

        // 폰넘버
        String strPhone = (me.u_extraData.phoneNumber);
        if (!TextUtils.isEmpty(strPhone)) {
            etPhoneNumber.setText(strPhone);
        }

        // 상태 메세지
        String strStatus = (me.u_statusMessage);
        if (!TextUtils.isEmpty(strStatus)) {
            etStatusMessage.setText(strStatus);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void updateProfileSucceed() {
        ColoredToast.show(JandiApplication.getContext()
                .getString(R.string.jandi_profile_update_succeed));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void updateProfileFailed() {
        ColoredToast.showError(JandiApplication.getContext()
                .getString(R.string.err_profile_update));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(getActivity(), null);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finish() {
        getActivity().finish();
    }
}
