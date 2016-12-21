package com.tosslab.jandi.app.ui.profile.defaultimage.model;

import android.graphics.Color;

import com.tosslab.jandi.app.network.client.profile.ProfileApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;


public class ProfileImageSelectorModel {
    private Lazy<ProfileApi> profileApi;
    private ResAvatarsInfo resAvatarsInfo = null;

    @Inject
    public ProfileImageSelectorModel(Lazy<ProfileApi> profileApi) {
        this.profileApi = profileApi;
    }

    public List<String> getCharactersInfo() {
        if (resAvatarsInfo == null) {
            getImageInfos();
        }
        List<String> characterUrlList = new ArrayList<>();

        for (String characterUrl : resAvatarsInfo.getCharacters()) {
            characterUrlList.add(characterUrl);
        }
        return characterUrlList;
    }

    public List<Integer> getColors() {
        if (resAvatarsInfo == null) {
            getImageInfos();
        }

        List<Integer> colorList = new ArrayList<>();

        for (String color : resAvatarsInfo.getBackgroundColors()) {
            colorList.add(Color.parseColor(color));
        }

        return colorList;
    }

    private void getImageInfos() {
        try {
            resAvatarsInfo = profileApi.get().getAvartarsInfo();
        } catch (RetrofitException e) {
        }
    }
}
