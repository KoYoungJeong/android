package com.tosslab.toss.app;


import org.androidannotations.annotations.EFragment;

@EFragment(R.layout.fragment_right_menu)
public class RightMenuFragment extends BaseFragment {
    @Override
    public String getTitleForThisFragment() {
        return getActivity().getString(R.string.app_name);
    }
}
