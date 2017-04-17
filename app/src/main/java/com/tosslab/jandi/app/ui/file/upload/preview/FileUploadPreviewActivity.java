package com.tosslab.jandi.app.ui.file.upload.preview;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadPreviewImageClickEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadPagerAdapter;
import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadThumbAdapter;
import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadThumbDivideItemDecorator;
import com.tosslab.jandi.app.ui.file.upload.preview.dagger.DaggerFileUploadComponent;
import com.tosslab.jandi.app.ui.file.upload.preview.dagger.FileUploadModule;
import com.tosslab.jandi.app.ui.file.upload.preview.presenter.FileUploadPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.views.PricingPlanWarningViewController;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import net.yslibrary.android.keyboardvisibilityevent.KeyboardVisibilityEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnTextChanged;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

public class FileUploadPreviewActivity extends BaseAppCompatActivity implements FileUploadPresenter.View {

    public static final int REQUEST_CODE = 17863;
    public static final String KEY_SINGLE_FILE_UPLOADVO = "file_uploadvo";
    public static final int FROM_SELECT_IMAGE = 0x01;
    public static final int FROM_TAKE_PHOTO = 0x02;
    public static final int FROM_SELECT_FILE = 0x03;
    public static final int FROM_SELECT_VIDEO = 0x04;

    @Nullable
    @InjectExtra
    long selectedEntityIdToBeShared;    // Share 할 chat-room

    @Nullable
    @InjectExtra
    boolean singleUpload = false;

    @Nullable
    @InjectExtra
    int from = FROM_SELECT_IMAGE;

    @Nullable
    @InjectExtra
    ArrayList<String> realFilePathList = new ArrayList<>();
    @Inject
    FileUploadPresenter fileUploadPresenter;

    InputMethodManager inputMethodManager;

    @Bind(R.id.vp_file_upload_preview)
    ViewPager vpFilePreview;
    @Bind(R.id.tv_file_upload_title)
    TextView tvFileTitle;
    @Bind(R.id.tv_file_upload_entity)
    TextView tvEntity;
    @Bind(R.id.et_file_upload_comment)
    EditText etComment;
    @Bind({R.id.iv_file_upload_preview_previous, R.id.iv_file_upload_preview_next})
    List<ImageView> scrollButtons;
    @Bind(R.id.lv_file_upload_thumbs)
    RecyclerView lvthumb;
    @Bind(R.id.vg_upload_info_bottom_sheet)
    View vgUploadInfoBottomSheet;
    @Bind(R.id.vg_file_upload_preview_content_entity)
    ViewGroup vgFileUploadPreviewContentEntity;
    @Bind(R.id.v_restrict_warning)
    View vRestrictWarning;
    @Bind(R.id.vg_restrict_warning)
    ViewGroup vgRestrictWarning;


    @Bind(R.id.vg_coordinator)
    ViewGroup vgCoordinator;

    private boolean thumbNailViewVisible = false;
    private MentionControlViewModel mentionControlViewModel;
    private PublishSubject<Object> scrollButtonPublishSubject;
    private Subscription subscribe;
    private FileUploadThumbAdapter adapter;
    private BottomSheetBehavior bottomSheetBehavior;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedUnLockPassCode(false);
        setContentView(R.layout.activity_file_upload_insert_comment);
        ButterKnife.bind(this);
        Dart.inject(this);

        DaggerFileUploadComponent.builder()
                .fileUploadModule(new FileUploadModule(this))
                .build()
                .inject(this);

