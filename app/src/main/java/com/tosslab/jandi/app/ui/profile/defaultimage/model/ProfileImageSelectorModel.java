package com.tosslab.jandi.app.ui.profile.defaultimage.model;

import android.graphics.Color;

import com.tosslab.jandi.app.network.client.profile.ProfileApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;


/**
 * Created by tee on 16. 1. 6..
 */

@EBean
public class ProfileImageSelectorModel {
    @Inject
    Lazy<ProfileApi> profileApi;
    private ResAvatarsInfo resAvatarsInfo = null;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public void getImageInfos() {
        try {
            resAvatarsInfo = profileApi.get().getAvartarsInfo();
        } catch (RetrofitException e) {
        }
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
}
