package com.tosslab.jandi.app.ui.members.owner;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

/**
 * Created by tonyjs on 16. 1. 12..
 */
@EFragment
public class AssignTopicOwnerDialog extends DialogFragment {

    @FragmentArg
    String userName;

    @FragmentArg
    String profileUrl;

    private DialogInterface.OnClickListener confirmListener;

    public void setConfirmListener(DialogInterface.OnClickListener confirmListener) {
        this.confirmListener = confirmListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_assign_topic_owner, null);

        SimpleDraweeView ivProfile =
                (SimpleDraweeView) view.findViewById(R.id.iv_assign_topic_owner_profile);
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
