package com.tosslab.jandi.app.ui.carousel;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity_;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.FileSizeUtil;

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

import java.io.File;
import java.util.List;

/**
 * Created by Bill MinWook Heo on 15. 6. 23..
 */
@EActivity(R.layout.activity_carousel_viewer)
@OptionsMenu(R.menu.carousel_menu)
public class CarouselViewerActivity extends AppCompatActivity implements CarouselViewerPresenter.View {

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

    @Bean
    CarouselViewerModel carouselViewerModel;
    @Bean(CarouselViewerPresenterImpl.class)
    CarouselViewerPresenter carouselViewerPresenter;
    private CarouselViewerAdapter carouselViewerAdapter;
    private boolean isFullScreen = false;

    @AfterInject
    void initObject() {
        carouselViewerPresenter.setView(this);
    }


    @AfterViews
    public void initViews() {

        carouselViewerAdapter = new CarouselViewerAdapter(getSupportFragmentManager());
        carouselViewerAdapter.setCarouselImageClickListener(new OnCarouselImageClickListener() {
            @Override
            public void onCarouselImageClick() {

                isFullScreen = !isFullScreen;
                setUpFullScreen(isFullScreen);
            }
        });
        viewPager.setAdapter(carouselViewerAdapter);


        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                CarouselFileInfo fileInfo = carouselViewerAdapter.getFileInfo(position);

                getSupportActionBar().setTitle(fileInfo.getFileName());
                getSupportActionBar().setSubtitle(FileSizeUtil.fileSizeCalculation(fileInfo.getSize())
                        + ", " + fileInfo.getExt());
                tvFileWriterName.setText(fileInfo.getFileWriter());
                tvFileCreateTime.setText(fileInfo.getFileCreateTime());

            }
        });

        getImageFiles();
        setUpToolbar();
        setUpFullScreen(isFullScreen);

    }

    private void setUpFullScreen(boolean isFullScreen) {
        int systemUiOptions;
        ActionBar actionBar = getSupportActionBar();

        if (isFullScreen) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                systemUiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION // hide nav bar
                        | View.SYSTEM_UI_FLAG_FULLSCREEN // hide status bar
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            } else {
                // Do Nothing
                systemUiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            }

            // TODO 액션바 하단 툴바 숨기기
            if (actionBar != null) {
                actionBar.hide();
                vgCarouselBottom.setVisibility(View.GONE);
            }
        } else {
            // TODO 상태바, 액션바, 하단 툴바 노출
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                systemUiOptions = View.SYSTEM_UI_FLAG_IMMERSIVE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_FULLSCREEN;

            } else {
                systemUiOptions = View.SYSTEM_UI_FLAG_VISIBLE;
            }

            if (actionBar != null) {
                actionBar.show();
                vgCarouselBottom.setVisibility(View.VISIBLE);
            }
        }

        getWindow().getDecorView().setSystemUiVisibility(systemUiOptions);
    }

    void getImageFiles() {
        carouselViewerPresenter.getImageFiles(roomId, startLinkId, getApplicationContext());
    }


    @UiThread
    @Override
    public void addFileInfos(List<CarouselFileInfo> fileInfoList) {
        carouselViewerAdapter.addAll(fileInfoList);
        carouselViewerAdapter.notifyDataSetChanged();
    }

    @UiThread
    @Override
    public void setActionbarTitle(String fileName, String size, String ext) {
        getSupportActionBar().setTitle(fileName);
        getSupportActionBar().setSubtitle(size + ", " + ext);
    }

    @UiThread
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

        FileDetailActivity_
                .intent(this)
                .fileId(fileInfo.getFileLinkId())
                .start();

        finish();
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

        final ProgressDialog progressDialog = new ProgressDialog(CarouselViewerActivity.this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading " + fileInfo.getFileName());
        progressDialog.show();

        carouselViewerPresenter.onFileDownload(fileInfo, progressDialog);
    }

    @Click(R.id.iv_file_datail_info)
    void onMoveToFileDatail() {
        carouselViewerPresenter.onFileDatail();
    }

    @UiThread
    @Override
    public void downloadDone(File file, String fileType, ProgressDialog progressDialog) {

        progressDialog.dismiss();

        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(file), carouselViewerModel.getFileType(file, fileType));
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            String rawString = getString(R.string.err_unsupported_file_type);
            String formatString = String.format(rawString, file);
            ColoredToast.showError(this, formatString);
        }
    }

    @UiThread
    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(this, message);
    }

    public interface OnCarouselImageClickListener {
        void onCarouselImageClick();
    }

}
