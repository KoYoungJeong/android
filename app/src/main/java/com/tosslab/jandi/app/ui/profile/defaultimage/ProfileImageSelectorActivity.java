package com.tosslab.jandi.app.ui.profile.defaultimage;

import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.defaultimage.adapter.CharacterSelectorAdapter;
import com.tosslab.jandi.app.ui.profile.defaultimage.adapter.ColorSelectorAdapter;
import com.tosslab.jandi.app.ui.profile.defaultimage.presenter.ProfileImageSelectorPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.progresswheel.ProgressWheelUtil;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

/**
 * Created by tee on 16. 1. 6..
 */

@EActivity(R.layout.activity_profile_image_selector)
public class ProfileImageSelectorActivity extends BaseAppCompatActivity implements ProfileImageSelectorPresenter.View, OnRecyclerItemClickListener {

    public static final int CHARACTER_SELECTOR_MODE = 0x00;
    public static final int COLOR_SELECTOR_MODE = 0x01;

    public int mode = CHARACTER_SELECTOR_MODE;

    @ViewById(R.id.vg_option_character)
    LinearLayout vgOptionCharacter;

    @ViewById(R.id.vg_option_color)
    LinearLayout vgOptionColor;

    @ViewById(R.id.lv_profile_item_selector)
    RecyclerView lvProfileItemSelector;

    @ViewById(R.id.iv_main_image)
    SimpleDraweeView lvMainImage;

    @Bean
    ProfileImageSelectorPresenter profileImageSelectorPresenter;

    @Extra("profile_image_file_uri")
    Uri imageUri;

    private CharacterSelectorAdapter<String> profileCharacterSelectorAdapter;
    private ColorSelectorAdapter<Integer> profileColorSelectorAdapter;
    private int selectedColor = -1;
    private String selectedCharacterUrl = null;
    private ProgressWheelUtil progressWheelUtil;

    @AfterViews
    void initViews() {
        if (!NetworkCheckUtil.isConnected()) {
            ColoredToast.show(getResources().getString(R.string.err_network));
            finish();
        } else {
            profileImageSelectorPresenter.setView(this);
            profileCharacterSelectorAdapter = new CharacterSelectorAdapter<>();
            profileColorSelectorAdapter = new ColorSelectorAdapter<>();
            profileCharacterSelectorAdapter.setOnRecyclerItemClickListener(this);
            profileColorSelectorAdapter.setOnRecyclerItemClickListener(this);
            LinearLayoutManager layoutManager = new LinearLayoutManager(this);
            layoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
            lvProfileItemSelector.setLayoutManager(layoutManager);
            lvProfileItemSelector.setAdapter(profileCharacterSelectorAdapter);

            profileImageSelectorPresenter.initLists();

            onClickOptionCharacterButton();
            progressWheelUtil = ProgressWheelUtil.makeInstance();
        }
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showInitialImage() {
        selectedCharacterUrl = profileCharacterSelectorAdapter.getSelectedItem();
        selectedColor = profileColorSelectorAdapter.getSelectedItem();
        showMainProfileImage();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showCharacterList(List<String> characterUrls) {
        profileCharacterSelectorAdapter.setItems(characterUrls);
        profileCharacterSelectorAdapter.notifyDataSetChanged();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showColorList(List<Integer> colorRGBs) {
        profileColorSelectorAdapter.setItems(colorRGBs);
        profileColorSelectorAdapter.notifyDataSetChanged();
    }

    @Click(R.id.btn_cancel)
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void onClickCancelButton() {
        LogUtil.e("click cancel button");
        profileImageSelectorPresenter.removeFile(imageUri.getPath());
        finish();
    }

    @Click(R.id.btn_ok)
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void onClickOkButton() {
        LogUtil.e("click ok button");
        if (selectedCharacterUrl != null && selectedColor != -1) {
            LogUtil.e("ImagePath", imageUri.getPath());
            profileImageSelectorPresenter.makeCustomProfileImageFile(imageUri,
                    selectedCharacterUrl, selectedColor);
            setResult(RESULT_OK);
            finish();
        }
    }

    @Click(R.id.vg_option_color)
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void onClickOptionColorButton() {
        LogUtil.e("click option color button");
        mode = COLOR_SELECTOR_MODE;
        changeMode(mode);
    }

    @Click(R.id.vg_option_character)
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void onClickOptionCharacterButton() {
        LogUtil.e("click option character button");
        mode = CHARACTER_SELECTOR_MODE;
        changeMode(mode);
    }

    public int getMode() {
        return mode;
    }

    public void changeMode(int mode) {
        switch (mode) {
            case CHARACTER_SELECTOR_MODE:
                vgOptionCharacter.setSelected(true);
                vgOptionColor.setSelected(false);
                lvProfileItemSelector.setAdapter(profileCharacterSelectorAdapter);
                break;
            case COLOR_SELECTOR_MODE:
                vgOptionCharacter.setSelected(false);
                vgOptionColor.setSelected(true);
                lvProfileItemSelector.setAdapter(profileColorSelectorAdapter);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void showMainProfileImage() {
        ImageUtil.loadProfileImage(lvMainImage,
                Uri.parse(selectedCharacterUrl), 0, selectedColor);
    }

    @Override
    public void onBackPressed() {
        profileImageSelectorPresenter.removeFile(imageUri.getPath());
        super.onBackPressed();
    }

    @Override
    public void showProgress() {
        progressWheelUtil.showProgressWheel(this);
    }

    @Override
    public void finishProgress() {
        progressWheelUtil.dismissProgressWheel(this);
    }

    @Override
    public void onItemClick(View view, RecyclerView.Adapter adapter, int position) {
        if (adapter instanceof CharacterSelectorAdapter) {
            selectedCharacterUrl = ((CharacterSelectorAdapter<String>) adapter).getSelectedItem();
        } else if (adapter instanceof ColorSelectorAdapter) {
            selectedColor = ((ColorSelectorAdapter<Integer>) adapter).getSelectedItem();
        }
        showMainProfileImage();
    }

}
