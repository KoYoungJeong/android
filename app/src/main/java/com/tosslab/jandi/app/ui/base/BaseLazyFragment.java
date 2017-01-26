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
    private boolean isLoaded = false;

    private boolean isViewCreated = false;
    private Bundle savedInstanceState = null;

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.savedInstanceState = savedInstanceState;
        this.isViewCreated = true;
        if (getUserVisibleHint() && !isLoaded()) {
            lazyLoadOnActivityCreated(savedInstanceState);
            lazyLoadOnViewCreated(savedInstanceState);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isLoaded) {
            lazyLoadOnActivityCreated(savedInstanceState);
            lazyLoadOnViewCreated(savedInstanceState);
        }
    }

    public boolean isLoaded() {
        return isLoaded;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (getUserVisibleHint()) {
            isVisible = true;
            if (isViewCreated && !isLoaded) {
                lazyLoadOnActivityCreated(savedInstanceState);
                lazyLoadOnViewCreated(savedInstanceState);
            }
        } else {
            isVisible = false;
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    protected void lazyLoadOnViewCreated(Bundle savedInstanceState) {
    }

    protected void lazyLoadOnActivityCreated(Bundle savedInstanceState) {
        isLoaded = true;
    }

}