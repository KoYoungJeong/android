package com.tosslab.jandi.app.ui.carousel;

import android.Manifest;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.ui.carousel.presenter.CarouselViewerPresenter;
import com.tosslab.jandi.app.ui.carousel.presenter.CarouselViewerPresenterImpl;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.OnSwipeExitListener;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.utils.file.FileUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by Bill MinWook Heo on 15. 6. 23..
 */
@EActivity(R.layout.activity_carousel_viewer)
@OptionsMenu(R.menu.carousel_menu)
public class CarouselViewerActivity extends BaseAppCompatActivity
        implements CarouselViewerPresenter.View, OnSwipeExitListener {

    private static final int REQ_STORAGE_PERMISSION = 101;
    @ViewById(R.id.vp_carousel)
    ViewPager viewPager;

    @ViewById(R.id.tv_file_writer_name)
    TextView tvFileWriterName;

    @ViewById(R.id.tv_file_create_time)
    TextView tvFileCreateTime;

    @ViewById(R.id.vg_carousel_bottom)
    LinearLayout vgCarouselBottom;

    @Extra
    int startLinkId;

    @Extra
    int roomId = -1;

    @Bean(CarouselViewerPresenterImpl.class)
    CarouselViewerPresenter carouselViewerPresenter;
    CarouselViewerAdapter carouselViewerAdapter;
    private boolean isFullScreen = false;

    @AfterInject
    void initObject() {
        carouselViewerPresenter.setView(this);
        carouselViewerPresenter.setFileId(startLinkId);
        carouselViewerPresenter.setRoomId(roomId);
    }


    @AfterViews
    public void initViews() {

//        Glide.get(getApplicationContext()).clearMemory();
//        Glide.get(getApplicationContext()).setMemoryCategory(MemoryCategory.HIGH);

        if (roomId <= 0) {
            finish();
            return;
        }

        setUpToolbar();

        carouselViewerAdapter = new CarouselViewerAdapter(getSupportFragmentManager());
        carouselViewerAdapter.setCarouselImageClickListener(() -> {
            isFullScreen = !isFullScreen;
            setUpFullScreen(isFullScreen);
        });
        viewPager.setAdapter(carouselViewerAdapter);


        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                CarouselFileInfo fileInfo = carouselViewerAdapter.getFileInfo(position);
                int count = carouselViewerAdapter.getCount();

                ActionBar actionBar = getSupportActionBar();
                if (actionBar != null) {
                    actionBar.setTitle(fileInfo.getFileName());
                    actionBar.setSubtitle(FileUtil.fileSizeCalculation(fileInfo.getSize())
                            + ", " + fileInfo.getExt());
                }
                tvFileWriterName.setText(fileInfo.getFileWriter());
                tvFileCreateTime.setText(fileInfo.getFileCreateTime());

                if (position == 0) {
                    carouselViewerPresenter.onBeforeImageFiles(fileInfo
                            .getFileLinkId(), count);
                } else {
                    if (position == count - 1) {
                        carouselViewerPresenter.onAfterImageFiles(fileInfo
                                .getFileLinkId(), count);
                    }
                }

            }
        });


        carouselViewerPresenter.onInitImageFiles();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpFullScreen(isFullScreen);
        ActivityHelper.setOrientation(this);
    }

    private void setUpFullScreen(boolean isFullScreen) {
        int systemUiOptions;
        ActionBar actionBar = getSupportActionBar();

        if (isFullScreen) {
            // Do Nothing
            systemUiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (actionBar != null) {
                actionBar.hide();
                vgCarouselBottom.setVisibility(View.GONE);
            }

        } else {
            systemUiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;

            if (actionBar != null) {
                actionBar.show();
                vgCarouselBottom.setVisibility(View.VISIBLE);
            }
        }

        getWindow().getDecorView().setSystemUiVisibility(systemUiOptions);
    }


    @UiThread
    @Override
    public void addFileInfos(List<CarouselFileInfo> fileInfoList) {

        carouselViewerAdapter.addAll(fileInfoList);
        carouselViewerAdapter.notifyDataSetChanged();

    }

    @UiThread
    @Override
    public void addFileInfos(int position, List<CarouselFileInfo> imageFiles) {

        carouselViewerAdapter.addAll(position, imageFiles);
        carouselViewerAdapter.notifyDataSetChanged();

        viewPager.setCurrentItem(imageFiles.size(), false);
    }

    @UiThread
    @Override
    public void setInitFail() {
        ColoredToast.showWarning(getString(R.string.err_download));
        finish();
    }

    @UiThread
    @Override
    public void movePosition(int startLinkPosition) {
        viewPager.setCurrentItem(startLinkPosition, false);
    }

    @UiThread
    @Override
    public void setActionbarTitle(String fileName, String size, String ext) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(fileName);
            actionBar.setSubtitle(size + ", " + ext);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void setFileWriterName(String fileWriterName) {
        tvFileWriterName.setText(fileWriterName);
    }

    @UiThread
    @Override
    public void setFileCreateTime(String fileCreateTime) {
        tvFileCreateTime.setText(fileCreateTime);
    }

    @Override
    public void moveToFileDatail() {
        CarouselFileInfo fileInfo = getCarouselFileInfo();

        if (fileInfo != null) {

            FileDetailActivity_
                    .intent(CarouselViewerActivity.this)
                    .flags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    .roomId(roomId)
                    .fileId(fileInfo.getFileLinkId())
                    .start();

            finish();
        }
    }

    private void setUpToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_carousel);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @OptionsItem(R.id.menu_carousel_close)
    void carouselClose() {
        finish();
    }

    private CarouselFileInfo getCarouselFileInfo() {
        int currentItem = viewPager.getCurrentItem();
        return ((CarouselViewerAdapter) viewPager.getAdapter()).getFileInfo(currentItem);
    }

    @Click(R.id.iv_file_download)
    void onFileDownload() {
        CarouselFileInfo fileInfo = getCarouselFileInfo();
        if (fileInfo != null) {

            Permissions.getChecker()
                    .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .hasPermission(() -> {
                        carouselViewerPresenter.onFileDownload(fileInfo);
                    })
                    .noPermission(() -> {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQ_STORAGE_PERMISSION);
                    }).check();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::onFileDownload)
                .resultPermission(Permissions.createPermissionResult(requestCode,
                        permissions,
                        grantResults));
    }

    @Click(R.id.iv_file_datail_info)
    void onMoveToFileDatail() {
        carouselViewerPresenter.onFileDatail();
    }

    @UiThread
    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(message);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    @Override
    public void onSwipeExit(int direction) {
        finish();

        int anim = R.anim.slide_out_to_bottom;
        if (direction == OnSwipeExitListener.DIRECTION_TO_TOP) {
            anim = R.anim.slide_out_to_top;
        }

        overridePendingTransition(0, anim);
    }

    public interface OnCarouselImageClickListener {
        void onCarouselImageClick();
    }
}
