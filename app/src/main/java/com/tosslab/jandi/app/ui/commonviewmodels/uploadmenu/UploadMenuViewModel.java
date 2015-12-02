package com.tosslab.jandi.app.ui.commonviewmodels.uploadmenu;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.PixelFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.sticker.KeyboardHeightModel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import org.androidannotations.annotations.UiThread;

import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;

@EBean
public class UploadMenuViewModel {

    public static final int VIEW_HEIGHT_DEFAULT = -1;

    ViewGroup vgOptionSpace;

    WindowManager.LayoutParams params;

    @RootContext
    Context context;

    @Bean
    KeyboardHeightModel keyboardHeightModel;

    private FrameLayout vgUploadMenuSelector;
    private ImageView ivUploadImage;
    private ImageView ivUploadCamera;
    private ImageView ivUploadFile;

    private WindowManager windowManager;

    private boolean isShow = false;
    private boolean isKeyboardShow;

    private OnUploadLayoutShowListener onUploadLayoutShowListener;
    private OnClickUploadEventListener onClickUploadEventListener;

    @AfterViews
    void initViews() {
        vgUploadMenuSelector = new FrameLayout(context);
        vgUploadMenuSelector.setVisibility(View.GONE);
        LayoutInflater.from(context).inflate(R.layout.layout_listview_upload_menu, vgUploadMenuSelector, true);
        windowManager = (WindowManager) context.getSystemService(context.WINDOW_SERVICE);
        registKetboardCallback();
        ivUploadImage = (ImageView) vgUploadMenuSelector.findViewById(R.id.iv_upload_image);
        ivUploadCamera = (ImageView) vgUploadMenuSelector.findViewById(R.id.iv_upload_camera);
        ivUploadFile = (ImageView) vgUploadMenuSelector.findViewById(R.id.iv_upload_file);
        initClickEvent();
    }

    private void initClickEvent() {
        ivUploadImage.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestFileUploadEvent(FilePickerViewModel.TYPE_UPLOAD_GALLERY));
            dismissUploadSelector(true);
            onClickUploadEventListener.onClick();
        });
        ivUploadCamera.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestFileUploadEvent(FilePickerViewModel.TYPE_UPLOAD_TAKE_PHOTO));
            dismissUploadSelector(true);
            onClickUploadEventListener.onClick();
        });
        ivUploadFile.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestFileUploadEvent(FilePickerViewModel.TYPE_UPLOAD_EXPLORER));
            dismissUploadSelector(true);
            onClickUploadEventListener.onClick();
        });
    }

    private void registKetboardCallback() {
        keyboardHeightModel.addOnKeyboardShowListener(isShow -> {
            if (isShow) {
                isKeyboardShow = true;
                if (UploadMenuViewModel.this.isShowUploadSelector()) {
                    UploadMenuViewModel.this.dismissUploadSelector(true);
                }
            } else {
                isKeyboardShow = false;
            }
        });
    }

    public void setOptionSpace(ViewGroup optionSpace) {
        this.vgOptionSpace = optionSpace;
    }

    /**
     * @param height 높이
     */
    @UiThread
    public void showUploadSelector(int height) {
        Resources resources = vgUploadMenuSelector.getResources();

        int keyboardHeight;

        if (height < 0) {
            keyboardHeight = resources.getDisplayMetrics().heightPixels * 2 / 5;
        } else {
            keyboardHeight = height;
        }

        showUploadWindow(keyboardHeight);

        ViewGroup.LayoutParams layoutParams = vgUploadMenuSelector.getLayoutParams();
        ViewGroup.LayoutParams vgSpaceLayoutParams = vgOptionSpace.getLayoutParams();
        layoutParams.height = keyboardHeight;
        vgSpaceLayoutParams.height = keyboardHeight;
        vgUploadMenuSelector.setLayoutParams(layoutParams);
        vgOptionSpace.setLayoutParams(vgSpaceLayoutParams);

        vgUploadMenuSelector.setVisibility(View.VISIBLE);

        if (!isKeyboardShow) {
            vgOptionSpace.setVisibility(View.VISIBLE);
        } else {
            vgOptionSpace.setVisibility(View.GONE);
        }

        isShow = true;

        if (onUploadLayoutShowListener != null) {
            onUploadLayoutShowListener.onStickerLayoutShow(true);
        }
    }

    public void showUploadWindow(int height) {
        //최상위 윈도우에 넣기 위한 설정
        params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                height,
                WindowManager.LayoutParams.TYPE_PHONE,//항상 최 상위. 터치 이벤트 받을 수 있음.
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,  //포커스를 가지지 않음
                PixelFormat.TRANSLUCENT);                                        //투명
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        windowManager.addView(vgUploadMenuSelector, params);      //윈도우에 뷰 넣기. permission 필요.
    }

    public void dismissUploadWindow() {
        windowManager.removeView(vgUploadMenuSelector);
    }

    @UiThread
    public void dismissUploadSelector(boolean removeSpace) {
        if (isShow()) {
            dismissUploadWindow();
            vgUploadMenuSelector.setVisibility(View.GONE);
            if (removeSpace) {
                vgOptionSpace.setVisibility(View.GONE);
            }
            isShow = false;
            if (onUploadLayoutShowListener != null) {
                onUploadLayoutShowListener.onStickerLayoutShow(false);
            }
        }
    }

    public boolean isShowUploadSelector() {
        return vgUploadMenuSelector.getVisibility() == View.VISIBLE;
    }

    public boolean isShow() {
        return isShow;
    }

    public void onConfigurationChanged() {
        if (!isShowUploadSelector()) {
            return;
        }

        dismissUploadSelector(true);

        // 최소한 0.7 초를 주지 않으면 공간계산이 자동으로 되지 않는다. 특히 키보드 위에 스티커가 있는 경우 순간적으로 뷰가 전환되는
        // 시점에 많은 계산이 이루어 지는 것으로 보인다.
        if (keyboardHeightModel.isOpened()) {
            keyboardHeightModel.hideKeyboard();
            Observable.just(1)
                    .delay(700, TimeUnit.MILLISECONDS)
                    .subscribe(i -> {
                        showUploadSelector(VIEW_HEIGHT_DEFAULT);
                    });
        } else {
            showUploadSelector(VIEW_HEIGHT_DEFAULT);
        }
    }

    public void setOnUploadLayoutShowListener(OnUploadLayoutShowListener l) {
        this.onUploadLayoutShowListener = l;
    }

    public void setOnClickUploadEventListener(OnClickUploadEventListener l) {
        this.onClickUploadEventListener = l;
    }

    public interface OnUploadLayoutShowListener {
        void onStickerLayoutShow(boolean isShow);
    }

    public interface OnClickUploadEventListener {
        void onClick();
    }

}