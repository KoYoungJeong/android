package com.tosslab.jandi.app.ui.album.imagealbum;

import android.Manifest;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.SelectPictures;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@EActivity(R.layout.activity_image_album)
public class ImageAlbumActivity extends BaseAppCompatActivity {

    public static final String EXTRA_DATAS = "datas";
    public static final int REQ_STORAGE_PERMISSION = 101;

    public static final int EXTRA_MODE_CROP_PICK = 2;
    public static final int EXTRA_MODE_UPLOAD = 1;

    @Extra
    long entityId;

    @Extra
    int mode = EXTRA_MODE_UPLOAD;

    @ViewById(R.id.vg_image_album_content)
    ViewGroup contentLayout;

    @AfterViews
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
        Fragment fragment = ImageAlbumFragment_.builder()
                .mode(mode)
                .build();
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
        actionBar.setTitle(R.string.jandi_select_gallery);
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

    @OptionsItem(R.id.action_select_picture)
    void onSelectPicture() {
        ArrayList<String> selectedPicturesPathList = getSelectedPicturesPathList();

        if (!isOverFileSize(selectedPicturesPathList)) {

            FileUploadPreviewActivity_.intent(ImageAlbumActivity.this)
                    .realFilePathList(new ArrayList<String>(selectedPicturesPathList))
                    .selectedEntityIdToBeShared(entityId)
                    .from(FileUploadPreviewActivity.FROM_SELECT_IMAGE)
                    .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
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

    @OnActivityResult(FileUploadPreviewActivity.REQUEST_CODE)
    void onFileUploadActivityResult(int resultCode) {

        if (resultCode == Activity.RESULT_OK) {
            finish();
        }

    }

    private ArrayList<String> getSelectedPicturesPathList() {
        ArrayList<String> value = new ArrayList<String>();

        List<ImagePicture> pictures = SelectPictures.getSelectPictures().getPictures();

        for (ImagePicture picture : pictures) {
            value.add(picture.getImagePath());
        }
        return value;
    }

    private boolean hasSelectedPicture() {
        return SelectPictures.getSelectPictures().getPictures().size() > 0;
    }

    @OptionsItem(android.R.id.home)
    void onHomeMenuClick() {
        onBackPressed();
    }
}

