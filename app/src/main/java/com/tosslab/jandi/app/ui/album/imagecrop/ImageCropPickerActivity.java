package com.tosslab.jandi.app.ui.album.imagecrop;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.togoto.imagezoomcrop.cropoverlay.CropOverlayView;
import io.togoto.imagezoomcrop.photoview.PhotoView;

/**
 * Created by tee on 16. 2. 2..
 */


public class ImageCropPickerActivity extends BaseAppCompatActivity {

    @Bind(R.id.iv_photo)
    PhotoView ivPhoto;
    @Bind(R.id.crop_overlay)
    CropOverlayView cropOverlayView;

    private ImageCropPickerViewModel imageCropPickerViewModel;
    private Uri originUri;
    private Uri saveUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_crop);
        ButterKnife.bind(this);
        getExtras();
        imageCropPickerViewModel = new ImageCropPickerViewModel(this);
        ivPhoto.setImageBoundsListener(() -> cropOverlayView.getImageBounds());
        initImage();
    }

    private void getExtras() {
        originUri = getIntent().getParcelableExtra("input");
        saveUri = getIntent().getParcelableExtra("output");
    }


    @OnClick(R.id.btn_cancel)
    void onClickCancelButton() {
        finish();
    }

    @OnClick(R.id.btn_ok)
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
