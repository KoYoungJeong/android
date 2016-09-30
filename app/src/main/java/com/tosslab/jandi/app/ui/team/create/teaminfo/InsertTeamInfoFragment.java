package com.tosslab.jandi.app.ui.team.create.teaminfo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.services.socket.monitor.SocketServiceStarter;
import com.tosslab.jandi.app.ui.team.create.teaminfo.dagger.DaggerInsertTeamInfoComponent;
import com.tosslab.jandi.app.ui.team.create.teaminfo.dagger.InsertTeamInfoModule;
import com.tosslab.jandi.app.ui.team.create.teaminfo.presenter.InsertTeamInfoPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.analytics.sprinkler.ScreenViewProperty;
import com.tosslab.jandi.app.utils.analytics.sprinkler.model.SprinklrScreenView;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 16. 6. 21..
 */

public class InsertTeamInfoFragment extends Fragment implements InsertTeamInfoPresenter.View {

    public static final String MODE = "mode";
    public static final int MODE_FROM_MAIN_LIST = 0x01;
    public static final int MODE_FROM_ACCOUNT_HOME = 0x02;

    @Inject
    InsertTeamInfoPresenter teamInsertInfoPresenter;

    @Bind(R.id.tv_team_name_length)
    TextView tvTeamNameLength;

    @Bind(R.id.et_insert_team_name)
    EditText etInsertTeamName;

    @Bind(R.id.et_insert_team_domain)
    EditText etInsertTeamDomain;

    @Bind(R.id.tv_domain_tail)
    TextView tvDomainTail;

    @Bind(R.id.tv_team_name_insert_error)
    TextView tvTeamNameInsertError;

    @Bind(R.id.tv_team_domain_insert_error)
    TextView tvTeamDomainInsertError;

    @Bind(R.id.vg_button_type1)
    ViewGroup vgButtonType1;

    @Bind(R.id.vg_button_type2)
    ViewGroup vgButtonType2;

    @Bind(R.id.iv_team_create_done)
    ImageView ivTeamCreateDone;

    @Bind(R.id.tv_team_create_done)
    TextView tvTeamCreateDone;

    private int mode = MODE_FROM_MAIN_LIST;

