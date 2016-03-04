package com.tosslab.jandi.app.ui.message.v2.viewmodel;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.KeyboardHeightModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.StickerViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.uploadmenu.UploadMenuViewModel;
import com.tosslab.jandi.app.ui.message.to.StickerInfo;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Fragment;
import com.tosslab.jandi.app.ui.message.v2.MessageListV2Presenter;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.SdkUtils;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;

import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.UiThread;

import java.util.concurrent.TimeUnit;

import rx.Observable;

/**
 * Created by tee on 16. 2. 23..
 */

@EBean
public class KeyboardAreaController {

    ImageView btnActionButton1;
    ImageView btnActionButton2;
    ViewGroup vgKeyboardSpace;
    EditText etMessage;

    @RootContext
    Activity context;

    @SystemService
    InputMethodManager inputMethodManager;

    @Bean
    KeyboardHeightModel keyboardHeightModel;
    @Bean
    UploadMenuViewModel uploadMenuViewModel;
    @Bean
    StickerViewModel stickerViewModel;

    private MessageListV2Presenter presenter;

    private MessageListV2Presenter.View view;

    private boolean isInit = false;

    private ButtonAction buttonAction = ButtonAction.KEYBOARD;

    public void initKeyboardArea(ImageView button1, ImageView button2, ViewGroup keyboardSpace,
                                 EditText etMessage,
                                 MessageListV2Presenter.View view, MessageListV2Presenter presenter) {
        this.btnActionButton1 = button1;
        this.btnActionButton2 = button2;
        this.vgKeyboardSpace = keyboardSpace;
        this.etMessage = etMessage;
        this.view = view;
        this.presenter = presenter;
        initStickerViewModel();
        initUploadViewModel();
        initButtonClick();
        setActionButtons();
        initListeners();
        JandiPreference.setKeyboardHeight(JandiApplication.getContext(), -1);
        isInit = true;
    }

    private void initListeners() {
        keyboardHeightModel.addOnKeyboardShowListener((isShowing) -> {
            boolean visibility = keyboardHeightModel.isOpened()
                    || stickerViewModel.isShow() || uploadMenuViewModel.isShow();
//            presenter.setAnnouncementVisible(!visibility);
        });

        stickerViewModel.setOnStickerLayoutShowListener(isShow -> {
            boolean visibility = keyboardHeightModel.isOpened()
                    || stickerViewModel.isShow() || uploadMenuViewModel.isShow();
//            presenter.setAnnouncementVisible(!visibility);
        });

        uploadMenuViewModel.setOnUploadLayoutShowListener(isShow -> {
            boolean visibility = keyboardHeightModel.isOpened()
                    || stickerViewModel.isShow() || uploadMenuViewModel.isShow();
//            presenter.setAnnouncementVisible(!visibility);
        });

        uploadMenuViewModel.setOnClickUploadEventListener(() -> {
            if (keyboardHeightModel.isOpened()) {
                keyboardHeightModel.hideKeyboard();
            }
            buttonAction = ButtonAction.KEYBOARD;
            setActionButtons();
        });
    }

    private void initStickerViewModel() {
        stickerViewModel.setOptionSpace(vgKeyboardSpace);
        stickerViewModel.setOnStickerClick((groupId, stickerId) -> {
            StickerInfo newStickerInfo = new StickerInfo();
            newStickerInfo.setStickerGroupId(groupId);
            newStickerInfo.setStickerId(stickerId);
//            presenter.showStickerPreview(newStickerInfo);
//            view.setSendButtonEnabled(true);
//            presenter.sendAnalyticsEvent(AnalyticsValue.Action.Sticker_Select);
        });

//        stickerViewModel.setOnStickerDoubleTapListener((groupId, stickerId)
//                -> presenter.sendMessage(etMessage.getText().toString()));

//        stickerViewModel.setType(presenter.getChatTypeForSticker());

        stickerViewModel.setStickerButton(btnActionButton2);
    }

    private void initUploadViewModel() {
        uploadMenuViewModel.setOptionSpace(vgKeyboardSpace);
    }

