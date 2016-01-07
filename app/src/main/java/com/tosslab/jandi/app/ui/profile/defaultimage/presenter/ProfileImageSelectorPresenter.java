package com.tosslab.jandi.app.ui.profile.defaultimage.presenter;

import com.tosslab.jandi.app.ui.profile.defaultimage.model.ProfileImageSelectorModel;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.util.List;

/**
 * Created by tee on 16. 1. 6..
 */

@EBean
public class ProfileImageSelectorPresenter {

    @Bean
    ProfileImageSelectorModel profileImageSelectorModel;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void initLists() {
        List<String> characterUrls = profileImageSelectorModel.getCharactersInfo();
        view.showCharacterList(characterUrls);
        List<Integer> colorRGBs = profileImageSelectorModel.getColors();
        view.showColorList(colorRGBs);
        view.showInitialImage();
    }

    public interface View {
        void showCharacterList(List<String> characterUrls);

        void showColorList(List<Integer> colorRGBs);

        void showInitialImage();
    }

}