    private boolean isInsertTeamNamePositiveLength = false;
    private boolean isShownTeamDomainError = false;
    private ProgressWheel progressWheel;
    private OnChangePageClickListener onChangePageClickListener;

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
        DaggerInsertTeamInfoComponent.builder()
                .insertTeamInfoModule(new InsertTeamInfoModule(this))
                .build()
                .inject(this);
    }

    private void setActiveDoneButton() {
        if (mode != MODE_FROM_ACCOUNT_HOME) {
            return;
        }

        boolean active;

        if (etInsertTeamDomain.getText().length() > 0 &&
                etInsertTeamName.getText().length() > 0) {
            active = true;
        } else {
            active = false;
        }

        if (active) {
            ivTeamCreateDone.setImageResource(R.drawable.icon_profile_check_active);
            tvTeamCreateDone.setTextColor(0xff00a4e6);
        } else {
            ivTeamCreateDone.setImageResource(R.drawable.icon_profile_check_inactive);
            tvTeamCreateDone.setTextColor(0xff999999);
        }
    }

    private void setMode() {
        mode = getArguments().getInt(MODE);
        if (mode == MODE_FROM_MAIN_LIST) {
            vgButtonType1.setVisibility(View.VISIBLE);
            vgButtonType2.setVisibility(View.GONE);
        } else {
            vgButtonType1.setVisibility(View.GONE);
            vgButtonType2.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setMode();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_create_team, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        registTeamNameTextWatcher();
        registTeamDomainTextWatcher();
        teamInsertInfoPresenter.checkEmailInfo();

        AnalyticsUtil.sendScreenName(AnalyticsValue.Screen.CreateaTeam);
        SprinklrScreenView.sendLog(ScreenViewProperty.TEAM_CREATE);
    }

    @Override
    public void showTeamNameLengthError() {

        if (tvTeamNameLength.getVisibility() == View.VISIBLE) {
            etInsertTeamName.setBackgroundResource(R.drawable.profile_text_input2_bg_error_left);
            tvTeamNameLength.setBackgroundResource(R.drawable.profile_text_input2_bg_error_right);
        } else {
            etInsertTeamName.setBackgroundResource(R.drawable.profile_text_input_bg_error);
        }

        tvTeamNameInsertError.setVisibility(View.VISIBLE);
        tvTeamNameInsertError.setText(R.string.jandi_team_create_name_length_invalid);
    }

    public void hideTeamNameError() {
        etInsertTeamName.setBackgroundResource(R.drawable.profile_text_input2_bg_left);
        tvTeamNameLength.setBackgroundResource(R.drawable.profile_text_input2_bg_right);
        tvTeamNameInsertError.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showTeamDomainInvalidUrlError() {
        etInsertTeamDomain.setBackgroundResource(R.drawable.profile_text_input2_bg_error_left);
        tvDomainTail.setBackgroundResource(R.drawable.profile_text_input2_bg_error_right);
        tvTeamDomainInsertError.setVisibility(View.VISIBLE);
        tvTeamDomainInsertError.setText(R.string.jandi_team_create_domain_invalid);
        isShownTeamDomainError = true;
    }

    @Override
    public void showTeamDomainLengthError() {
        etInsertTeamDomain.setBackgroundResource(R.drawable.profile_text_input2_bg_error_left);
        tvDomainTail.setBackgroundResource(R.drawable.profile_text_input2_bg_error_right);
        tvTeamDomainInsertError.setVisibility(View.VISIBLE);
        tvTeamDomainInsertError.setText(R.string.jandi_team_create_domain_length_invalid);
        isShownTeamDomainError = true;
    }

    @Override
    public void showTeamInvalidOrSameDomainError() {
        etInsertTeamDomain.setBackgroundResource(R.drawable.profile_text_input2_bg_error_left);
        tvDomainTail.setBackgroundResource(R.drawable.profile_text_input2_bg_error_right);
        tvTeamDomainInsertError.setVisibility(View.VISIBLE);
        tvTeamDomainInsertError.setText(R.string.jandi_domain_is_already_taken);
        isShownTeamDomainError = true;
    }

    public void hideTeamDomainError() {
        if (isShownTeamDomainError) {
            etInsertTeamDomain.setBackgroundResource(R.drawable.profile_text_input2_bg_left);
            tvDomainTail.setBackgroundResource(R.drawable.profile_text_input2_bg_right);
            tvTeamDomainInsertError.setVisibility(View.INVISIBLE);
            isShownTeamDomainError = false;
        }
    }

    @OnClick(R.id.iv_team_create_cancel)
    void onClickTeamCreateCancel() {
        getActivity().finish();
    }

    @OnClick(R.id.iv_team_create_next)
    void onClickTeamCreateNext() {
        createTeam();
    }

    @OnClick(R.id.iv_team_create_done)
    void onClickTeamCreateDone() {
        createTeam();
    }

    private void createTeam() {
        String teamName = etInsertTeamName.getText().toString().trim();
        String teamDomain = etInsertTeamDomain.getText().toString().trim();
        teamInsertInfoPresenter.createTeam(teamName, teamDomain.toLowerCase(), mode);
    }

    @OnClick(R.id.tv_go_to_main_button)
    void onCLickGoToMain() {
        finish();
    }

    @Override
    public void showFailToast(String message) {
        ColoredToast.showWarning(message);
    }

    @Override
    public void failCreateTeam(int statusCode) {
        ColoredToast.showWarning(getString(R.string.fail_to_create_team));
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
    public void finish() {
        getActivity().finish();
    }

    private void registTeamNameTextWatcher() {
        etInsertTeamName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setActiveDoneButton();
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) etInsertTeamName.getLayoutParams();

                int margin = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());

                if (editable.length() > 0) {
                    tvTeamNameLength.setVisibility(View.VISIBLE);
                    if (!isInsertTeamNamePositiveLength) {
                        isInsertTeamNamePositiveLength = true;
                        etInsertTeamName.setBackgroundResource(R.drawable.profile_text_input2_bg_left);
                        params.leftMargin = margin;
                        params.rightMargin = 0;
                        etInsertTeamName.setLayoutParams(params);
                    }

                    if (editable.length() > 20) {
                        Spannable lengthError = new SpannableString("20/20");
                        lengthError.setSpan(
                                new ForegroundColorSpan(0xffff5b49), 0, 2, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        showTeamNameLengthError();
                        etInsertTeamName.removeTextChangedListener(this);
                        etInsertTeamName.setText(editable.subSequence(0, 20));
                        etInsertTeamName.addTextChangedListener(this);
                        etInsertTeamName.setSelection(20);
                        tvTeamNameLength.setText(lengthError);
                    } else {
                        tvTeamNameLength.setText(editable.length() + "/20");
                        hideTeamNameError();
                    }
                } else {
                    tvTeamNameLength.setVisibility(View.GONE);
                    if (isInsertTeamNamePositiveLength) {
                        isInsertTeamNamePositiveLength = false;
                        etInsertTeamName.setBackgroundResource(R.drawable.profile_text_input_bg);
                        params.leftMargin = margin;
                        params.rightMargin = margin;
                        etInsertTeamName.setLayoutParams(params);
                    }
                }
            }
        });
    }

    private void registTeamDomainTextWatcher() {
        etInsertTeamDomain.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
                setActiveDoneButton();
                if (isShownTeamDomainError) {
                    hideTeamDomainError();
                }
            }
        });
    }

    @Override
    public void onMoveInsertProfilePage() {
        onChangePageClickListener.onClickMoveInsertProfileFirstPage();
    }

    @Override
    public void onMoveMainTabActivity() {
        JandiSocketService.stopService(getContext());
        getContext().sendBroadcast(new Intent(SocketServiceStarter.START_SOCKET_SERVICE));

        startActivity(Henson.with(getContext())
                .gotoMainTabActivity()
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TASK
                        | Intent.FLAG_ACTIVITY_CLEAR_TOP));
        getActivity().overridePendingTransition(0, 0);

        finish();
    }

    public interface OnChangePageClickListener {
        void onClickMoveInsertProfileFirstPage();
    }

}