    private void initButtonClick() {
        btnActionButton1.setOnClickListener(v -> {
            switch (buttonAction) {
                case KEYBOARD:
                    showUploadMenuSelectorIfNotShow();
                    break;
                case STICKER:
                    showUploadMenuSelectorIfNotShow();
                    break;
                case UPLOAD:
                    if (keyboardHeightModel.isOpened()) {
                        dismissUploadSelectorIfShow();
                    } else {
                        dismissUploadSelectorIfShow();
                        keyboardHeightModel.showKeyboard();
                    }
                    break;
            }
        });

        btnActionButton2.setOnClickListener(v -> {
            switch (buttonAction) {
                case KEYBOARD:
                    showStickerSelectorIfNotShow();
                    break;
                case UPLOAD:
                    showStickerSelectorIfNotShow();
                    break;
                case STICKER:
//                    presenter.sendAnalyticsEvent(AnalyticsValue.Action.Sticker);
                    if (keyboardHeightModel.isOpened()) {
                        dismissStickerSelectorIfShow();
                    } else {
                        dismissStickerSelectorIfShow();
                        keyboardHeightModel.showKeyboard();
                    }
                    break;
            }
        });
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setActionButtons() {
        if (!isInit) {
            return;
        }
        switch (buttonAction) {
            case STICKER:
                btnActionButton1.setImageResource(R.drawable.chat_icon_upload);
                btnActionButton2.setImageResource(R.drawable.chat_icon_keypad);
                break;
            case UPLOAD:
                btnActionButton1.setImageResource(R.drawable.chat_icon_keypad);
                btnActionButton2.setImageResource(R.drawable.chat_icon_emoticon);
                break;
            case KEYBOARD:
                btnActionButton1.setImageResource(R.drawable.chat_icon_upload);
                btnActionButton2.setImageResource(R.drawable.chat_icon_emoticon);
                break;
        }
    }

    public void showStickerSelectorIfNotShow() {
        int keyboardHeight = JandiPreference.getKeyboardHeight(JandiApplication.getContext());
        if (!stickerViewModel.isShow()) {
            if (isCanDrawWindowOverlay()) {
                stickerViewModel.showStickerSelector(keyboardHeight);
                Observable.just(1)
                        .delay(100, TimeUnit.MILLISECONDS)
                        .subscribe(i -> {
                            if (uploadMenuViewModel.isShow()) {
                                uploadMenuViewModel.dismissUploadSelector(false);
                            }
                        });
                buttonAction = ButtonAction.STICKER;
                setActionButtons();
            } else {
                // Android M (23) 부터 적용되는 시나리오
                requestWindowPermission();
            }
        }
    }

    public void showUploadMenuSelectorIfNotShow() {
        int keyboardHeight = JandiPreference.getKeyboardHeight(JandiApplication.getContext());
        if (!uploadMenuViewModel.isShow()) {
            if (isCanDrawWindowOverlay()) {
                Permissions.getChecker()
                        .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        .hasPermission(() -> {
                            uploadMenuViewModel.showUploadSelector(keyboardHeight);
                            Observable.just(1)
                                    .delay(100, TimeUnit.MILLISECONDS)
                                    .subscribe(i -> {
                                        if (stickerViewModel.isShow()) {
                                            stickerViewModel.dismissStickerSelector(false);
                                        }
                                    });
                            buttonAction = ButtonAction.UPLOAD;
                            setActionButtons();
//                            presenter.sendAnalyticsEvent(AnalyticsValue.Action.Upload);
                        })
                        .noPermission(() -> {
                            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            context.requestPermissions(permissions, MessageListV2Fragment.REQ_STORAGE_PERMISSION);
                        })
                        .check();
            } else {
                requestWindowPermission();
            }
        }
    }

    public void dismissStickerSelectorIfShow() {
        if (stickerViewModel.isShow()) {
            stickerViewModel.dismissStickerSelector(true);
            buttonAction = ButtonAction.KEYBOARD;
            setActionButtons();
        }
    }

    public void dismissUploadSelectorIfShow() {
        if (uploadMenuViewModel.isShow()) {
            uploadMenuViewModel.dismissUploadSelector(true);
            buttonAction = ButtonAction.KEYBOARD;
            setActionButtons();
        }
    }

    public void openKeyboardIfNotOpen() {
        if (buttonAction != ButtonAction.KEYBOARD) {
            if (buttonAction == ButtonAction.STICKER || buttonAction == ButtonAction.UPLOAD) {
                if (keyboardHeightModel.isOpened()) {
                    dismissStickerSelectorIfShow();
                    dismissUploadSelectorIfShow();
                } else {
                    keyboardHeightModel.showKeyboard();
                }
                buttonAction = ButtonAction.KEYBOARD;
                setActionButtons();
            }
        }
    }

    public void hideKeyboard() {
        if (inputMethodManager.isAcceptingText()) {
            inputMethodManager.hideSoftInputFromWindow(etMessage.getWindowToken(), 0);
        }
    }

    public boolean isKeyboardOpened() {
        return keyboardHeightModel.isOpened();
    }

    private boolean isCanDrawWindowOverlay() {
        boolean canDraw;
        if (SdkUtils.isMarshmallow()) {
            canDraw = Settings.canDrawOverlays(context);
        } else {
            canDraw = true;
        }
        return canDraw;
    }

    private void requestWindowPermission() {
        String packageName = JandiApplication.getContext().getPackageName();
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + packageName));
        context.startActivityForResult(intent, MessageListV2Fragment.REQ_WINDOW_PERMISSION);
    }

    public ButtonAction getCurrentButtonAction() {
        return buttonAction;
    }

    public void onConfigurationChanged(){
        stickerViewModel.onConfigurationChanged();
        uploadMenuViewModel.onConfigurationChanged();
    }

    public enum ButtonAction {
        UPLOAD, STICKER, KEYBOARD
    }

}
