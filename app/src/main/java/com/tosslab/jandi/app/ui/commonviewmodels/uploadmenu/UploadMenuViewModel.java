package com.tosslab.jandi.app.ui.commonviewmodels.uploadmenu;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;

import org.androidannotations.annotations.EBean;
import org.androidannotations.annotations.RootContext;
import de.greenrobot.event.EventBus;

@EBean
public class UploadMenuViewModel {

    @RootContext
    Context context;

    private View vgUploadMenuSelector;
    private ImageView ivUploadImage;
    private ImageView ivUploadCamera;
    private ImageView ivUploadFile;

    private boolean isShow = false;

    private OnClickUploadEventListener onClickUploadEventListener;

    public void showUploadPanel(ViewGroup root) {
        vgUploadMenuSelector =
                LayoutInflater.from(context).inflate(R.layout.layout_listview_upload_menu, root, true);
        ivUploadImage = (ImageView) vgUploadMenuSelector.findViewById(R.id.iv_upload_image);
        ivUploadCamera = (ImageView) vgUploadMenuSelector.findViewById(R.id.iv_upload_camera);
        ivUploadFile = (ImageView) vgUploadMenuSelector.findViewById(R.id.iv_upload_file);
        initClickEvent();
    }

    void initClickEvent() {
        ivUploadImage.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_GALLERY));
            if (onClickUploadEventListener != null) {
                onClickUploadEventListener.onClick();
            }
        });
        ivUploadCamera.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_TAKE_PHOTO));
            if (onClickUploadEventListener != null) {
                onClickUploadEventListener.onClick();
            }
        });
        ivUploadFile.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_EXPLORER));
            if (onClickUploadEventListener != null) {
                onClickUploadEventListener.onClick();
            }
        });
    }

    public boolean isShow() {
        return isShow;
    }

    public void setOnClickUploadEventListener(OnClickUploadEventListener l) {
        this.onClickUploadEventListener = l;
    }

    public interface OnClickUploadEventListener {
        void onClick();
    }

}