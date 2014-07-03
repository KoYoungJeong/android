package com.tosslab.toss.app;



import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_right_menu)
public class RightMenuFragment extends BaseFragment {
    @Override
    public String getTitleForThisFragment() {
        return getActivity().getString(R.string.app_name);
    }
}
