package com.tosslab.jandi.app.ui.file.upload.preview;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.LayoutInflater;
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

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadPreviewImageClickEvent;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadPagerAdapter;
import com.tosslab.jandi.app.ui.file.upload.preview.presenter.FileUploadPresenter;
import com.tosslab.jandi.app.ui.file.upload.preview.presenter.FileUploadPresenterImpl;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.Fullscreen;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.SystemService;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;

import java.util.ArrayList;
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
@Fullscreen
public class FileUploadPreviewActivity extends AppCompatActivity implements FileUploadPresenter.View {

    public static final int REQUEST_CODE = 17863;
    @Extra
    int selectedEntityIdToBeShared;    // Share 할 chat-room

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
    private PublishSubject<Object> scrollButtonPublishSubject;
    private Subscription subscribe;


    @AfterViews
    void initView() {
        setupActionbar();

        fileUploadPresenter.setView(this);
        fileUploadPresenter.onInitEntity(selectedEntityIdToBeShared);
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

    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        EventBus.getDefault().unregister(this);
        super.onPause();
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
        fileUploadPresenter.onUploadStartFile();
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
            actionBar.setTitle(String.format("%s (%d/%d)", getString(R.string.title_file_upload), current, max));
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

    @Click(R.id.tv_file_upload_entity)
    void onEntityTextClick() {
        fileUploadPresenter.onEntitySelect(vpFilePreview.getCurrentItem());
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
        etComment.setText(comment);
        etComment.setSelection(etComment.getText().length());
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

        Dialog dialog = new AlertDialog.Builder(FileUploadPreviewActivity.this)
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
    public void setShareEntity(int entityId) {
        this.selectedEntityIdToBeShared = entityId;
    }

}
