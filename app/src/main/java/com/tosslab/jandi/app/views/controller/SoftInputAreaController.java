package com.tosslab.jandi.app.views.controller;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.uploadmenu.UploadMenuViewModel;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.views.SoftInputDetectLinearLayout;

/**
 * Created by tee on 16. 2. 23..
 */
public class SoftInputAreaController {

    private InputMethodManager inputMethodManager;
    private StickerViewModel stickerViewModel;
    private UploadMenuViewModel uploadMenuViewModel;

    private SoftInputDetectLinearLayout softInputDetector;
    private ViewGroup vgSoftInputArea;
    private EditText editText;
    private ImageView btnAction1;
    private ImageView btnAction2;

    private OnSoftInputAreaShowingListener onSoftInputAreaShowingListener;

    private ExpectPanel expectPanel = ExpectPanel.SOFT_INPUT;
    private OnUploadButtonClickListener onUploadButtonClickListener;
    private OnStickerButtonClickListener onStickerButtonClickListener;

    public SoftInputAreaController(StickerViewModel stickerViewModel,
                                   UploadMenuViewModel uploadMenuViewModel,
                                   SoftInputDetectLinearLayout softInputDetector,
                                   ViewGroup vgSoftInputArea,
                                   ImageView btnAction1,
                                   ImageView btnAction2,
                                   EditText editText) {
        this.softInputDetector = softInputDetector;
        this.vgSoftInputArea = vgSoftInputArea;
        this.btnAction1 = btnAction1;
        this.btnAction2 = btnAction2;
        this.stickerViewModel = stickerViewModel;
        this.uploadMenuViewModel = uploadMenuViewModel;
        this.editText = editText;
    }

    public void init() {
        initObjects();

        initSoftInputAreaHeight();

        replaceActionButtons(ExpectPanel.UPLOAD, ExpectPanel.STICKER);

        initSoftInputDetector();
    }

