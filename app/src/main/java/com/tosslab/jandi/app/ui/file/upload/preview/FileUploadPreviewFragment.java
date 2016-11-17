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
                    .uri(uri)
                    .into(ivFileImage);
        } else {
            ivFileImage.setVisibility(View.GONE);
            vgFileExtensions.setVisibility(View.VISIBLE);
            ivFileExtensions.setScaleType(ImageView.ScaleType.FIT_CENTER);
            ivFileExtensions.setImageResource(getFileTypeResource(extensions));
            vgFileExtensions.setBackgroundColor(getFileDetailBackground(extensions));
            tvFileExtensions.setText(extensions.name().toUpperCase());
        }
    }

    @Click(R.id.iv_file_upload_preview)
    void onImageClick() {
        EventBus.getDefault().post(new FileUploadPreviewImageClickEvent());
    }

    int getFileTypeResource(FileExtensionsUtil.Extensions extensions) {
        int resource = R.drawable.file_detail_etc;
        switch (extensions) {
            case TXT:
                resource = R.drawable.file_detail_text;
                break;
            case AUDIO:
                resource = R.drawable.file_detail_audio;
                break;
            case VIDEO:
                resource = R.drawable.file_detail_video;
                break;
            case EXEL:
                resource = R.drawable.file_detail_excel;
                break;
            case PPT:
                resource = R.drawable.file_detail_ppt;
                break;
            case PDF:
                resource = R.drawable.file_detail_pdf;
                break;
            case IMAGE:
                resource = R.drawable.file_detail_img;
                break;
            case HWP:
                resource = R.drawable.file_detail_hwp;
                break;
            case ZIP:
                resource = R.drawable.file_detail_zip;
                break;
            case ETC:
                resource = R.drawable.file_detail_etc;
                break;
        }

        return resource;
    }

    private int getFileDetailBackground(FileExtensionsUtil.Extensions extensions) {
        int color = 0xffa7a7a7;
        switch (extensions) {
            case TXT:
                color = 0xff426bb7;
                break;
            case AUDIO:
                color = 0xffff992c;
                break;
            case VIDEO:
                color = 0xff8267c1;
                break;
            case EXEL:
                color = 0xff109d57;
                break;
            case PPT:
                color = 0xffed6e3c;
                break;
            case PDF:
                color = 0xffef5050;
                break;
            case IMAGE:
                color = 0xffe88064;
                break;
            case HWP:
                color = 0xff07adad;
                break;
            case ZIP:
                color = 0xff828282;
                break;
            case ETC:
                color = 0xffa7a7a7;
                break;
        }

        return color;
    }
}
