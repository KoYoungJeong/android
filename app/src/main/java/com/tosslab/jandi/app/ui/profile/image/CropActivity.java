package com.tosslab.jandi.app.ui.profile.image;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.files.upload.FilePickerViewModel;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.ImageFilePath;
import com.tosslab.jandi.app.views.CropView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;

/**
 * Created by Steve SeongUg Jung on 15. 7. 31..
 */
@EActivity(R.layout.activity_profile_crop)
public class CropActivity extends AppCompatActivity {

    @ViewById(R.id.iv_profile_crop)
    ImageView ivProfile;

    @ViewById(R.id.crop_profile)
    CropView cropView;

    private String imageFilePath;

    @AfterViews
    void initViews() {

        Intent intent = new Intent(Intent.ACTION_GET_CONTENT,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");
        startActivityForResult(intent, FilePickerViewModel.TYPE_UPLOAD_GALLERY);
    }


    @OnActivityResult(FilePickerViewModel.TYPE_UPLOAD_GALLERY)
    void onGalleryResult(int resultCode, Intent intent) {
        if (resultCode != RESULT_OK) {
            finish();
            return;
        }

        Uri data = intent.getData();

        String path = ImageFilePath.getPath(CropActivity.this, data);

        if (TextUtils.isEmpty(path)) {
            finish();
            return;
        }

        if (!GoogleImagePickerUtil.isUrl(path)) {
            setUpImageFile(path);
        } else {

            String downloadFileName = GoogleImagePickerUtil.getWebImageName();
            ProgressDialog progressDialog = getProgressDialog(downloadFileName);

            downloadImageFile(path, progressDialog, downloadFileName);
        }


    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();
        getMenuInflater().inflate(R.menu.profile_crop, menu);
        return true;
    }

    @Background
    void setUpImageFile(String path) {
        imageFilePath = path;

        Bitmap bitmap = BitmapUtil.getOptimizedBitmap(path);

        setUpToImageView(bitmap);

    }

    @UiThread
    void setUpToImageView(Bitmap bitmap) {
        ivProfile.setImageBitmap(bitmap);
    }

    private ProgressDialog getProgressDialog(String downloadFileName) {
        ProgressDialog progressDialog = new ProgressDialog(CropActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getString(R.string.jandi_file_uploading) + " " + downloadFileName);
        progressDialog.show();

        return progressDialog;
    }

    @Background
    void downloadImageFile(String url, ProgressDialog progressDialog, String downloadFileName) {
        try {
            File file = GoogleImagePickerUtil.downloadFile(CropActivity.this, progressDialog, url,
                    GoogleImagePickerUtil.getDownloadPath(), downloadFileName);
            setUpImageFile(file.getPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
