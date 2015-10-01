package com.tosslab.jandi.app.ui.album;

import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.ui.album.fragment.ImageAlbumFragment_;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.fragment.vo.SelectPictures;
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

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
@EActivity(R.layout.activity_image_album)
public class ImageAlbumActivity extends BaseAppCompatActivity {

    public static final String EXTRA_DATAS = "datas";

    @Extra
    int entityId;

    @ViewById(R.id.vg_image_album_content)
    ViewGroup contentLayout;

    @AfterViews
    void initViews() {

        SelectPictures.getSelectPictures().clear();

        setupActionbar();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = ImageAlbumFragment_.builder().build();
        fragmentTransaction.replace(R.id.vg_image_album_content, fragment);
        fragmentTransaction.commit();

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

        if (hasSelectedPicture()) {
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
                    .startForResult(FileUploadPreviewActivity.REQUEST_CODE);
        } else {
            ColoredToast.showError(ImageAlbumActivity.this, getString(R.string.err_file_upload_failed));
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

