package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.dagger.DaggerNameStatusComponent;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.dagger.NameStatusModule;
import com.tosslab.jandi.app.ui.profile.modify.property.namestatus.presenter.NameStatusPresenter;
import com.tosslab.jandi.app.utils.ProgressWheel;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class NameChangeFragment extends Fragment implements NameStatusPresenter.View {

    public static final int NAME_MAX_LENGTH = 30;
    @Bind(R.id.et_name_change)
    EditText etName;

    @Bind(R.id.tv_name_change_count)
    TextView tvCount;

    @Bind(R.id.toolbar_name_change)
    Toolbar toolbar;
    @Inject
    NameStatusPresenter presenter;
    private ProgressWheel progressWheel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_name_change, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DaggerNameStatusComponent.builder()
                .nameStatusModule(new NameStatusModule(this))
                .build()
                .inject(this);

        presenter.onInitUserInfo();

        setUpActionbar();
        setHasOptionsMenu(true);

    }

    private void setUpActionbar() {
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.update_profile_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case android.R.id.home:
                getActivity().finish();
                break;
            case R.id.action_update_profile:
                updateName();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if (etName != null) {
            MenuItem item = menu.findItem(R.id.action_update_profile);
            if (item != null) {
                item.setEnabled(etName.length() > 0);
            }
        }
    }

    void updateName() {
        showProgress();
        presenter.updateName(etName.getText().toString());
    }

    private void showProgress() {
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
        } else if (progressWheel.isShowing()) {
            return;
        }

        progressWheel.show();

    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @OnTextChanged(value = R.id.et_name_change, callback = OnTextChanged.Callback.TEXT_CHANGED)
    void onNameTextChanged(CharSequence text) {
        getActivity().invalidateOptionsMenu();
        presenter.onTextChange(text.toString());
    }

    @Override
    public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void setTextCount(int count) {
        tvCount.setText(String.format("%d/%d", count, NAME_MAX_LENGTH));

    }

    @Override
    public void successUpdate() {
        dismissProgress();
        getActivity().finish();
    }

    @Override
    public void setUser(User user) {
        etName.setText(user.getName());
        etName.setSelection(etName.length());
    }
}
