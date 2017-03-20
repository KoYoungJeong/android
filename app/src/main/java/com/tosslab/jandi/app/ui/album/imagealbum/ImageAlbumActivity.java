package com.tosslab.jandi.app.ui.album.imagealbum;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.SelectPictures;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.utils.ColoredToast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageAlbumActivity extends BaseAppCompatActivity {

    public static final String EXTRA_DATAS = "datas";
    public static final int REQ_STORAGE_PERMISSION = 101;

    public static final int EXTRA_MODE_CROP_PICK = 2;
    public static final int EXTRA_MODE_UPLOAD = 1;

    @Nullable
    @InjectExtra
    long entityId;

    @Nullable
    @InjectExtra
    int mode = EXTRA_MODE_UPLOAD;

    @Bind(R.id.vg_image_album_content)
    ViewGroup contentLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_album);
        ButterKnife.bind(this);
        Dart.inject(this);

        initViews();

    }

    void initViews() {
        SelectPictures.getSelectPictures().clear();

        setupActionbar();

        Permissions.getChecker()
                .activity(ImageAlbumActivity.this)
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(this::initFragment)
                .noPermission(() -> {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(ImageAlbumActivity.this,
                            permissions,
                            REQ_STORAGE_PERMISSION);
                })
                .check();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(ImageAlbumActivity.this)
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::initFragment)
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(ImageAlbumActivity.this);
                })
                .resultPermission(Permissions.createPermissionResult(requestCode, permissions, grantResults));
    }

    private void initFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = ImageAlbumFragment.create(mode);
        fragmentTransaction.replace(R.id.vg_image_album_content, fragment);
        fragmentTransaction.commitAllowingStateLoss();
    }

    private void setupActionbar() {
        if (getSupportActionBar() == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_image_album);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("비디오 선택");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.clear();

        if (mode == EXTRA_MODE_UPLOAD && hasSelectedPicture()) {
            getMenuInflater().inflate(R.menu.select_picture, menu);
        }

        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_select_picture) {
            onSelectPicture();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onSelectPicture() {
        List<String> selectedPicturesPathList = getSelectedPicturesPathList();

        if (!isOverFileSize(selectedPicturesPathList)) {

            startActivityForResult(Henson.with(this)
                    .gotoFileUploadPreviewActivity()
                    .realFilePathList(new ArrayList<>(selectedPicturesPathList))
                    .selectedEntityIdToBeShared(entityId)
                    .from(FileUploadPreviewActivity.FROM_SELECT_IMAGE)
                    .build(), (FileUploadPreviewActivity.REQUEST_CODE));
        } else {
            ColoredToast.showError(getString(R.string.err_file_upload_failed));
        }
    }

    private boolean isOverFileSize(List<String> selectedPicturesPathList) {
        File uploadFile;
        for (String filePath : selectedPicturesPathList) {
            uploadFile = new File(filePath);
            if (uploadFile.exists()) {
                if (uploadFile.length() > FilePickerModel.MAX_FILE_SIZE) {
                    return true;
                }
            }

        }
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FileUploadPreviewActivity.REQUEST_CODE) {
            onFileUploadActivityResult(resultCode);
        }
    }

    void onFileUploadActivityResult(int resultCode) {

        if (resultCode == Activity.RESULT_OK) {
            finish();
        }

    }

    private List<String> getSelectedPicturesPathList() {
        List<String> value = new ArrayList<>();

        List<ImagePicture> pictures = SelectPictures.getSelectPictures().getPictures();

        for (ImagePicture picture : pictures) {
            value.add(picture.getImagePath());
        }
        return value;
    }

    private boolean hasSelectedPicture() {
        return SelectPictures.getSelectPictures().getPictures().size() > 0;
    }

}

