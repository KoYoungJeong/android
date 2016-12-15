package com.tosslab.jandi.app.ui.team.create.invite;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.invites.InviteDialogExecutor;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by tee on 16. 6. 27..
 */
public class InviteTeamMemberFragment extends Fragment {
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_invite_team, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(R.id.tv_invite_button)
    public void onClickInvite() {
        InviteDialogExecutor.getInstance().executeInvite(getContext());
    }

    @OnClick(R.id.iv_browse_team)
    public void onClickBrowseTeam() {
        getActivity().setResult(Activity.RESULT_OK);
        getActivity().finish();
    }
}
