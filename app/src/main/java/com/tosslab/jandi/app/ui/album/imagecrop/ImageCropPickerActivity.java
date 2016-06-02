package com.tosslab.jandi.app.ui.album.imagecrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.util.Log;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import io.togoto.imagezoomcrop.cropoverlay.CropOverlayView;
import io.togoto.imagezoomcrop.photoview.PhotoView;

/**
 * Created by tee on 16. 2. 2..
 */

@EActivity(R.layout.activity_image_crop)
public class ImageCropPickerActivity extends BaseAppCompatActivity {

    @Bean
    ImageCropPickerViewModel imageCropPickerViewModel;

    @ViewById(R.id.iv_photo)
    PhotoView ivPhoto;

    @ViewById(R.id.crop_overlay)
    CropOverlayView cropOverlayView;

    @Extra("input")
    Uri originUri;

    @Extra("output")
    Uri saveUri;

    @AfterViews
    void initViews() {
        ivPhoto.setImageBoundsListener(() -> cropOverlayView.getImageBounds());
        initImage();
    }

    @Click(R.id.btn_cancel)
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void onClickCancelButton() {
        finish();
    }

    @Click(R.id.btn_ok)
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void onClickOkButton() {
        saveUploadCroppedImage();
    }

    private void initImage() {
        Bitmap bitmap = imageCropPickerViewModel.getBitmap(originUri);
        if (bitmap != null) {
            Drawable drawable = new BitmapDrawable(getResources(), bitmap);
            float minScale = ivPhoto.setMinimumScaleToFit(drawable);
            ivPhoto.setMaximumScale(minScale * 3);
            ivPhoto.setMediumScale(minScale * 2);
            ivPhoto.setScale(minScale);
            ivPhoto.setImageDrawable(drawable);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    void showUnexpectedErrorToast() {
        ColoredToast.show(R.string.jandi_err_unexpected);
    }

    private void saveUploadCroppedImage() {
        Bitmap croppedImage;
        try {
            croppedImage = ivPhoto.getCroppedImage();
        } catch (Exception e) {
            LogUtil.e("ImageCropPickerActivity", Log.getStackTraceString(e));
            showUnexpectedErrorToast();
            return;
        }

        boolean saved = imageCropPickerViewModel.saveOutput(croppedImage, saveUri);

        if (saved) {
            Intent intent = new Intent();
            intent.putExtra("output", saveUri);
            setResult(RESULT_OK, intent);
            finish();
        } else {
            // error
        }
    }

}
