package com.tosslab.jandi.app.ui.members.owner;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.image.ImageUtil;

public class AssignTopicOwnerDialog extends DialogFragment {

    @InjectExtra
    String userName;

    @InjectExtra
    String profileUrl;
    private DialogInterface.OnClickListener confirmListener;

    public static AssignTopicOwnerDialog create(String userName, String profileUrl) {
        AssignTopicOwnerDialog fragment = new AssignTopicOwnerDialog();
        Bundle args = new Bundle();
        args.putString("userName", userName);
        args.putString("profileUrl", profileUrl);
        fragment.setArguments(args);
        return fragment;
    }

    public void setConfirmListener(DialogInterface.OnClickListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Dart.inject(this, getArguments());
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_assign_topic_owner, null);

        ImageView ivProfile =
                (ImageView) view.findViewById(R.id.iv_assign_topic_owner_profile);
        TextView tvName = (TextView) view.findViewById(R.id.tv_assign_topic_owner_name);

        ImageUtil.loadProfileImage(ivProfile, profileUrl, R.drawable.profile_img);

        tvName.setText(userName);

        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setView(view)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, confirmListener)
                .create();

    }

}
