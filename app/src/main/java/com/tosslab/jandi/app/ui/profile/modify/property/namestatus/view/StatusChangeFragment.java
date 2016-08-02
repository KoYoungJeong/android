package com.tosslab.jandi.app.ui.profile.modify.property.namestatus.view;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;

public class StatusChangeFragment extends Fragment implements NameStatusPresenter.View {
    public static final int STATUS_MAX_LENGTH = 30;

    @Bind(R.id.et_name_change)
    EditText etStatus;

    @Bind(R.id.tv_name_change_count)
    TextView tvCount;

    @Inject
    NameStatusPresenter presenter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_status_change, container);
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
                updateStatus();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateStatus() {
        presenter.updateStatus(etStatus.getText().toString());
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        if (etStatus != null) {
            MenuItem item = menu.findItem(R.id.action_update_profile);
            if (item != null) {
                item.setEnabled(etStatus.length() > 0);
            }
        }
    }

    @OnTextChanged(value = R.id.et_status_change, callback = OnTextChanged.Callback.TEXT_CHANGED)
    void onNameTextChanged(CharSequence text) {
        presenter.onTextChange(text.toString());
    }


    @Override
    public void onDestroy() {
        presenter.onDestroy();
        super.onDestroy();
    }

    @Override
    public void setTextCount(int count) {
        tvCount.setText(String.format("%d/%d", count, STATUS_MAX_LENGTH));
    }

    @Override
    public void successUpdate() {
        getActivity().finish();

    }

    @Override
    public void setUser(User user) {
        etStatus.setText(user.getStatusMessage());
    }
}
