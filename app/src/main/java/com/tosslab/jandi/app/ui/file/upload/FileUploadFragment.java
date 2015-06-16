package com.tosslab.jandi.app.ui.file.upload;

import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Bill MinWook Heo on 15. 6. 15..
 */
@EFragment(R.layout.fragment_file_upload_insert_comment)
public class FileUploadFragment extends Fragment {

    @FragmentArg
    String realFilePath;

    @ViewById(R.id.iv_file_upload_preview)
    ImageView ivFileImage;

    @AfterViews
    void initView() {

        Glide.with(getActivity())
                .load(realFilePath)
                .fitCenter()
                .into(ivFileImage);
    }

}
