package com.tosslab.jandi.app.ui.share.type.etc;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;

import com.iangclifton.android.floatlabel.FloatLabel;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.share.type.adapter.ShareEntityAdapter;
import com.tosslab.jandi.app.ui.share.type.to.EntityInfo;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import rx.Observable;

@EFragment(R.layout.fragment_share_image)
public class EtcShareDialogFragment extends DialogFragment implements EtcSharePresenter.View {

    @FragmentArg
    String uriString;

    @Bean
    EtcSharePresenter etcSharePresenter;
    @ViewById(R.id.spinner_share_image_entity)
    Spinner entitySpinner;
    @ViewById(R.id.img_share_image)
    ImageView shareImageView;
    @ViewById(R.id.txt_share_image_title)
    FloatLabel titleText;
    @ViewById(R.id.txt_share_image_comment)
    FloatLabel commentText;

    private ShareEntityAdapter shareEntityAdapter;

    @AfterInject
    void initObject() {

        etcSharePresenter.setView(this);

        shareEntityAdapter = new ShareEntityAdapter(getActivity());

        etcSharePresenter.onInitObject();
    }

    @AfterViews
    void initViews() {

        EditText titleEditText = titleText.getEditText();
        titleEditText.setImeOptions(EditorInfo.IME_ACTION_NEXT);
        titleEditText.setEnabled(false);
        titleEditText.setTextColor(Color.BLACK);

        commentText.getEditText().setMaxLines(4);

        shareImageView.setVisibility(View.GONE);

        entitySpinner.setAdapter(shareEntityAdapter);

        etcSharePresenter.onInitFile(uriString);

    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(R.string.jandi_share_to_jandi);
        return dialog;
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        getActivity().finish();
    }

    @Click(R.id.btn_share_text_send)
    void onSendClick() {

        EntityInfo selectedEntity = shareEntityAdapter.getItem(entitySpinner
                .getSelectedItemPosition());

        String title = titleText.getEditText().getText().toString().trim();
        String comment = commentText.getEditText().getText().toString().trim();
        etcSharePresenter.onSendFile(getActivity(), selectedEntity, title, comment, uriString);

    }

    @Override
    public void setTitle(String title) {
        titleText.getEditText().setText(title);
    }

    @Override
    public void setEntityInfos(List<EntityInfo> entities) {
        Observable.from(entities)
                .subscribe(shareEntityAdapter::add);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void finishOnMainThread() {
        getActivity().finish();
    }

    @Override
    public ProgressDialog getUploadProgress(String absolutePath, String name) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getString(R.string.jandi_upload) + " " + absolutePath + "/" + name);
        progressDialog.setCancelable(false);
        progressDialog.show();

        return progressDialog;

    }

    @UiThread
    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(getActivity(), message);
    }

    @UiThread
    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(getActivity(), message);
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissDialog(ProgressDialog uploadProgress) {
        if (uploadProgress != null && uploadProgress.isShowing()) {
            uploadProgress.dismiss();
        }
    }
}
