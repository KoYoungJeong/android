package com.tosslab.jandi.app.ui.share.multi.interaction;


public interface FileShareInteractor {

    void onClickContent();
    void onFocusContent(boolean focus);

    interface Content {
        void onFocusContent(boolean focus);
    }

    interface Wrapper {
        void toggleContent();
    }
}