    void initObjects() {
        Context context = JandiApplication.getContext();
        inputMethodManager =
                (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
    }

    private void initSoftInputAreaHeight() {
        Context context = vgSoftInputArea.getContext();
        int keyboardHeight = JandiPreference.getKeyboardHeight(context);
        if (keyboardHeight <= 0) {
            keyboardHeight = (int) (ApplicationUtil.getDisplaySize(true) * 2 / 5f);
            JandiPreference.setKeyboardHeight(context, keyboardHeight);
        }

        ViewGroup.LayoutParams layoutParams = vgSoftInputArea.getLayoutParams();
        layoutParams.height = keyboardHeight;
        vgSoftInputArea.setLayoutParams(layoutParams);
    }

    private void replaceActionButtons(ExpectPanel expectPanel1, ExpectPanel expectPanel2) {
        if (btnAction1 != null) {
            switch (expectPanel1) {
                case UPLOAD:
                    initUploadButton(btnAction1);
                    break;
                case STICKER:
                    initStickerButton(btnAction1);
                    break;
                case SOFT_INPUT:
                    initSoftInputButton(btnAction1);
                    break;
            }
        }

        if (btnAction2 != null) {
            switch (expectPanel2) {
                case UPLOAD:
                    initUploadButton(btnAction2);
                    break;
                case STICKER:
                    initStickerButton(btnAction2);
                    break;
                case SOFT_INPUT:
                    initSoftInputButton(btnAction2);
                    break;
            }
        }

    }

    private void initSoftInputDetector() {
        softInputDetector.setOnSoftInputDetectListener((isSoftInputShowing, softInputHeight) -> {
            if (isSoftInputShowing) {

                setSoftInputAreaHeightIfNeed(softInputHeight);

                replaceSoftInputAreaAndActionButtons(expectPanel = ExpectPanel.SOFT_INPUT);

            } else {

                replaceSoftInputAreaAndActionButtons(expectPanel);

            }
        });
    }

    private void initSoftInputButton(ImageView button) {
        button.setImageResource(R.drawable.chat_icon_keypad);
        button.setOnClickListener(v -> {

            expectPanel = ExpectPanel.SOFT_INPUT;

            showSoftInput();

        });
    }

    private void initStickerButton(ImageView button) {
        button.setImageResource(R.drawable.chat_icon_emoticon);
        button.setOnClickListener(v -> {

            expectPanel = ExpectPanel.STICKER;

            if (softInputDetector.isSoftInputShowing()) {
                hideSoftInput();
            } else {
                replaceSoftInputAreaAndActionButtons(expectPanel);
            }

            if (onStickerButtonClickListener != null) {
                onStickerButtonClickListener.onStickerButtonClick();
            }
        });
    }

    private void initUploadButton(ImageView button) {
        button.setImageResource(R.drawable.chat_icon_upload);
        button.setOnClickListener(v -> {

            expectPanel = ExpectPanel.UPLOAD;

            if (softInputDetector.isSoftInputShowing()) {
                hideSoftInput();
            } else {
                replaceSoftInputAreaAndActionButtons(expectPanel);
            }

            if (onUploadButtonClickListener != null) {
                onUploadButtonClickListener.onUploadButtonClick();
            }
        });
    }

    public void replaceSoftInputAreaAndActionButtons(ExpectPanel expectPanel) {
        switch (expectPanel) {
            case UPLOAD:
                vgSoftInputArea.removeAllViews();

                uploadMenuViewModel.showUploadPanel(vgSoftInputArea);
                vgSoftInputArea.setVisibility(View.VISIBLE);

                replaceActionButtons(ExpectPanel.SOFT_INPUT, ExpectPanel.STICKER);
                break;

            case STICKER:
                vgSoftInputArea.removeAllViews();

                stickerViewModel.showStickerPanel(vgSoftInputArea);
                vgSoftInputArea.setVisibility(View.VISIBLE);

                replaceActionButtons(ExpectPanel.UPLOAD, ExpectPanel.SOFT_INPUT);
                break;

            case SOFT_INPUT:

                if (isSoftInputAreaShowing()) {
                    hideSoftInputArea(false, false);
                }

                replaceActionButtons(ExpectPanel.UPLOAD, ExpectPanel.STICKER);

                break;
        }

        if (onSoftInputAreaShowingListener != null) {
            onSoftInputAreaShowingListener.onShowing(
                    isSoftInputAreaShowing() || isSoftInputShowing(), getSoftInputAreaHeight());
        }

        this.expectPanel = ExpectPanel.SOFT_INPUT;
    }

    public boolean isSoftInputShowing() {
        return softInputDetector.isSoftInputShowing();
    }

    public void showSoftInput() {
        if (editText == null) {
            return;
        }

        editText.requestFocus();
        inputMethodManager.showSoftInput(editText, 0);
    }

    public void hideSoftInput() {
        if (editText == null) {
            return;
        }

        inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
    }

    public boolean isSoftInputAreaShowing() {
        return vgSoftInputArea != null && vgSoftInputArea.getVisibility() == View.VISIBLE;
    }

    public void hideSoftInputArea(boolean replaceActionButtons, boolean shouldNotifyToListener) {
        vgSoftInputArea.removeAllViews();
        vgSoftInputArea.setVisibility(View.GONE);

        if (replaceActionButtons) {
            replaceActionButtons(ExpectPanel.UPLOAD, ExpectPanel.STICKER);
        }

        if (shouldNotifyToListener) {
            if (onSoftInputAreaShowingListener != null) {
                onSoftInputAreaShowingListener.onShowing(false, getSoftInputAreaHeight());
            }
        }
    }

    public void onConfigurationChanged() {
        initSoftInputAreaHeight();
    }

    public void setSoftInputAreaHeightIfNeed(int softInputAreaHeight) {
        ViewGroup.LayoutParams layoutParams = vgSoftInputArea.getLayoutParams();
        if (layoutParams.height != softInputAreaHeight) {
            layoutParams.height = softInputAreaHeight;
            vgSoftInputArea.setLayoutParams(layoutParams);

            JandiPreference.setKeyboardHeight(vgSoftInputArea.getContext(), softInputAreaHeight);
        }
    }

    public void setOnSoftInputAreaShowingListener(
            OnSoftInputAreaShowingListener onSoftInputAreaShowingListener) {
        this.onSoftInputAreaShowingListener = onSoftInputAreaShowingListener;
    }

    public void hideSoftInputAreaAndShowSoftInput() {
        expectPanel = ExpectPanel.SOFT_INPUT;
        showSoftInput();
    }

    public int getSoftInputAreaHeight() {
        return vgSoftInputArea.getLayoutParams().height;
    }

    public void setOnUploadButtonClickListener(OnUploadButtonClickListener onUploadButtonClickListener) {
        this.onUploadButtonClickListener = onUploadButtonClickListener;
    }

    public void setOnStickerButtonClickListener(OnStickerButtonClickListener onStickerButtonClickListener) {
        this.onStickerButtonClickListener = onStickerButtonClickListener;
    }

    public enum ExpectPanel {
        UPLOAD, STICKER, SOFT_INPUT
    }

    public interface OnSoftInputAreaShowingListener {
        void onShowing(boolean isShowing, int softInputHeight);
    }

    public interface OnUploadButtonClickListener {
        void onUploadButtonClick();
    }

    public interface OnStickerButtonClickListener {
        void onStickerButtonClick();
    }

}
