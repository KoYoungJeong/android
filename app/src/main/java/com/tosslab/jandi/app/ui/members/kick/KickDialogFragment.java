package com.tosslab.jandi.app.ui.members.kick;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.transform.glide.GlideCircleTransform;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

@EFragment
public class KickDialogFragment extends DialogFragment {

    @FragmentArg
    String userName;

    @FragmentArg
    String profileUrl;

    private DialogInterface.OnClickListener onKickConfirmClickListener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_kick_user_topic, null);

        ImageView ivProfile = (ImageView) view.findViewById(R.id.iv_kick_user_topic_profile);
        TextView tvName = (TextView) view.findViewById(R.id.tv_kick_user_topic_name);

        Glide.with(KickDialogFragment.this)
                .load(profileUrl)
                .placeholder(R.drawable.profile_img)
                .error(R.drawable.profile_img)
                .transform(new GlideCircleTransform(getActivity()))
                .into(ivProfile);

        tvName.setText(userName);

        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setView(view)
                .setNegativeButton(R.string.jandi_cancel, null)
                .setPositiveButton(R.string.jandi_confirm, (dialog, which) -> {
                    if (onKickConfirmClickListener != null) {
                        onKickConfirmClickListener.onClick(dialog, which);
                    }
                })
                .create();


    }

    public void setOnKickConfirmClickListener(DialogInterface.OnClickListener onKickConfirmClickListener) {
        this.onKickConfirmClickListener = onKickConfirmClickListener;
    }
}
