package com.tosslab.jandi.app.ui.profile.defaultimage;

import android.net.Uri;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.LinearLayout;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.DefaultProfileChangeEvent;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.defaultimage.adapter.ProfileSelectorAdapter;
import com.tosslab.jandi.app.ui.profile.defaultimage.presenter.ProfileImageSelectorPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 1. 6..
 */

@EActivity(R.layout.activity_profile_image_selector)
public class ProfileImageSelectorActivity extends BaseAppCompatActivity implements ProfileImageSelectorPresenter.View {

    public static final int CHARACTER_SELECTOR_MODE = 0x00;
    public static final int COLOR_SELECTOR_MODE = 0x01;

    public int mode = CHARACTER_SELECTOR_MODE;

    @ViewById(R.id.vg_option_character)
    LinearLayout vgOptionCharacter;

    @ViewById(R.id.vg_option_color)
    LinearLayout vgOptionColor;

    @ViewById(R.id.lv_profile_color_selector)
    RecyclerView lvProfileColorSelector;

    @ViewById(R.id.lv_profile_character_selector)
    RecyclerView lvProfileCharacterSelector;

    @ViewById(R.id.iv_main_image)
    SimpleDraweeView lvMainImage;

    @Bean
    ProfileImageSelectorPresenter profileImageSelectorPresenter;

    @Extra("profile_image_file_uri")
    Uri imageUri;

    private ProfileSelectorAdapter profileCharacterSelectorAdapter;
    private ProfileSelectorAdapter profileColorSelectorAdapter;
    private int selectedColor = -1;
    private String selectedCharacterUrl = null;

    @AfterViews
    void initViews() {
        if (!NetworkCheckUtil.isConnected()) {
            ColoredToast.show(this, getResources().getString(R.string.err_network));
            finish();
        } else {
            profileImageSelectorPresenter.setView(this);
            profileCharacterSelectorAdapter = new ProfileSelectorAdapter(ProfileSelectorAdapter.MODE_CHARACTER_LIST);
            profileColorSelectorAdapter = new ProfileSelectorAdapter(ProfileSelectorAdapter.MODE_COLOR_LIST);

            LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
            layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
            lvProfileCharacterSelector.setLayoutManager(layoutManager1);
            lvProfileCharacterSelector.setAdapter(profileCharacterSelectorAdapter);

            LinearLayoutManager layoutManager2 = new LinearLayoutManager(this);
            layoutManager2.setOrientation(LinearLayoutManager.HORIZONTAL);
            lvProfileColorSelector.setLayoutManager(layoutManager2);
            lvProfileColorSelector.setAdapter(profileColorSelectorAdapter);

            profileImageSelectorPresenter.initLists();
            EventBus.getDefault().register(this);

            onClickOptionCharacterButton();
        }
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showInitialImage() {
        selectedCharacterUrl = (String) profileCharacterSelectorAdapter.getSelectedItem();
        selectedColor = (Integer) profileColorSelectorAdapter.getSelectedItem();
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

    @Click(R.id.bt_cancel)
    @UiThread(propagation = UiThread.Propagation.REUSE)
    void onClickCancelButton() {
        LogUtil.e("click cancel button");
        finish();
    }

    @Click(R.id.bt_ok)
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
                lvProfileCharacterSelector.setVisibility(View.VISIBLE);
                lvProfileColorSelector.setVisibility(View.GONE);
                break;
            case COLOR_SELECTOR_MODE:
                vgOptionCharacter.setSelected(false);
                vgOptionColor.setSelected(true);
                lvProfileCharacterSelector.setVisibility(View.GONE);
                lvProfileColorSelector.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    public void onEvent(DefaultProfileChangeEvent event) {
        if (event.getMode() == ProfileSelectorAdapter.MODE_CHARACTER_LIST) {
            selectedCharacterUrl = (String) event.getData();
        } else {
            selectedColor = (Integer) event.getData();
        }
        showMainProfileImage();
    }

    private void showMainProfileImage() {
        ImageUtil.loadProfileImage(lvMainImage,
                Uri.parse(selectedCharacterUrl), 0, selectedColor);
    }

}
