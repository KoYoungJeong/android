package com.tosslab.jandi.app.ui.album.videoalbum;

import android.Manifest;
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
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.SelectVideos;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoAlbumActivity extends BaseAppCompatActivity {

    public static final int REQ_STORAGE_PERMISSION = 101;

    @Nullable
    @InjectExtra
    long entityId;

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
        SelectVideos.getSelectVideos().clear();

        setupActionbar();

        Permissions.getChecker()
                .activity(VideoAlbumActivity.this)
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(this::initFragment)
                .noPermission(() -> {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    ActivityCompat.requestPermissions(VideoAlbumActivity.this,
                            permissions,
                            REQ_STORAGE_PERMISSION);
                })
                .check();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(VideoAlbumActivity.this)
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::initFragment)
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(VideoAlbumActivity.this);
                })
                .resultPermission(Permissions.createPermissionResult(requestCode, permissions, grantResults));
    }

    private void initFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = VideoAlbumFragment.create(entityId);
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
        actionBar.setTitle(R.string.jandi_select_video);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}