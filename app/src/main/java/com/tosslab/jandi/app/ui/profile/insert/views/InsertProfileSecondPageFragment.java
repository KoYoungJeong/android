package com.tosslab.jandi.app.ui.profile.insert.views;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.insert.dagger.DaggerInsertProfileSecondPageComponent;
import com.tosslab.jandi.app.ui.profile.insert.dagger.InsertProfileSecondPageModule;
import com.tosslab.jandi.app.ui.profile.insert.presenter.InsertProfileSecondPagePresenter;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 16. 3. 16..
 */

public class InsertProfileSecondPageFragment extends Fragment
        implements InsertProfileSecondPagePresenter.View {

    @Inject
    InsertProfileSecondPagePresenter presenter;

    @Bind(R.id.tv_email)
    TextView tvEmail;

    @Bind(R.id.et_department)
    EditText etDepartment;

    @Bind(R.id.et_positon)
    EditText etPosition;

    @Bind(R.id.et_phone_number)
    EditText etPhoneNumber;

    @Bind(R.id.et_status_message)
    EditText etStatusMessage;

    @Bind(R.id.vg_bottom_for_insert_profile)
    ViewGroup vgBottomForInsertProfile;

    @Bind(R.id.vg_bottom_for_create_team_next)
    ViewGroup vgBottomForCreateTeamNext;

    @Bind(R.id.vg_bottom_for_create_team_previous)
    ViewGroup getVgBottomForCreateTeamPrevious;

    private ProgressWheel progressWheel;
    private String pageMode;

    private OnChangePageClickListener onChangePageClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert_profile_second, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChangePageClickListener) {
            onChangePageClickListener = (OnChangePageClickListener) context;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerInsertProfileSecondPageComponent.builder()
                .insertProfileSecondPageModule(new InsertProfileSecondPageModule(this))
                .build()
                .inject(this);
        Bundle bundle = getArguments();
        pageMode = bundle.getString(InsertProfileFirstPageFragment.MODE);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (pageMode.equals(InsertProfileFirstPageFragment.MODE_INSERT_PROFILE)) {
            vgBottomForInsertProfile.setVisibility(View.VISIBLE);
            vgBottomForCreateTeamNext.setVisibility(View.GONE);
            getVgBottomForCreateTeamPrevious.setVisibility(View.GONE);
        } else {
            vgBottomForInsertProfile.setVisibility(View.GONE);
            vgBottomForCreateTeamNext.setVisibility(View.VISIBLE);
            getVgBottomForCreateTeamPrevious.setVisibility(View.VISIBLE);
        }
        presenter.requestProfile();
    }

    @OnClick(R.id.iv_profile_check)
    void onClickProfileCheck() {
        presenter.uploadExtraInfo(
                etDepartment.getText().toString(),
                etPosition.getText().toString(),
                etPhoneNumber.getText().toString(),
                etStatusMessage.getText().toString()
        );
    }

    @OnClick(R.id.iv_team_create_next)
    void onClickTeamCreateNext() {
        onClickProfileCheck();
    }

    @OnClick(R.id.iv_team_create_previous)
    void onClickTeamCreatePrevious() {
        onChangePageClickListener.onClickMovePrevPage();
    }

    @OnClick(R.id.tv_email)
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

    @Override
    public void showProgressWheel() {
        dismissProgressWheel();
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
        }
        progressWheel.show();
    }

    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

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
    public void displayProfileInfos(User me) {
        presenter.setEmail(me.getEmail());

        // 부서
        String strDivision = (me.getDivision());
        if (!TextUtils.isEmpty(strDivision)) {
            etDepartment.setText(strDivision);
        }

        // 직책
        String strPosition = me.getPosition();
        if (!TextUtils.isEmpty(strPosition)) {
            etPosition.setText(strPosition);
        }

        // 폰넘버
        String strPhone = (me.getPhoneNumber());
        if (!TextUtils.isEmpty(strPhone)) {
            etPhoneNumber.setText(strPhone);
        }

        // 상태 메세지
        String strStatus = (me.getStatusMessage());
        if (!TextUtils.isEmpty(strStatus)) {
            etStatusMessage.setText(strStatus);
        }
    }

    @Override
    public void updateProfileSucceed() {
        ColoredToast.show(JandiApplication.getContext()
                .getString(R.string.jandi_profile_update_succeed));
    }

    @Override
    public void updateProfileFailed() {
        ColoredToast.showError(JandiApplication.getContext()
                .getString(R.string.err_profile_update));
    }

    @Override
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(getActivity(), null);
    }

    @Override
    public void finish() {
        if (pageMode.equals(InsertProfileFirstPageFragment.MODE_INSERT_PROFILE)) {
            // 프로필 입력 모드 시
            getActivity().finish();
        } else {
            // 팀 생성 모드 시
            onChangePageClickListener.onClickMoveFinalPage();
        }
    }

    public interface OnChangePageClickListener {
        void onClickMoveFinalPage();

        void onClickMovePrevPage();
    }

}
