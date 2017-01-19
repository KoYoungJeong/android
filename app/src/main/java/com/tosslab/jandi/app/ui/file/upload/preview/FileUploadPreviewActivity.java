package com.tosslab.jandi.app.ui.file.upload.preview;

import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.views.PricingPlanWarningViewController;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

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
    @Bind(R.id.vg_file_upload_preview_content)
    ViewGroup vgFileInfo;
    @Bind({R.id.iv_file_upload_preview_previous, R.id.iv_file_upload_preview_next})
    List<ImageView> scrollButtons;
    @Bind(R.id.layout_pricing_plan_warning)
    ViewGroup layoutPricingPlanWarning;

    @Bind(R.id.lv_file_upload_thumbs)
    RecyclerView lvthumb;

    private MentionControlViewModel mentionControlViewModel;
    private PublishSubject<Object> scrollButtonPublishSubject;
    private Subscription subscribe;
    private FileUploadThumbAdapter adapter;

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

        fileUploadPresenter.onInitPricingInfo();

        if (realFilePathList.size() > 1) {

            lvthumb.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            adapter = new FileUploadThumbAdapter();
            adapter.setItemClickListener((view, adapter1, position) -> {
                vpFilePreview.setCurrentItem(position);
            });
            lvthumb.setAdapter(adapter);
            lvthumb.addItemDecoration(new FileUploadThumbDivideItemDecorator());

            fileUploadPresenter.initThumbInfo(realFilePathList);

        } else {
            lvthumb.setVisibility(View.GONE);
        }

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
        if (vgFileInfo.getVisibility() != View.VISIBLE) {
            // 보이도록 하기, 배경 흰색
            vgFileInfo.setVisibility(View.VISIBLE);
            lvthumb.setVisibility(View.VISIBLE);
            vpFilePreview.setBackgroundColor(Color.WHITE);
            if (actionBar != null) {
                actionBar.show();
            }
        } else {
            // 안보이게 하기, 배경 검정
            vgFileInfo.setVisibility(View.GONE);
            lvthumb.setVisibility(View.GONE);
            vpFilePreview.setBackgroundColor(Color.BLACK);
            if (actionBar != null) {
                actionBar.hide();
            }

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
                })
                .setNegativeButton(getString(R.string.jandi_cancel), null);

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

    @OnClick(R.id.tv_file_rename_button)
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

    @Override
    public void setPricingLimitView(Boolean isLimited) {
        if (isLimited) {
            layoutPricingPlanWarning.setVisibility(View.VISIBLE);
            PricingPlanWarningViewController pricingPlanWarningViewController
                    = PricingPlanWarningViewController.with(this, layoutPricingPlanWarning);
            if (from == FROM_SELECT_IMAGE) {
                pricingPlanWarningViewController.bind(PricingPlanWarningViewController.TYPE_UPLOAD_FROM_SELECT_IMAGE);
            } else if (from == FROM_TAKE_PHOTO) {
                pricingPlanWarningViewController.bind(PricingPlanWarningViewController.TYPE_UPLOAD_FROM_TAKE_PHOTO);
            } else if (from == FROM_SELECT_FILE) {
                pricingPlanWarningViewController.bind(PricingPlanWarningViewController.TYPE_UPLOAD_FROM_SELECT_FILE);
            }
        } else {
            layoutPricingPlanWarning.setVisibility(View.GONE);
        }
    }

    @Override
    public void setFileThumbInfo(List<FileUploadThumbAdapter.FileThumbInfo> files) {
        adapter.setFileThumbInfo(files);
        adapter.notifyDataSetChanged();
    }

}
