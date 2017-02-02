package com.tosslab.jandi.app.ui.base;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;

/**
 * Created by tee on 2017. 1. 24..
 */

public abstract class BaseLazyFragment extends Fragment {

    protected boolean isVisible = false;
    private boolean isLoadedAllView = false;
    private boolean isLoadedAllDatas = false;
    private Bundle savedInstanceState = null;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.savedInstanceState = savedInstanceState;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isVisible && !isLoadedAllDatas) {
            onLazyLoad(savedInstanceState);
            isLoadedAllDatas = true;
        }
        isLoadedAllView = true;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            if (isLoadedAllView && !isLoadedAllDatas) {
                onLazyLoad(savedInstanceState);
                isLoadedAllDatas = true;
            }
        } else {
            isVisible = false;
        }
    }

    protected boolean isLoadedAll() {
        return isLoadedAllDatas;
    }

    protected void onLazyLoad(Bundle savedInstanceState) {
    }

}