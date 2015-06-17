package com.tosslab.jandi.app.ui.file.upload;

import android.support.v4.app.Fragment;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadPreviewImageClickEvent;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

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

    @Click(R.id.iv_file_upload_preview)
    void onImageClick() {
        EventBus.getDefault().post(new FileUploadPreviewImageClickEvent());
    }
}
