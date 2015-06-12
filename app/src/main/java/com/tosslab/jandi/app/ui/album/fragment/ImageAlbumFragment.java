package com.tosslab.jandi.app.ui.album.fragment;

import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;

import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
@EFragment(R.layout.fragment_image_album)
public class ImageAlbumFragment extends Fragment {

    @ViewById(R.id.rv_image_album)
    RecyclerView recyclerView;

    @AfterViews
    void initViews() {

    }
}
