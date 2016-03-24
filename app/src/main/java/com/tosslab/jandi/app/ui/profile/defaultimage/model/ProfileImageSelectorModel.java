package com.tosslab.jandi.app.ui.profile.defaultimage.model;

import android.graphics.Color;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResAvatarsInfo;

import org.androidannotations.annotations.EBean;

import java.util.ArrayList;
import java.util.List;



/**
 * Created by tee on 16. 1. 6..
 */

@EBean
public class ProfileImageSelectorModel {
    private ResAvatarsInfo resAvatarsInfo = null;

    public void getImageInfos() {
        try {
            resAvatarsInfo = RequestApiManager.getInstance().getAvartarsInfo();
        } catch (RetrofitError e) {
            e.printStackTrace();
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
