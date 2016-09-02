package com.tosslab.jandi.app.ui.file.upload.preview;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadPreviewImageClickEvent;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import de.greenrobot.event.EventBus;

@EFragment(R.layout.fragment_file_upload_insert_comment)
public class FileUploadPreviewFragment extends Fragment {

    @FragmentArg
    String realFilePath;

    @ViewById(R.id.iv_file_upload_preview)
    ImageView ivFileImage;

    @ViewById(R.id.vg_file_extensions)
    ViewGroup vgFileExtensions;

    @ViewById(R.id.iv_file_extensions)
    ImageView ivFileExtensions;

    @ViewById(R.id.tv_file_extenstions)
    TextView tvFileExtensions;

    @AfterViews
    void initView() {
        FileExtensionsUtil.Extensions extensions = FileExtensionsUtil.getExtensions(realFilePath);

        if (extensions == FileExtensionsUtil.Extensions.IMAGE) {
            Uri uri = UriUtil.getFileUri(realFilePath);

            ImageLoader.newInstance()
                    .fragment(this)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .placeHolder(R.drawable.file_icon_img_198, ImageView.ScaleType.CENTER)
                    .uri(uri)
                    .into(ivFileImage);
        } else {
            ivFileImage.setVisibility(View.GONE);
            vgFileExtensions.setVisibility(View.VISIBLE);

            ivFileExtensions.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivFileExtensions.setImageResource(getFileTypeResource(extensions));
            tvFileExtensions.setText(extensions.name().toUpperCase());
        }
    }

    @Click(R.id.iv_file_upload_preview)
    void onImageClick() {
        EventBus.getDefault().post(new FileUploadPreviewImageClickEvent());
    }

    int getFileTypeResource(FileExtensionsUtil.Extensions extensions) {
        int resource = R.drawable.file_icon_etc_198;
        switch (extensions) {
            case TXT:
                resource = R.drawable.file_icon_txt_198;
                break;
            case AUDIO:
                resource = R.drawable.file_icon_audio_198;
                break;
            case VIDEO:
                resource = R.drawable.file_icon_video_198;
                break;
            case EXEL:
                resource = R.drawable.file_icon_exel_198;
                break;
            case PPT:
                resource = R.drawable.file_icon_ppt_198;
                break;
            case PDF:
                resource = R.drawable.file_icon_pdf_198;
                break;
            case IMAGE:
                resource = R.drawable.file_icon_img_198;
                break;
            case HWP:
                resource = R.drawable.file_icon_hwp_198;
                break;
            case ZIP:
                resource = R.drawable.file_icon_zip_198;
                break;
            case ETC:
                resource = R.drawable.file_icon_etc_198;
                break;
        }

        return resource;
    }
}
