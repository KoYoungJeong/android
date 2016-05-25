package com.tosslab.jandi.app.ui.intro.signin.presenter;

import com.tosslab.jandi.app.ui.intro.signin.model.MainSignInModel;
import com.tosslab.jandi.app.utils.FormatConverter;

import javax.inject.Inject;

/**
 * Created by tee on 16. 5. 25..
 */
public class MainSignInPresenterImpl implements MainSignInPresenter {

    private MainSignInModel model;

    private MainSignInPresenter.View view;

    @Inject
    public MainSignInPresenterImpl(MainSignInPresenter.View view,
                                   MainSignInModel model) {
        this.view = view;
        this.model = model;
    }

    private void checkValidEmail(String email) {
        if (!FormatConverter.isInvalidEmailString(email)) {

        } else {

        }
    }

}