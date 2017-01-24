package com.tosslab.jandi.app.ui.carousel;

import android.Manifest;
import android.animation.Animator;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.DeleteFileEvent;
import com.tosslab.jandi.app.events.files.FileCommentRefreshEvent;
import com.tosslab.jandi.app.events.messages.MessageStarEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.permissions.Check;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.authority.Level;
import com.tosslab.jandi.app.team.room.DirectMessageRoom;
import com.tosslab.jandi.app.team.room.TopicRoom;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.carousel.dagger.CarouselViewerModule;
import com.tosslab.jandi.app.ui.carousel.dagger.DaggerCarouselViewerComponent;
import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.ui.carousel.model.CarouselViewerModel;
import com.tosslab.jandi.app.ui.carousel.presenter.CarouselViewerPresenter;
import com.tosslab.jandi.app.ui.filedetail.FileDetailActivity;
import com.tosslab.jandi.app.ui.filedetail.views.FileSharedEntityChooseActivity;
import com.tosslab.jandi.app.ui.maintab.tabs.file.FileListFragment;
import com.tosslab.jandi.app.ui.search.filter.room.RoomFilterActivity;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.OnSwipeExitListener;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimatorListener;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class CarouselViewerActivity extends BaseAppCompatActivity
        implements CarouselViewerPresenter.View, OnSwipeExitListener {

    public static final long CAROUSEL_MODE = 0;
    public static final long SINGLE_IMAGE_MODE = 1;
    public static final String KEY_FILE_ID = "file_id";
    private static final int REQ_STORAGE_PERMISSION = 101;

    @Bind(R.id.vp_carousel)
    ViewPager viewPager;

    @Bind(R.id.tv_carousel_file_title)
    TextView tvFileTitle;

    @Bind(R.id.tv_carousel_file_writer)
    TextView tvFileWriterName;

    @Bind(R.id.tv_carousel_file_create_time)
    TextView tvFileCreateTime;

    @Bind(R.id.tv_carousel_file_info)
    TextView tvFileInfo;

    @Bind(R.id.tv_carousel_file_comment)
    TextView tvFileComment;

    @Bind(R.id.btn_carousel_star)
    ImageView btnStar;

    @Bind(R.id.vg_carousel_infos)
    ViewGroup vgCarouselInfos;

    @Bind(R.id.vg_carousel_swipe_buttons)
    ViewGroup vgSwipeButtons;

    @Bind(R.id.btn_carousel_swipe_to_left)
    View btnSwipeToLeft;

    @Bind(R.id.btn_carousel_swipe_to_right)
    View btnSwipeToRight;

    @Bind(R.id.toolbar_carousel)
    Toolbar toolbar;

    @Nullable
    @InjectExtra
    long startMessageId;

    @Nullable
    @InjectExtra
    long roomId = -1;

    @Nullable
    @InjectExtra
    long mode = CAROUSEL_MODE;

    @Nullable
    @InjectExtra
    CarouselFileInfo singleImageInfo;

    @Nullable
    @InjectExtra
    boolean shouldOpenImmediately = false;

    @Nullable
    @InjectExtra
    boolean fromFileDetail = false;

    @Inject
    CarouselViewerPresenter carouselViewerPresenter;

    @Inject
    ClipboardManager clipboardManager;

    CarouselViewerAdapter carouselViewerAdapter;

    private boolean isFullScreen = false;
    private ProgressWheel progressWheel;

    public static CarouselViewerActivity$$IntentBuilder getCarouselViewerIntent(
            Activity activity, long startMessageId, long roomId) {
        return Henson.with(activity)
                .gotoCarouselViewerActivity()
                .roomId(roomId)
                .startMessageId(startMessageId)
                .mode(CAROUSEL_MODE);
    }

    // for only use SINGLE_IMAGE_MODE
    public static CarouselViewerActivity$$IntentBuilder getImageViewerIntent(
            Activity activity, ResMessages.FileMessage fileMessage) {
        CarouselFileInfo carouselFileInfo =
                CarouselViewerModel.getCarouselInfoFromFileMessage(-1, fileMessage);
        return Henson.with(activity)
                .gotoCarouselViewerActivity()
                .singleImageInfo(carouselFileInfo)
                .mode(SINGLE_IMAGE_MODE);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Dart.inject(this);
        if (mode == CAROUSEL_MODE && roomId <= 0) {
            finish();
            return;
        }

        setShouldSetOrientation(false);

        setupStatusBar();

        injectComponent();

        setContentView(R.layout.activity_carousel_viewer);

        ButterKnife.bind(this);

        initProgressWheel();

        setUpToolbar();

        initViews();

        EventBus.getDefault().register(this);
    }

    private void setupStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
            getWindow().setStatusBarColor(getResources().getColor(R.color.jandi_transparent_black_60p));
        }
    }

    private void injectComponent() {
        DaggerCarouselViewerComponent.builder()
                .carouselViewerModule(new CarouselViewerModule(this))
                .build()
                .inject(this);
    }

    public void initViews() {
        carouselViewerAdapter = new CarouselViewerAdapter(getSupportFragmentManager());
        carouselViewerAdapter.setCarouselImageClickListener(() -> {
            isFullScreen = !isFullScreen;
            setFullScreen(isFullScreen);
        });
        viewPager.setAdapter(carouselViewerAdapter);
        viewPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {

            private int latestPosition = -1;

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);

                if (mode != CAROUSEL_MODE) {
                    return;
                }

                CarouselFileInfo fileInfo = carouselViewerAdapter.getFileInfo(position);
                initCarouselInfo(fileInfo);

                int count = carouselViewerAdapter.getCount();

                setSwipeButtons(position, count);

                if (position == 0) {
                    carouselViewerPresenter.onBeforeImageFiles(roomId, fileInfo.getFileMessageId(), count);
                } else {
                    if (position == count - 1) {
                        carouselViewerPresenter.onAfterImageFiles(roomId, fileInfo.getFileMessageId(), count);
                    }
                }

                if (latestPosition <= -1) {
                    latestPosition = position;
                } else {
                    if (position > latestPosition) {
                        AnalyticsValue.Action action = AnalyticsValue.Action.MoveToRight_Swipe;
                        sendAnalyticsEvent(action);
                    } else if (position < latestPosition) {
                        sendAnalyticsEvent(AnalyticsValue.Action.MoveToLeft_Swipe);
                    }
                }
            }
        });

        if (mode == CAROUSEL_MODE) {
            vgSwipeButtons.setVisibility(View.VISIBLE);
            carouselViewerPresenter.onInitImageFiles(roomId, startMessageId);
        } else if (mode == SINGLE_IMAGE_MODE) {
            vgSwipeButtons.setVisibility(View.GONE);
            carouselViewerPresenter.onInitImageSingleFile(singleImageInfo);
        }
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action) {
        AnalyticsValue.Screen screen = mode == SINGLE_IMAGE_MODE
                ? AnalyticsValue.Screen.ImageFullScreen
                : AnalyticsValue.Screen.Carousel;
        AnalyticsUtil.sendEvent(screen, action);
    }

    private void sendAnalyticsEvent(AnalyticsValue.Action action, AnalyticsValue.Label label) {
        AnalyticsValue.Screen screen = mode == SINGLE_IMAGE_MODE
                ? AnalyticsValue.Screen.ImageFullScreen
                : AnalyticsValue.Screen.Carousel;
        AnalyticsUtil.sendEvent(screen, action, label);
    }

    private void setSwipeButtons(int position, int itemCount) {
        if (itemCount <= 1) {
            return;
        }

        setVisibilitySwipeToLeftButton(position != 0);
        setVisibilitySwipeToRightButton(!(position == itemCount - 1));
    }

    @Override
    public void initCarouselInfo(CarouselFileInfo fileInfo) {
        setFileTitle(fileInfo.getFileName());
        setFileWriterName(fileInfo.getFileWriterName());
        setFileCreateTime(fileInfo.getFileCreateTime());

        String fileSize = fileInfo.getSize() > 0 ? FileUtil.formatFileSize(fileInfo.getSize()) : "";
        setFileInfo(fileSize, fileInfo.getExt());

        setFileComments(fileInfo.getFileCommentCount());
        setFileStarredState(fileInfo.isStarred(), false);

        supportInvalidateOptionsMenu();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setFullScreen(isFullScreen);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return true;
        }

        if (carouselFileInfo.getFileWriterId() == TeamInfoLoader.getInstance().getMyId()
                || (TeamInfoLoader.getInstance().getMyLevel() == Level.Admin ||
                TeamInfoLoader.getInstance().getMyLevel() == Level.Owner)) {
            getMenuInflater().inflate(R.menu.carousel_activity_my_menu, menu);
        } else {
            getMenuInflater().inflate(R.menu.carousel_activity_menu, menu);
        }

        SubMenu subMenu = menu.findItem(R.id.menu_overflow).getSubMenu();
        if (carouselFileInfo.isExternalFileShared()) {
            MenuItem menuItem = subMenu.findItem(R.id.action_file_detail_enable_external_link);
            menuItem.setTitle(R.string.jandi_copy_link);
        } else {
            subMenu.removeItem(R.id.action_file_detail_disable_external_link);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_file_detail_share:
                share();
                break;
            case R.id.action_file_detail_unshare:
                unShare();
                break;
            case R.id.action_file_detail_export:
                export();
                break;
            case R.id.action_file_detail_delete:
                delete();
                break;
            case R.id.action_file_detail_enable_external_link:
                enableExternalLink();
                break;
            case R.id.action_file_detail_disable_external_link:
                disableExternalLink();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    void share() {
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return;
        }

        RoomFilterActivity.startForResultWithTopicId(this, -1,
                FileDetailActivity.REQUEST_CODE_SHARE);

        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Share);
    }

    void unShare() {
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return;
        }

        long[] sharedEntities = getSharedEntitiesArray(carouselFileInfo.getSharedEntities());
        startActivityForResult(Henson.with(this)
                        .gotoFileSharedEntityChooseActivity()
                        .fileId(carouselFileInfo.getFileMessageId())
                        .sharedEntities(sharedEntities)
                        .build(),
                FileDetailActivity.REQUEST_CODE_UNSHARE);

        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_UnShare);
    }

    private long[] getSharedEntitiesArray(List<Long> sharedEntities) {
        long[] sharedEntitiesArray = new long[sharedEntities.size()];
        for (int i = 0; i < sharedEntitiesArray.length; i++) {
            // 기존 FileSharedEntityChooseActivity가 DM ID정보를 User 정보로 가지고 있기 때문에 변환해 주어야 함
            if (TeamInfoLoader.getInstance().isChat(sharedEntities.get(i))) {
                DirectMessageRoom directMessageRoom =
                        TeamInfoLoader.getInstance().getChat(sharedEntities.get(i));
                sharedEntitiesArray[i] = directMessageRoom.getCompanionId();
            } else {
                sharedEntitiesArray[i] = sharedEntities.get(i);
            }
        }
        return sharedEntitiesArray;
    }

    void export() {
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return;
        }

        ProgressDialog progressDialog = new ProgressDialog(CarouselViewerActivity.this);
        progressDialog.setMax(100);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setCanceledOnTouchOutside(false);

        carouselViewerPresenter.onExportFile(carouselFileInfo, progressDialog);
    }

    void delete() {
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return;
        }

        AlertUtil.showConfirmDialog(this, R.string.jandi_action_delete,
                R.string.jandi_file_delete_message, (dialog, which) -> {
                    carouselViewerPresenter.onDeleteFile(carouselFileInfo.getFileMessageId());
                }, true);

        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Delete);
    }

    void enableExternalLink() {
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return;
        }

        if (carouselFileInfo.isExternalFileShared()) {
            setExternalLinkToClipboard();
            return;
        }

        carouselViewerPresenter.onEnableExternalLink(carouselFileInfo);
        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_CreatePublicLink);

    }

    public void copyToClipboard(String text) {
        if (TextUtils.isEmpty(text)) {
            return;
        }

        ClipData clipData = ClipData.newPlainText(null, text);
        clipboardManager.setPrimaryClip(clipData);
    }

    private String getExternalLink(String externalCode) {
        StringBuffer link = new StringBuffer(JandiConstantsForFlavors.getServiceBaseUrl())
                .append("file/")
                .append(externalCode);
        return link.toString();
    }

    void disableExternalLink() {
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return;
        }

        AlertUtil.showDialog(this,
                R.string.jandi_disable_external_link,
                R.string.jandi_are_you_sure_disable_external_link,
                R.string.jandi_action_delete, (dialog, which) -> {
                    carouselViewerPresenter.onDisableExternalLink(carouselFileInfo);
                }, /* positive */
                -1, null, /* neutral */
                R.string.jandi_cancel, null, /* negative */
                true);
        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_DeleteLink);

    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        carouselViewerPresenter.clearAllEventQueue();
        super.onDestroy();
    }

    private void setFullScreen(boolean isFullScreen) {
        ActionBar actionBar = getSupportActionBar();

        if (isFullScreen) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN);

            if (actionBar != null) {
                actionBar.hide();
                vgCarouselInfos.setVisibility(View.GONE);
            }

        } else {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            if (actionBar != null) {
                actionBar.show();
                vgCarouselInfos.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void addFileInfos(List<CarouselFileInfo> fileInfoList) {

        carouselViewerAdapter.addAll(fileInfoList);
        carouselViewerAdapter.notifyDataSetChanged();

    }

    @Override
    public void addFileInfos(int position, List<CarouselFileInfo> imageFiles) {

        carouselViewerAdapter.addAll(position, imageFiles);
        carouselViewerAdapter.notifyDataSetChanged();

        viewPager.setCurrentItem(imageFiles.size(), false);
    }

    @Override
    public void setInitFail() {
        ColoredToast.showWarning(getString(R.string.err_download));
        finish();
    }

    @Override
    public void movePosition(int startLinkPosition) {
        viewPager.setCurrentItem(startLinkPosition, false);
    }

    @Override
    public void setFileTitle(String fileName) {
        tvFileTitle.setText(fileName);
    }

    @Override
    public void setFileWriterName(String fileWriterName) {
        tvFileWriterName.setText(fileWriterName);
    }

    @Override
    public void setFileCreateTime(String fileCreateTime) {
        tvFileCreateTime.setText(fileCreateTime);
    }

    @Override
    public void setFileInfo(String size, String ext) {
        StringBuilder sb = new StringBuilder();
        if (!TextUtils.isEmpty(size)) {
            sb.append(size + ", ");
        }
        sb.append(ext);

        tvFileInfo.setText(sb.toString());
    }

    @Override
    public void setFileComments(int fileCommentCount) {
        String comments = getString(R.string.jandi_comment_count, fileCommentCount);
        tvFileComment.setText(comments);
    }

    @Override
    public void showStarredSuccessToast() {
        ColoredToast.show(getString(R.string.jandi_message_starred));
    }

    @Override
    public void showUnstarredSuccessToast() {
        ColoredToast.show(getString(R.string.jandi_unpinned_message));
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
    }

    private CarouselFileInfo getCarouselFileInfo() {
        int currentItem = viewPager.getCurrentItem();
        return ((CarouselViewerAdapter) viewPager.getAdapter()).getFileInfo(currentItem);
    }

    @OnClick(R.id.btn_carousel_swipe_to_left)
    void swipeToLeft() {
        int currentItem = viewPager.getCurrentItem();

        if (currentItem - 1 >= 0) {
            viewPager.setCurrentItem(currentItem - 1);
            sendAnalyticsEvent(AnalyticsValue.Action.MoveToLeft_Click);
        }
    }

    @OnClick(R.id.btn_carousel_swipe_to_right)
    void swipeToRight() {
        int currentItem = viewPager.getCurrentItem();

        if (currentItem + 1 < carouselViewerAdapter.getCount()) {
            viewPager.setCurrentItem(currentItem + 1);
            sendAnalyticsEvent(AnalyticsValue.Action.MoveToRight_Click);
        }
    }

    @OnClick(R.id.btn_carousel_download)
    void onFileDownload() {
        sendAnalyticsEvent(AnalyticsValue.Action.Download);

        CarouselFileInfo fileInfo = getCarouselFileInfo();
        if (fileInfo != null) {

            Permissions.getChecker()
                    .activity(CarouselViewerActivity.this)
                    .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    .hasPermission(() -> carouselViewerPresenter.onFileDownload(fileInfo))
                    .noPermission(() -> {
                        ActivityCompat.requestPermissions(CarouselViewerActivity.this,
                                new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                REQ_STORAGE_PERMISSION);
                    })
                    .check();
        }
    }

    @OnClick(R.id.btn_carousel_star)
    void onFileStar(View view) {
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        long fileMessageId = carouselFileInfo.getFileMessageId();

        boolean futureStarred = !carouselFileInfo.isStarred();
        carouselFileInfo.setIsStarred(futureStarred);

        setFileStarredState(futureStarred, true);

        carouselViewerPresenter.onChangeStarredState(fileMessageId, futureStarred);

        sendAnalyticsEvent(AnalyticsValue.Action.Star,
                futureStarred ? AnalyticsValue.Label.On : AnalyticsValue.Label.Off);
    }

    private void setFileStarredState(boolean futureStarred, boolean withAnimation) {
        int iconResId = futureStarred
                ? R.drawable.image_view_icon_star_on : R.drawable.image_view_icon_star_off;

        btnStar.setImageResource(iconResId);

        if (withAnimation && futureStarred) {
            btnStar.setScaleX(0.9f);
            btnStar.setScaleY(0.9f);

            btnStar.animate()
                    .scaleX(1.2f)
                    .scaleY(1.2f)
                    .setDuration(100)
                    .setListener(new SimpleEndAnimatorListener() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            btnStar.animate()
                                    .scaleX(1.0f)
                                    .scaleY(1.0f)
                                    .setListener(null);
                        }
                    });
        } else {
            btnStar.setScaleX(1.0f);
            btnStar.setScaleY(1.0f);
        }
    }

    @OnClick({R.id.btn_carousel_comment, R.id.tv_carousel_file_comment})
    void moveToFileDetailActivity(View view) {
        if (view.getId() == R.id.btn_carousel_comment) {
            sendAnalyticsEvent(AnalyticsValue.Action.TapCommentIcon);
        } else {
            sendAnalyticsEvent(AnalyticsValue.Action.TapCommentCount);
        }

        if (fromFileDetail) {
            finish();
            return;
        }
        long fileId = getCarouselFileInfo() != null
                ? getCarouselFileInfo().getFileMessageId()
                : -1;
        startActivity(Henson.with(this)
                .gotoFileDetailActivity()
                .fromCarousel(true)
                .roomId(mode == CAROUSEL_MODE ? roomId : -1)
                .fileId(fileId)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(CarouselViewerActivity.this)
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::onFileDownload)
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(CarouselViewerActivity.this);
                })
                .resultPermission(Permissions.createPermissionResult(requestCode,
                        permissions,
                        grantResults));
    }

    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(message);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    public void onEvent(MessageStarEvent event) {
        long messageId = event.getMessageId();
        boolean starred = event.isStarred();

        CarouselFileInfo current = getCarouselFileInfo();

        if (mode == SINGLE_IMAGE_MODE) {
            if (current.getFileMessageId() == messageId
                    && current.isStarred() != starred) {
                Observable.just(1)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(o -> {
                            current.setIsStarred(starred);
                            initCarouselInfo(current);
                        });
            }
        } else if (mode == CAROUSEL_MODE) {
            Observable.from(carouselViewerAdapter.getFileInfos())
                    .takeFirst(carouselFileInfo ->
                            carouselFileInfo.getFileMessageId() == messageId
                                    && carouselFileInfo.isStarred() != starred)
                    .firstOrDefault(new CarouselFileInfo.Builder().create())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(carouselFileInfo -> {
                        if (carouselFileInfo.getFileMessageId() > 0) {
                            carouselFileInfo.setIsStarred(starred);
                        }
                    });
        }
    }

    @Override
    public void dismissDialog(ProgressDialog dialog) {
        if (isFinishing()) {
            return;
        }

        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }

    @Override
    public void showUnexpectedErrorToast() {
        ColoredToast.showError(R.string.jandi_err_unexpected);
    }

    @Override
    public void exportLink(String fileOriginalUrl) {
        Intent target = new Intent(Intent.ACTION_SEND);
        target.putExtra(Intent.EXTRA_TEXT, fileOriginalUrl);
        target.setType("text/plain");
        try {
            Intent chooser = Intent.createChooser(target, getString(R.string.jandi_export_to_app));
            startActivity(chooser);
            sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_Export);
        } catch (ActivityNotFoundException e) {
            LogUtil.e(Log.getStackTraceString(e));
            ColoredToast.showError(R.string.jandi_err_unexpected);
        }
    }

    @Override
    public void showDialog(ProgressDialog dialog) {
        if (isFinishing()) {
            return;
        }

        if (dialog != null && !dialog.isShowing()) {
            dialog.show();
        }
    }

    @Override
    public void startExportedFileViewerActivity(File file, String mimeType) {
        Intent target = FileUtil.createFileIntent(file, mimeType);
        target.setAction(Intent.ACTION_SEND);
        Bundle extras = new Bundle();
        Uri uri = Uri.fromFile(file);
        extras.putParcelable(Intent.EXTRA_STREAM, uri);
        target.putExtras(extras);
        try {
            Intent chooser = Intent.createChooser(target, getString(R.string.jandi_export_to_app));
            startActivity(chooser);
        } catch (ActivityNotFoundException e) {
            LogUtil.e(Log.getStackTraceString(e));
            showUnexpectedErrorToast();
        }
    }

    @Override
    public void startDownloadedFileViewerActivity(File file, String mimeType) {
        try {
            ColoredToast.show(getString(R.string.jandi_file_downloaded_into, file.getPath()));
            startActivity(FileUtil.createFileIntent(file, mimeType));
        } catch (ActivityNotFoundException | SecurityException e) {
            ColoredToast.showError(getString(R.string.err_unsupported_file_type, file));
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    @Override
    public void requestPermission(int requestCode, String... permissions) {
        super.requestPermissions(permissions, requestCode);
    }

    @Override
    public void checkPermission(String persmissionString, Check.HasPermission hasPermission, Check.NoPermission noPermission) {
        Permissions.getChecker()
                .activity(CarouselViewerActivity.this)
                .permission(() -> persmissionString)
                .hasPermission(hasPermission)
                .noPermission(noPermission)
                .check();
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(this);
        progressWheel.setCanceledOnTouchOutside(false);
        progressWheel.setCancelable(false);
    }

    @Override
    public void showProgress() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void dismissProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showDeleteSuccessToast() {
        ColoredToast.show(getString(R.string.jandi_image_deleted));
    }

    @Override
    public void showDeleteErrorToast() {
        ColoredToast.showError(R.string.err_delete_file);
    }

    @Override
    public void setExternalLinkToClipboard() {
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return;
        }

        String externalLink = getExternalLink(carouselFileInfo.getExternalCode());
        copyToClipboard(externalLink);
        ColoredToast.show(R.string.jandi_success_copy_clipboard_external_link);
        sendAnalyticsEvent(AnalyticsValue.Action.FileSubMenu_CopyLink);

    }

    @Override
    public void showDisableExternalLinkSuccessToast() {
        ColoredToast.show(R.string.jandi_success_disable_external_link);
    }

    @Override
    public void showShareErrorToast() {
        ColoredToast.showError(R.string.err_share);
    }

    @Override
    public void showMoveToSharedTopicDialog(long entityId) {
        AlertUtil.showDialog(this,
                -1, /* title */
                R.string.jandi_move_entity_after_share, /* message */
                R.string.jandi_confirm, ((dialog, which) -> {
                    moveToSharedEntity(entityId);
                }), /* positive */
                -1, null,  /* neutral */
                R.string.jandi_cancel, null, /* cancel */
                true /* cancellable */);
    }

    private void moveToSharedEntity(long entityId) {
        TeamInfoLoader teamInfoLoader = TeamInfoLoader.getInstance();

        int entityType;
        boolean isStarred = false;
        boolean isUser = false;
        boolean isBot = false;

        if (teamInfoLoader.isTopic(entityId)) {
            if (teamInfoLoader.isPublicTopic(entityId)) {
                entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
            } else {
                entityType = JandiConstants.TYPE_PRIVATE_TOPIC;
            }
            isStarred = teamInfoLoader.isStarred(entityId);
        } else if (teamInfoLoader.isUser(entityId)) {
            entityType = JandiConstants.TYPE_DIRECT_MESSAGE;
            isStarred = teamInfoLoader.isStarredUser(entityId);
            isUser = true;
            if (teamInfoLoader.isJandiBot(entityId)) {
                isBot = true;
            }
        } else {
            entityType = JandiConstants.TYPE_PUBLIC_TOPIC;
        }

        if (isUser || isBot) {
            moveToMessageListActivity(entityId, entityType, -1, isStarred);
            return;
        } else {
            TopicRoom topic = teamInfoLoader.getTopic(entityId);
            if (topic.isJoined()) {

                moveToMessageListActivity(entityId, entityType, entityId, isStarred);
            } else {
                carouselViewerPresenter.joinAndMove(topic);
            }
        }
    }

    @Override
    public void moveToMessageListActivity(long entityId, int entityType, long roomId,
                                          boolean isStarred) {
        startActivity(Henson.with(this)
                .gotoMessageListV2Activity()
                .teamId(TeamInfoLoader.getInstance().getTeamId())
                .entityId(entityId)
                .entityType(entityType)
                .roomId(roomId)
                .isFromPush(false)
                .build()
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }

    @Override
    public void showUnshareErrorToast() {
        CharSequence title = tvFileTitle.getText();
        ColoredToast.showError(getString(R.string.jandi_unshare_succeed, title));
    }

    public void onEvent(DeleteFileEvent event) {
        if (isFinishing()) {
            return;
        }

        long fileMessageId = event.getId();
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo.getFileMessageId() == fileMessageId) {

            carouselViewerPresenter.onImageFileDeleted();

        } else if (mode == CAROUSEL_MODE) {

            carouselViewerPresenter.onImageFileDeleted(
                    carouselViewerAdapter.getFileInfos(), fileMessageId);

        }
    }

    @Override
    public void remove(CarouselFileInfo fileInfo) {
        carouselViewerAdapter.remove(fileInfo);
    }

    public void onEvent(FileCommentRefreshEvent event) {
        long fileMessageId = event.getFileId();
        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();

        if (carouselFileInfo.getFileMessageId() == fileMessageId) {

            carouselViewerPresenter.onSocketCommentEvent(carouselFileInfo, event.isAdded());

        } else {
            if (mode == CAROUSEL_MODE) {
                carouselViewerPresenter.onSocketCommentEvent(
                        carouselViewerAdapter.getFileInfos(), fileMessageId, event.isAdded());
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case FileDetailActivity.REQUEST_CODE_SHARE:
                onShareResult(resultCode, data);
                break;
            case FileDetailActivity.REQUEST_CODE_UNSHARE:
                onUnshareResult(resultCode, data);
                break;
        }

    }

    void onShareResult(int resultCode, Intent data) {

        if (resultCode != RESULT_OK || data == null) {
            return;
        }

        boolean isTopic = data.getBooleanExtra(RoomFilterActivity.KEY_IS_TOPIC, true);

        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return;
        }

        long entityId = -1;

        if (isTopic) {
            entityId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_ROOM_ID, -1);
        } else {
            entityId = data.getLongExtra(RoomFilterActivity.KEY_FILTERED_MEMBER_ID, -1);
        }

        carouselViewerPresenter.onShareAction(entityId, carouselFileInfo);
    }

    void onUnshareResult(int resultCode, Intent data) {
        if (resultCode != RESULT_OK
                || data == null
                || !data.hasExtra(FileSharedEntityChooseActivity.KEY_ENTITY_ID)) {
            return;
        }

        CarouselFileInfo carouselFileInfo = getCarouselFileInfo();
        if (carouselFileInfo == null) {
            return;
        }

        long entityId = data.getLongExtra(FileSharedEntityChooseActivity.KEY_ENTITY_ID, -1);
        carouselViewerPresenter.onUnshareAction(entityId, carouselFileInfo);
    }

    @Override
    public void onSwipeExit(int direction) {
        sendAnalyticsEvent(AnalyticsValue.Action.CloseBySwipe);

        finish();

        int anim = R.anim.slide_out_to_bottom;
        if (direction == OnSwipeExitListener.DIRECTION_TO_TOP) {
            anim = R.anim.slide_out_to_top;
        }

        overridePendingTransition(0, anim);
    }

    @Override
    public void finish() {
        CarouselFileInfo fileInfo = carouselViewerAdapter.getFileInfo(viewPager.getCurrentItem());
        if (fileInfo != null) {
            Intent data = new Intent();
            data.putExtra(KEY_FILE_ID, fileInfo.getFileMessageId());
            setResult(RESULT_OK, data);
        }

        super.finish();
    }

    @Override
    public void deletedFinish(long fileId) {
        if (fileId >= 0) {
            Intent data = new Intent();
            data.putExtra(FileListFragment.KEY_FILE_DELETED, true);
            data.putExtra(FileListFragment.KEY_FILE_ID, fileId);
            setResult(RESULT_OK, data);
        }

        super.finish();
    }

    @Override
    public void notifyDataSetChanged() {
        carouselViewerAdapter.notifyDataSetChanged();
    }

    @Override
    public void setVisibilitySwipeToLeftButton(boolean show) {
        btnSwipeToLeft.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void setVisibilitySwipeToRightButton(boolean show) {
        btnSwipeToRight.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        sendAnalyticsEvent(AnalyticsValue.Action.Close);
        super.onBackPressed();
    }

    public interface OnCarouselImageClickListener {
        void onCarouselImageClick();
    }
}
