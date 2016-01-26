package com.tosslab.jandi.app.ui.file.upload.preview;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadPreviewImageClickEvent;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadPagerAdapter;
import com.tosslab.jandi.app.ui.file.upload.preview.presenter.FileUploadPresenter;
import com.tosslab.jandi.app.ui.file.upload.preview.presenter.FileUploadPresenterImpl;
import com.tosslab.jandi.app.ui.file.upload.preview.to.FileUploadVO;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.activity.ActivityHelper;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.subjects.PublishSubject;

/**
 * Created by Bill MinWook Heo on 15. 6. 15..
 */
@EActivity(R.layout.activity_file_upload_insert_commnet)
@OptionsMenu(R.menu.file_insert_comment_menu)
public class FileUploadPreviewActivity extends BaseAppCompatActivity implements FileUploadPresenter.View {

    public static final int REQUEST_CODE = 17863;
    public static final String KEY_SINGLE_FILE_UPLOADVO = "file_uploadvo";
    @Extra
    int selectedEntityIdToBeShared;    // Share 할 chat-room

    @Extra
    boolean singleUpload = false;

    @Extra
    ArrayList<String> realFilePathList;

    @Bean(FileUploadPresenterImpl.class)
    FileUploadPresenter fileUploadPresenter;

    @SystemService
    InputMethodManager inputMethodManager;

    @ViewById(R.id.vp_file_upload_preview)
    ViewPager vpFilePreview;

    @ViewById(R.id.tv_file_upload_title)
    TextView tvFileTitle;

    @ViewById(R.id.tv_file_upload_entity)
    TextView tvEntity;

    @ViewById(R.id.et_file_upload_comment)
    EditText etComment;

    @ViewById(R.id.vg_file_upload_preview_content)
    ViewGroup vgFileInfo;

    @ViewsById({R.id.iv_file_upload_preview_previous, R.id.iv_file_upload_preview_next})
    List<ImageView> scrollButtons;

    private MentionControlViewModel mentionControlViewModel;
    private PublishSubject<Object> scrollButtonPublishSubject;
    private Subscription subscribe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setNeedUnLockPassCode(false);
    }

    @AfterViews
    void initView() {
        setupActionbar();

        fileUploadPresenter.setView(this);
        fileUploadPresenter.onInitEntity(FileUploadPreviewActivity.this, selectedEntityIdToBeShared);
        fileUploadPresenter.onInitViewPager(selectedEntityIdToBeShared, realFilePathList);


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
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        ActivityHelper.setOrientation(this);
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
        mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
    }

    public void onEventMainThread(FileUploadPreviewImageClickEvent event) {

        ActionBar actionBar = getSupportActionBar();
        if (vgFileInfo.getVisibility() != View.VISIBLE) {
            // 보이도록 하기, 배경 흰색
            vgFileInfo.setVisibility(View.VISIBLE);
            vpFilePreview.setBackgroundColor(Color.WHITE);
            if (actionBar != null) {
                actionBar.show();
            }
        } else {
            // 안보이게 하기, 배경 검정
            vgFileInfo.setVisibility(View.GONE);
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

    @OptionsItem(android.R.id.home)
    void onGoBackOptionSelect() {
        finish();
    }

    @OptionsItem(R.id.action_confirm)
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

        vpFilePreview.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                fileUploadPresenter.onPagerSelect(position);

                setVisibleScrollButton(position);
                setupActionbarTitle(vpFilePreview.getCurrentItem() + 1, vpFilePreview.getAdapter().getCount());

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

    @AfterTextChange(R.id.et_file_upload_comment)
    void onCommentTextChange(Editable text) {
        int currentItemPosition = vpFilePreview.getCurrentItem();
        fileUploadPresenter.onCommentTextChange(text.toString(), currentItemPosition);
    }

    @Click(R.id.iv_file_upload_preview_previous)
    void onPreviousClick() {
        int currentItem = vpFilePreview.getCurrentItem();

        if (currentItem - 1 >= 0) {
            vpFilePreview.setCurrentItem(currentItem - 1, true);
        }
    }

    @Click(R.id.iv_file_upload_preview_next)
    void onNextClick() {
        int currentItem = vpFilePreview.getCurrentItem();
        int count = vpFilePreview.getAdapter().getCount();

        if (currentItem + 1 < count) {
            vpFilePreview.setCurrentItem(currentItem + 1, true);
        }
    }

    @Override
    public void setFileName(String fileName) {
        tvFileTitle.setText(fileName);
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
    public void showEntitySelectDialog(List<FormattedEntity> entityList) {
        android.view.View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_invite_to_topic, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        EditText et = (EditText) view.findViewById(R.id.et_cdp_search);

        final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(this, entityList);

        PublishSubject<String> publishSubject = PublishSubject.create();
        Subscription subscribe = publishSubject.throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                .flatMap(s -> {
                    String searchText = s.toLowerCase();

                    return Observable.from(entityList)
                            .filter(formattedEntity -> formattedEntity.getName().toLowerCase()
                                    .contains(searchText))
                            .collect((Func0<ArrayList<FormattedEntity>>) ArrayList::new, ArrayList::add);

                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(adapter::setEntities);

        publishSubject.onNext("");

        et.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                publishSubject.onNext(s.toString());
            }
        });

        Dialog dialog = new AlertDialog.Builder(FileUploadPreviewActivity.this,
                R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setTitle(R.string.jandi_title_cdp_to_be_shared)
                .setView(view)
                .create();
        dialog.show();

        dialog.setOnDismissListener(dialog1 -> subscribe.unsubscribe());

        lv.setAdapter(adapter);

        lv.setOnItemClickListener((adapterView, view1, position, l) -> {

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }

            FormattedEntity item = ((EntitySimpleListAdapter) adapterView.getAdapter()).getItem(position);

            fileUploadPresenter.onEntityUpdate(item);
        });
    }

    @Override
    public void exitOnOK() {
        setResult(RESULT_OK);
        finish();
    }

    @Override
    public void exitOnOk(FileUploadVO fileUploadVO) {
        Intent intent = new Intent();
        intent.putExtra(KEY_SINGLE_FILE_UPLOADVO, fileUploadVO);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void setShareEntity(int entityId, boolean isUser) {
        this.selectedEntityIdToBeShared = entityId;

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
}
