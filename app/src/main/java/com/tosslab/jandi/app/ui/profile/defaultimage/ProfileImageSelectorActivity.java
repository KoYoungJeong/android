package com.tosslab.jandi.app.ui.profile.defaultimage;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.profile.defaultimage.adapter.CharacterSelectorAdapter;
import com.tosslab.jandi.app.ui.profile.defaultimage.adapter.ColorSelectorAdapter;
import com.tosslab.jandi.app.ui.profile.defaultimage.dagger.DaggerProfileImageSelectorComponent;
import com.tosslab.jandi.app.ui.profile.defaultimage.dagger.ProfileImageSelectorModule;
import com.tosslab.jandi.app.ui.profile.defaultimage.presenter.ProfileImageSelectorPresenter;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;
import com.tosslab.jandi.app.utils.progresswheel.ProgressWheelUtil;
import com.tosslab.jandi.app.views.listeners.OnRecyclerItemClickListener;

import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ProfileImageSelectorActivity extends BaseAppCompatActivity implements ProfileImageSelectorPresenter.View, OnRecyclerItemClickListener {

    public static final int CHARACTER_SELECTOR_MODE = 0x00;
    public static final int COLOR_SELECTOR_MODE = 0x01;

    public int mode = CHARACTER_SELECTOR_MODE;

    @Bind(R.id.vg_option_character)
    LinearLayout vgOptionCharacter;

    @Bind(R.id.vg_option_color)
    LinearLayout vgOptionColor;

    @Bind(R.id.lv_profile_item_selector)
    RecyclerView lvProfileItemSelector;

    @Bind(R.id.iv_main_image)
    ImageView ivMain;

    @Inject
    ProfileImageSelectorPresenter profileImageSelectorPresenter;

    @InjectExtra("profile_image_file_uri")
    Uri imageUri;

    private CharacterSelectorAdapter profileCharacterSelectorAdapter;
    private ColorSelectorAdapter profileColorSelectorAdapter;
    private int selectedColor = -1;
    private String selectedCharacterUrl = null;
    private ProgressWheelUtil progressWheelUtil;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_image_selector);

        Dart.inject(this);
        ButterKnife.bind(this);
        DaggerProfileImageSelectorComponent.builder()
                .profileImageSelectorModule(new ProfileImageSelectorModule(this))
                .build()
                .inject(this);
        initViews();
    }

    void initViews() {
        if (!NetworkCheckUtil.isConnected()) {
            ColoredToast.show(getResources().getString(R.string.err_network));
            finish();
        } else {
            profileCharacterSelectorAdapter = new CharacterSelectorAdapter();
            profileColorSelectorAdapter = new ColorSelectorAdapter();
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
    public void showInitialImage() {
        selectedCharacterUrl = profileCharacterSelectorAdapter.getSelectedItem();
        selectedColor = profileColorSelectorAdapter.getSelectedItem();
        showMainProfileImage();
    }

    @Override
    public void showCharacterList(List<String> characterUrls) {
        profileCharacterSelectorAdapter.setItems(characterUrls);
        profileCharacterSelectorAdapter.notifyDataSetChanged();
    }

    @Override
    public void showColorList(List<Integer> colorRGBs) {
        profileColorSelectorAdapter.setItems(colorRGBs);
        profileColorSelectorAdapter.notifyDataSetChanged();
    }

    @OnClick(R.id.btn_cancel)
    void onClickCancelButton() {
        LogUtil.e("click cancel button");
        profileImageSelectorPresenter.removeFile(imageUri.getPath());
        finish();
    }

    @OnClick(R.id.btn_ok)
    void onClickOkButton() {
        if (selectedCharacterUrl != null && selectedColor != -1) {
            profileImageSelectorPresenter.makeCustomProfileImageFile(imageUri,
                    selectedCharacterUrl, selectedColor);

        }
    }

    @Override
    public void finishWithOK() {
        setResult(RESULT_OK);
        finish();
    }

    @OnClick(R.id.vg_option_color)
    void onClickOptionColorButton() {
        LogUtil.e("click option color button");
        mode = COLOR_SELECTOR_MODE;
        changeMode(mode);
    }

    @OnClick(R.id.vg_option_character)
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
        ImageUtil.loadProfileImage(ivMain, selectedCharacterUrl, 0, selectedColor);
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
            selectedCharacterUrl = ((CharacterSelectorAdapter) adapter).getSelectedItem();
        } else if (adapter instanceof ColorSelectorAdapter) {
            selectedColor = ((ColorSelectorAdapter) adapter).getSelectedItem();
        }
        showMainProfileImage();
    }

}
