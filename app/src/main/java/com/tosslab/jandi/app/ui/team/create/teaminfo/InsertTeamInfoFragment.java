package com.tosslab.jandi.app.ui.team.create.teaminfo;

import android.content.Context;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.team.create.teaminfo.dagger.DaggerInsertTeamInfoComponent;
import com.tosslab.jandi.app.ui.team.create.teaminfo.dagger.InsertTeamInfoModule;
import com.tosslab.jandi.app.ui.team.create.teaminfo.presenter.InsertTeamInfoPresenter;
import com.tosslab.jandi.app.utils.AccountUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.lib.sprinkler.constant.event.Event;
import com.tosslab.jandi.lib.sprinkler.constant.property.PropertyKey;
import com.tosslab.jandi.lib.sprinkler.constant.property.ScreenViewProperty;
import com.tosslab.jandi.lib.sprinkler.io.model.FutureTrack;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 16. 6. 21..
 */

public class InsertTeamInfoFragment extends Fragment implements InsertTeamInfoPresenter.View {

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
        AnalyticsUtil.trackSprinkler(new FutureTrack.Builder()
                .event(Event.ScreenView)
                .accountId(AccountUtil.getAccountId(JandiApplication.getContext()))
                .property(PropertyKey.ScreenView, ScreenViewProperty.TEAM_CREATE)
                .build());
    }

    @Override
    public void showTeamNameLengthError() {
        etInsertTeamName.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_error_left));
        tvTeamNameLength.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_error_right));
        tvTeamNameInsertError.setVisibility(View.VISIBLE);
        tvTeamNameInsertError.setText("최대 20자까지 사용 가능합니다");
    }

    public void hideTeamNameError() {
        etInsertTeamName.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_left));
        tvTeamNameLength.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_right));
        tvTeamNameInsertError.setVisibility(View.INVISIBLE);
    }

    @Override
    public void showTeamDomainInvalidUrlError() {
        etInsertTeamDomain.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_error_left));
        tvDomainTail.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_error_right));
        tvTeamDomainInsertError.setVisibility(View.VISIBLE);
        tvTeamDomainInsertError.setText("알파벳,숫자 또는 (-)만 입력 가능합니다");
        isShownTeamDomainError = true;
    }

    @Override
    public void showTeamDomainLengthError() {
        etInsertTeamDomain.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_error_left));
        tvDomainTail.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_error_right));
        tvTeamDomainInsertError.setVisibility(View.VISIBLE);
        tvTeamDomainInsertError.setText("알파벳 3자 이상 입력해 주세요");
        isShownTeamDomainError = true;
    }

    @Override
    public void showTeamInvalidOrSameDomainError() {
        etInsertTeamDomain.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_error_left));
        tvDomainTail.setBackgroundDrawable(
                getResources().getDrawable(R.drawable.profile_text_input2_bg_error_right));
        tvTeamDomainInsertError.setVisibility(View.VISIBLE);
        tvTeamDomainInsertError.setText(getString(R.string.jandi_domain_is_already_taken));
        isShownTeamDomainError = true;
    }

    public void hideTeamDomainError() {
        if (isShownTeamDomainError) {
            etInsertTeamDomain.setBackgroundDrawable(
                    getResources().getDrawable(R.drawable.profile_text_input2_bg_left));
            tvDomainTail.setBackgroundDrawable(
                    getResources().getDrawable(R.drawable.profile_text_input2_bg_right));
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
        String teamName = etInsertTeamName.getText().toString();
        String teamDomain = etInsertTeamDomain.getText().toString();
        teamInsertInfoPresenter.createTeam(teamName, teamDomain.toLowerCase());
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
                RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) etInsertTeamName.getLayoutParams();

                int margin = (int) TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics());

                if (editable.length() > 0) {
                    tvTeamNameLength.setVisibility(View.VISIBLE);
                    if (!isInsertTeamNamePositiveLength) {
                        isInsertTeamNamePositiveLength = true;
                        etInsertTeamName.setBackgroundDrawable(
                                getResources().getDrawable(R.drawable.profile_text_input2_bg_left));
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
                        etInsertTeamName.setBackgroundDrawable(
                                getResources().getDrawable(R.drawable.profile_text_input_bg));
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

    public interface OnChangePageClickListener {
        void onClickMoveInsertProfileFirstPage();
    }

}