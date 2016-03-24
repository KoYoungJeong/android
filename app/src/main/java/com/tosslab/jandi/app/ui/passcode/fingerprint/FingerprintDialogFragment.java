package com.tosslab.jandi.app.ui.passcode.fingerprint;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;

/**
 * Created by tonyjs on 16. 3. 24..
 */
public class FingerprintDialogFragment extends DialogFragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);
    }

//    @Nullable
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
//        return super.onCreateView(inflater, container, savedInstanceState);
//    }


    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.layout_fingerprint, null);
        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle("지문을 입력해주세요.")
                .setView(view)
                .setNegativeButton(R.string.jandi_cancel, (dialog, which) -> dismiss())
                .create();

    }
}