        initView();
    }

    void initView() {
        inputMethodManager = ((InputMethodManager) getSystemService(INPUT_METHOD_SERVICE));
        setupActionbar();

        fileUploadPresenter.setView(this);
        fileUploadPresenter.onInitEntity(selectedEntityIdToBeShared, realFilePathList);

        scrollButtonPublishSubject = PublishSubject.create();
        subscribe = scrollButtonPublishSubject.throttleWithTimeout(3000, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(o -> {
                    for (ImageView scrollButton : scrollButtons) {
                        if (scrollButton.getVisibility() != View.GONE) {

                            AlphaAnimation animation = new AlphaAnimation(1f, 0f);
                            animation.setDuration(300);
                            animation.setStartTime(AnimationUtils.currentAnimationTimeMillis());
                            animation.setAnimationListener(new SimpleEndAnimationListener() {
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    scrollButton.setVisibility(View.GONE);
                                }
                            });
                            scrollButton.startAnimation(animation);
                        }
                    }
                }, throwable -> {
                });

        TextCutter.with(etComment)
                .listener((s) -> {
                    SuperToast.cancelAllSuperToasts();
                    ColoredToast.showError(R.string.jandi_exceeded_max_text_length);
                });

        if (realFilePathList.size() > 1) {
            lvthumb.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            adapter = new FileUploadThumbAdapter();
            adapter.setItemClickListener((view, adapter1, position) -> {
                vpFilePreview.setCurrentItem(position);
            });
            lvthumb.setAdapter(adapter);
            lvthumb.addItemDecoration(new FileUploadThumbDivideItemDecorator());

            fileUploadPresenter.initThumbInfo(realFilePathList);
            thumbNailViewVisible = true;

            etComment.setMaxLines(18);
        } else {
            thumbNailViewVisible = false;
            lvthumb.setVisibility(View.GONE);
            etComment.setMaxLines(21);
        }

        vgUploadInfoBottomSheet.setOnTouchListener((v, event) -> true);

        bottomSheetBehavior = BottomSheetBehavior.from(vgUploadInfoBottomSheet);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        etComment.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_UP:
                    v.getParent().requestDisallowInterceptTouchEvent(false);
                    break;
            }
            return false;
        });

        KeyboardVisibilityEvent.setEventListener(
                this, isOpen -> {
                    if (isOpen) {
                        if (vgRestrictWarning.getVisibility() == View.VISIBLE) {
                            vgRestrictWarning.setVisibility(View.INVISIBLE);
                        }
                        etComment.setMaxLines(9);
                        vgFileUploadPreviewContentEntity.setVisibility(View.GONE);
                        Completable.complete()
                                .delay(400, TimeUnit.MILLISECONDS)
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(() -> {
                                    bottomSheetBehavior.setPeekHeight((int) UiUtils.getPixelFromDp(169.5f));
                                });
                    } else {
                        if (vgRestrictWarning.getVisibility() == View.INVISIBLE) {
                            if (etComment.getLineCount() > 1) {
                                vgRestrictWarning.setVisibility(View.GONE);
                            } else {
                                vgRestrictWarning.setVisibility(View.VISIBLE);
                            }
                        }
                        vgFileUploadPreviewContentEntity.setVisibility(View.VISIBLE);
                        if (lvthumb.getVisibility() == View.VISIBLE) {
                            etComment.setMaxLines(18);
                        } else {
                            etComment.setMaxLines(21);
                        }
                    }
                });

        setPricingLimitView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        setNeedUnLockPassCode(true);

        if (mentionControlViewModel != null) {
            mentionControlViewModel.registClipboardListener();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Observable.just(1, 1)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (mentionControlViewModel != null) {
                        mentionControlViewModel.onConfigurationChanged();
                    }
                });

    }

    @Override
    protected void onPause() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }
        EventBus.getDefault().unregister(this);
        super.onPause();
    }

    public void onEvent(SelectedMemberInfoForMentionEvent event) {
        SearchedItemVO searchedItemVO = new SearchedItemVO();
        searchedItemVO.setId(event.getId());
        searchedItemVO.setName(event.getName());
        searchedItemVO.setType(event.getType());
        if (mentionControlViewModel != null) {
            mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
        }
    }

    public void onEventMainThread(FileUploadPreviewImageClickEvent event) {

        ActionBar actionBar = getSupportActionBar();
        if (vgUploadInfoBottomSheet.getVisibility() != View.VISIBLE) {
            // 보이도록 하기, 배경 흰색
            vgUploadInfoBottomSheet.setVisibility(View.VISIBLE);
            if (vgRestrictWarning.getVisibility() == View.INVISIBLE) {
                vgRestrictWarning.setVisibility(View.VISIBLE);
            }
            if (thumbNailViewVisible) {
                lvthumb.setVisibility(View.VISIBLE);
            }
            vpFilePreview.setBackgroundColor(Color.WHITE);
            if (actionBar != null) {
                actionBar.show();
            }
            RelativeLayout.LayoutParams nextScrollLayoutParams =
                    (RelativeLayout.LayoutParams) scrollButtons.get(0).getLayoutParams();
            nextScrollLayoutParams.setMargins(0, (int) UiUtils.getPixelFromDp(245), 0, 0);
            nextScrollLayoutParams.removeRule(RelativeLayout.CENTER_VERTICAL);
            scrollButtons.get(0).setLayoutParams(nextScrollLayoutParams);
            RelativeLayout.LayoutParams prevScrollLayoutParams =
                    (RelativeLayout.LayoutParams) scrollButtons.get(1).getLayoutParams();
            prevScrollLayoutParams.setMargins(0, (int) UiUtils.getPixelFromDp(245), 0, 0);
            prevScrollLayoutParams.removeRule(RelativeLayout.CENTER_VERTICAL);
            scrollButtons.get(1).setLayoutParams(prevScrollLayoutParams);
        } else {
            // 안보이게 하기, 배경 검정
            vgUploadInfoBottomSheet.setVisibility(View.GONE);
            if (vgRestrictWarning.getVisibility() == View.VISIBLE) {
                vgRestrictWarning.setVisibility(View.INVISIBLE);
            }
            if (thumbNailViewVisible) {
                lvthumb.setVisibility(View.GONE);
            }
            vpFilePreview.setBackgroundColor(Color.BLACK);
            if (actionBar != null) {
                actionBar.hide();
            }

            RelativeLayout.LayoutParams nextScrollLayoutParams =
                    (RelativeLayout.LayoutParams) scrollButtons.get(0).getLayoutParams();
            nextScrollLayoutParams.setMargins(0, 0, 0, 0);
            nextScrollLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            scrollButtons.get(0).setLayoutParams(nextScrollLayoutParams);
            RelativeLayout.LayoutParams prevScrollLayoutParams =
                    (RelativeLayout.LayoutParams) scrollButtons.get(1).getLayoutParams();
            prevScrollLayoutParams.setMargins(0, 0, 0, 0);
            prevScrollLayoutParams.addRule(RelativeLayout.CENTER_VERTICAL);
            scrollButtons.get(1).setLayoutParams(prevScrollLayoutParams);

            inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
        }
    }

    @Override
    protected void onDestroy() {
        if (subscribe != null && !subscribe.isUnsubscribed()) {
            subscribe.unsubscribe();
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.file_insert_comment_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.action_confirm) {
            onSendFile();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void onSendFile() {
        fileUploadPresenter.onMultiFileUpload(mentionControlViewModel);
    }

    private void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar_file_upload);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(
                    new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        }
    }

    private void setupActionbarTitle(int current, int max) {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            String title = !singleUpload
                    ? String.format("%s (%d/%d)", getString(R.string.title_file_upload), current, max)
                    : getString(R.string.title_file_upload);
            actionBar.setTitle(title);
        }
    }

    @Override
    public void initViewPager(List<String> realFilePathList) {
        FileUploadPagerAdapter fileUploadPagerAdapter = new FileUploadPagerAdapter(getSupportFragmentManager(),
                realFilePathList);

        vpFilePreview.setAdapter(fileUploadPagerAdapter);

        vpFilePreview.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                fileUploadPresenter.onPagerSelect(position);
                setVisibleScrollButton(position);
                setupActionbarTitle(vpFilePreview.getCurrentItem() + 1, vpFilePreview.getAdapter().getCount());
                if (adapter != null) {
                    int itemCount = adapter.getItemCount();

                    for (int idx = 0; idx < itemCount; idx++) {
                        adapter.getItem(idx).setSelected(position == idx);
                    }
                    adapter.notifyDataSetChanged();
                    lvthumb.getLayoutManager().scrollToPosition(position);
                }
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                for (ImageView scrollButton : scrollButtons) {
                    if (scrollButton.getAnimation() != null) {
                        scrollButton.getAnimation().reset();
                    }
                }
                scrollButtonPublishSubject.onNext(new Object());
            }
        });

        fileUploadPresenter.onPagerSelect(0);
        setVisibleScrollButton(0);
        setupActionbarTitle(vpFilePreview.getCurrentItem() + 1, vpFilePreview.getAdapter().getCount());
    }

    public void showRenameTitleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        RelativeLayout vgInputEditText = (RelativeLayout) LayoutInflater
                .from(this).inflate(R.layout.dialog_fragment_input_text, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_dialog_input_text);
        ((TextView) vgInputEditText.findViewById(R.id.tv_popup_title)).setText(R.string.common_fileupload_rename_description);
        String filename = fileUploadPresenter.getFileName(vpFilePreview.getCurrentItem());
        String extension = getFileExtension(filename);
        String filenameWithoutExtension = filename.replaceAll(extension, "");
        input.setHint(getString(R.string.jandi_name));
        input.setText(filenameWithoutExtension);
        input.setSelection(filenameWithoutExtension.length());

        builder.setView(vgInputEditText)
                .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                    String renamedFileName = input.getText().toString() + extension;
                    fileUploadPresenter.changeFileName(
                            vpFilePreview.getCurrentItem(), renamedFileName);
                    setFileName(renamedFileName);
                    tvFileTitle.requestFocus();
                    Completable.fromAction(() -> {
                    }).delay(200, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                            });

                })
                .setNegativeButton(getString(R.string.jandi_cancel), (dialog, which) -> {
                    Completable.fromAction(() -> {
                    }).delay(200, TimeUnit.MILLISECONDS)
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(() -> {
                                inputMethodManager.hideSoftInputFromWindow(etComment.getWindowToken(), 0);
                            });
                });

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        input.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String name = s.toString();
                if (name.trim().length() <= 0
                        || TextUtils.equals(filenameWithoutExtension, s)
                        || name.contains("\\")
                        || name.contains("/")
                        || name.contains(":")
                        || name.contains("*")
                        || name.contains("?")
                        || name.contains("\"")
                        || name.contains("<")
                        || name.contains(">")
                        || name.contains("|")) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }

    private String getFileExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i);
        }
        return extension;
    }

    private void setVisibleScrollButton(int position) {
        if (position == 0) {
            scrollButtons.get(0).setVisibility(View.GONE);
        } else {
            scrollButtons.get(0).setVisibility(View.VISIBLE);
        }

        if (position == vpFilePreview.getAdapter().getCount() - 1) {
            scrollButtons.get(1).setVisibility(View.GONE);
        } else {
            scrollButtons.get(1).setVisibility(View.VISIBLE);
        }
    }

    @OnTextChanged(value = R.id.et_file_upload_comment, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void onCommentTextChange(CharSequence text) {
        int currentItemPosition = vpFilePreview.getCurrentItem();
        fileUploadPresenter.onCommentTextChange(text.toString(), currentItemPosition);
    }

    @OnClick(R.id.iv_file_upload_preview_previous)
    void onPreviousClick() {
        int currentItem = vpFilePreview.getCurrentItem();

        if (currentItem - 1 >= 0) {
            vpFilePreview.setCurrentItem(currentItem - 1, true);
        }
    }

    @OnClick(R.id.iv_file_upload_preview_next)
    void onNextClick() {
        int currentItem = vpFilePreview.getCurrentItem();
        int count = vpFilePreview.getAdapter().getCount();

        if (currentItem + 1 < count) {
            vpFilePreview.setCurrentItem(currentItem + 1, true);
        }
    }

    @OnClick(R.id.vg_file_upload_preview_content_title)
    void onClickFileRename() {
        showRenameTitleDialog();
    }

    @Override
    public void setFileName(String fileName) {
        String extension = getFileExtension(fileName);
        int lastIndexOf = fileName.lastIndexOf(extension);
        final SpannableStringBuilder filenameSp = new SpannableStringBuilder(fileName);

        filenameSp.setSpan(new ForegroundColorSpan(0xff333333),
                lastIndexOf, lastIndexOf + extension.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvFileTitle.setText(filenameSp);
    }

    @Override
    public void setComment(String comment) {
        if (mentionControlViewModel != null && !TextUtils.isEmpty(comment)) {
            mentionControlViewModel.setUpMention(comment);
        } else {
            etComment.setText(comment);
            etComment.setSelection(etComment.getText().length());
        }
    }

    @Override
    public void setEntityInfo(String entity) {
        tvEntity.setText(entity);
    }

    @Override
    public void exitOnOK() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void exitOnFail() {
        finish();
    }

    @Override
    public void setShareEntity(long entityId, boolean isUser) {
        if (!isUser) {
            mentionControlViewModel = MentionControlViewModel.newInstance(FileUploadPreviewActivity.this,
                    etComment,
                    Arrays.asList(entityId),
                    MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);
        }
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
    }

    public void setPricingLimitView() {
        long fileSize = TeamInfoLoader.getInstance().getTeamUsage().getFileSize();
        boolean isExceedThreshold = fileSize > 1024 * 1024 * 1024 * 4.5;
        boolean isFree = TeamInfoLoader.getInstance().getTeamPlan().getPricing().equals("free");
        boolean isNotShowWithin3Days = JandiPreference.isExceedPopupWithin3Days();
        if (isExceedThreshold && isFree && !isNotShowWithin3Days) {
            vgRestrictWarning.setVisibility(View.VISIBLE);
            PricingPlanWarningViewController pricingPlanWarningViewController
                    = PricingPlanWarningViewController.with(this, vRestrictWarning);
            pricingPlanWarningViewController.bind();
            pricingPlanWarningViewController.setOnClickRemoveViewListener(() -> {
                vgRestrictWarning.setVisibility(View.GONE);
            });
        } else {
            vgRestrictWarning.setVisibility(View.GONE);
        }
    }

    @Override
    public void setFileThumbInfo(List<FileUploadThumbAdapter.FileThumbInfo> files) {
        adapter.setFileThumbInfo(files);
        adapter.notifyDataSetChanged();
    }

}