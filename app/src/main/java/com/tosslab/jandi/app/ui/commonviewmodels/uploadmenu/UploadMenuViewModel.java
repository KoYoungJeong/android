package com.tosslab.jandi.app.ui.commonviewmodels.uploadmenu;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.RequestFileUploadEvent;
import com.tosslab.jandi.app.events.poll.RequestCreatePollEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;

import de.greenrobot.event.EventBus;

public class UploadMenuViewModel {

    private View vgUploadMenuSelector;
    private View btnUploadImage;
    private View btnShowCamera;
    private View btnUploadFile;
    private View btnCreatePoll;

    private boolean isShow = false;
    private RoomType roomType = RoomType.TOPIC;

    private OnClickUploadEventListener onClickUploadEventListener;

    public void showUploadPanel(ViewGroup root) {
        vgUploadMenuSelector =
                LayoutInflater.from(root.getContext()).inflate(R.layout.layout_upload_menu, root, true);
        btnUploadImage = vgUploadMenuSelector.findViewById(R.id.btn_upload_menu_choose_image);
        btnShowCamera = vgUploadMenuSelector.findViewById(R.id.btn_upload_menu_show_camera);
        btnUploadFile = vgUploadMenuSelector.findViewById(R.id.btn_upload_menu_choose_file);
        btnCreatePoll = vgUploadMenuSelector.findViewById(R.id.btn_upload_menu_create_poll);

        if (roomType == RoomType.DM) {
            btnCreatePoll.setVisibility(View.GONE);
        }

        initClickEvent();
    }

    void initClickEvent() {
        btnUploadImage.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_GALLERY));
            if (onClickUploadEventListener != null) {
                onClickUploadEventListener.onClick();
            }
        });
        btnShowCamera.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_TAKE_PHOTO));
            if (onClickUploadEventListener != null) {
                onClickUploadEventListener.onClick();
            }
        });
        btnUploadFile.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestFileUploadEvent(FileUploadController.TYPE_UPLOAD_EXPLORER));
            if (onClickUploadEventListener != null) {
                onClickUploadEventListener.onClick();
            }
        });
        btnCreatePoll.setOnClickListener(v -> {
            EventBus.getDefault().post(new RequestCreatePollEvent());
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

    public void setRoomType(RoomType roomType) {
        this.roomType = roomType;
    }

    public enum RoomType {
        DM, TOPIC
    }

    public interface OnClickUploadEventListener {
        void onClick();
    }

}