package com.tosslab.jandi.app.ui.share.type.image;

import android.app.Fragment;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;

/**
 * Created by Steve SeongUg Jung on 15. 2. 13..
 */
@EFragment(R.layout.fragment_share_image)
public class ImageShareFragment extends Fragment {


    @FragmentArg
    String uriString;

}
