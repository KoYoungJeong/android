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
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.views.SoftInputDetectLinearLayout;

/**
 * Created by tee on 16. 2. 23..
 */
public class SoftInputAreaController {

    private InputMethodManager inputMethodManager;
    private StickerViewModel stickerViewModel;

    private SoftInputDetectLinearLayout softInputDetector;
    private ViewGroup vgSoftInputArea;
    private EditText editText;
    private ImageView btnAction2;
    private ViewGroup vgBtnAction2;

    private OnSoftInputAreaShowingListener onSoftInputAreaShowingListener;

    private ExpectPanel expectPanel = ExpectPanel.SOFT_INPUT;
    private OnStickerButtonClickListener onStickerButtonClickListener;

    private boolean isShowKeyboard = false;

    public SoftInputAreaController(StickerViewModel stickerViewModel,
                                   SoftInputDetectLinearLayout softInputDetector,
                                   ViewGroup vgSoftInputArea,
                                   ImageView btnAction2,
                                   ViewGroup vgBtnAction2,
                                   EditText editText) {
        this.softInputDetector = softInputDetector;
        this.vgSoftInputArea = vgSoftInputArea;
        this.btnAction2 = btnAction2;
        this.stickerViewModel = stickerViewModel;
        this.editText = editText;
        this.vgBtnAction2 = vgBtnAction2;
    }

    public void init() {
        initObjects();

        initSoftInputAreaHeight();

        replaceActionButtons(ExpectPanel.STICKER);

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

    private void replaceActionButtons(ExpectPanel expectPanel) {
        if (btnAction2 != null) {
            switch (expectPanel) {
                case STICKER:
                    initStickerButton(btnAction2, vgBtnAction2);
                    break;
                case SOFT_INPUT:
                    initSoftInputButton(btnAction2, vgBtnAction2);
                    break;
            }
        }
    }

    private void initSoftInputDetector() {
        softInputDetector.setOnSoftInputDetectListener((isSoftInputShowing, softInputHeight) -> {
            if (isSoftInputShowing) {
                isShowKeyboard = true;
                setSoftInputAreaHeightIfNeed(softInputHeight);
                replaceSoftInputAreaAndActionButtons(expectPanel = ExpectPanel.SOFT_INPUT);
            } else {
                isShowKeyboard = false;
                replaceSoftInputAreaAndActionButtons(expectPanel);
            }
        });
    }

    private void initSoftInputButton(ImageView button, ViewGroup vgButton) {
        button.setImageResource(R.drawable.chat_icon_keypad);
        if (vgButton != null) {
            vgButton.setOnClickListener(v -> {
                onClickSoftInputButton();
            });
        } else {
            button.setOnClickListener(v -> {
                onClickSoftInputButton();
            });
        }
    }

    private void onClickSoftInputButton() {
        expectPanel = ExpectPanel.SOFT_INPUT;
        showSoftInput();
    }

    private void initStickerButton(ImageView button, ViewGroup vgButton) {
        button.setImageResource(R.drawable.chat_icon_emoticon);
        if (vgButton != null) {
            vgButton.setOnClickListener(v -> {
                onClickStickerButton();
            });
        } else {
            button.setOnClickListener(v -> {
                onClickStickerButton();
            });
        }
    }

    private void onClickStickerButton() {
        expectPanel = ExpectPanel.STICKER;

        if (softInputDetector.isSoftInputShowing()) {
            hideSoftInput();
        } else {
            replaceSoftInputAreaAndActionButtons(expectPanel);
        }

        if (onStickerButtonClickListener != null) {
            onStickerButtonClickListener.onStickerButtonClick();
        }
    }

    public void replaceSoftInputAreaAndActionButtons(ExpectPanel expectPanel) {
        switch (expectPanel) {
            case STICKER:
                vgSoftInputArea.removeAllViews();

                stickerViewModel.showStickerPanel(vgSoftInputArea);
                vgSoftInputArea.setVisibility(View.VISIBLE);

                replaceActionButtons(ExpectPanel.SOFT_INPUT);
                break;
            case SOFT_INPUT:
                if (isSoftInputAreaShowing()) {
                    hideSoftInputArea(false, false);
                }

                replaceActionButtons(ExpectPanel.STICKER);

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
            replaceActionButtons(ExpectPanel.STICKER);
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

    public boolean isShowSoftInput() {
        return isShowKeyboard;
    }

    public void setOnStickerButtonClickListener(OnStickerButtonClickListener onStickerButtonClickListener) {
        this.onStickerButtonClickListener = onStickerButtonClickListener;
    }

    public enum ExpectPanel {
        STICKER, SOFT_INPUT
    }

    public interface OnSoftInputAreaShowingListener {
        void onShowing(boolean isShowing, int softInputHeight);
    }

    public interface OnStickerButtonClickListener {
        void onStickerButtonClick();
    }

}
