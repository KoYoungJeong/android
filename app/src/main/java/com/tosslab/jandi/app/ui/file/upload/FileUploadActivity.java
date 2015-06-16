package com.tosslab.jandi.app.ui.file.upload;

import android.app.Dialog;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.ui.file.upload.adapter.FileUploadPagerAdapter;
import com.tosslab.jandi.app.ui.file.upload.presenter.FileUploadPresenter;
import com.tosslab.jandi.app.ui.file.upload.presenter.FileUploadPresenterImpl;
import com.tosslab.jandi.app.views.listeners.SimpleEndAnimationListener;

import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.ViewsById;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.android.schedulers.AndroidSchedulers;
import rx.subjects.PublishSubject;

/**
 * Created by Bill MinWook Heo on 15. 6. 15..
 */
@EActivity(R.layout.activity_file_upload_insert_commnet)
@OptionsMenu(R.menu.file_insert_comment_menu)
public class FileUploadActivity extends AppCompatActivity implements FileUploadPresenter.View {

    @Extra
    int selectedEntityIdToBeShared;    // Share 할 chat-room

    @Extra
    ArrayList<String> realFilePathList;

    @Bean(FileUploadPresenterImpl.class)
    FileUploadPresenter fileUploadPresenter;

    @ViewById(R.id.vp_file_upload_preview)
    ViewPager vpFilePreview;

    @ViewById(R.id.tv_file_upload_title)
    TextView tvFileTitle;

    @ViewById(R.id.tv_file_upload_entity)
    TextView tvEntity;

    @ViewById(R.id.et_file_upload_comment)
    EditText etComment;

    @ViewsById({R.id.iv_file_upload_preview_previous, R.id.iv_file_upload_preview_next})
    List<ImageView> scrollButtons;
    private PublishSubject<Object> scrollButtonPublishSubject;


    @AfterViews
    void initView() {

        fileUploadPresenter.setView(this);
        fileUploadPresenter.onInitViewPager(selectedEntityIdToBeShared, realFilePathList);
        fileUploadPresenter.onInitEntity(selectedEntityIdToBeShared);

        setupActionbar();

        scrollButtonPublishSubject = PublishSubject.create();
        scrollButtonPublishSubject.throttleWithTimeout(3000, TimeUnit.MILLISECONDS)
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


    @OptionsItem(android.R.id.home)
    void onGoBackOptionSelect() {
        finish();
    }

    @OptionsItem(R.id.action_confirm)
    void onSendFile() {

    }

    private void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.actionbar_file_upload);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle(R.string.title_file_upload);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(
                    new ColorDrawable(getResources().getColor(android.R.color.transparent)));
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


        setVisibleScrollButton(0);
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
    }

    @Override
    public void setEntityInfo(String entity) {

        tvEntity.setText(entity);

    }

    @Override
    public void showEntitySelectDialog(List<FormattedEntity> entityList) {


        android.view.View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_select_cdp, null);
        ListView lv = (ListView) view.findViewById(R.id.lv_cdp_select);
        final EntitySimpleListAdapter adapter = new EntitySimpleListAdapter(getApplicationContext(), entityList);

        Dialog dialog = new AlertDialog.Builder(FileUploadActivity.this)
                .setTitle(R.string.jandi_file_search_user)
                .setView(view)
                .create();
        dialog.show();

        lv.setOnItemClickListener((adapterView, view1, position, l) -> {

            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
            }

            FormattedEntity item = ((EntitySimpleListAdapter) adapterView.getAdapter()).getItem(position);

            fileUploadPresenter.onEntityUpdate(item);
        });
        lv.setAdapter(adapter);
    }

}
