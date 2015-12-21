package com.tosslab.jandi.app.ui.file.upload.preview;

import android.net.Uri;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadPreviewImageClickEvent;
import com.tosslab.jandi.app.utils.ApplicationUtil;
import com.tosslab.jandi.app.utils.UriFactory;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;

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
public class FileUploadPreviewFragment extends Fragment {

    @FragmentArg
    String realFilePath;

    @ViewById(R.id.iv_file_upload_preview)
    SimpleDraweeView ivFileImage;

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
            GenericDraweeHierarchy hierarchy = ivFileImage.getHierarchy();
            hierarchy.setPlaceholderImage(R.drawable.file_icon_img_198);
            ivFileImage.setHierarchy(hierarchy);

            Uri uri = UriFactory.getFileUri(realFilePath);

            int width = ApplicationUtil.getDisplaySize(false);
            int height = ApplicationUtil.getDisplaySize(true);

            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setAutoRotateEnabled(true)
                    .setResizeOptions(new ResizeOptions(width, height))
                    .build();
            DraweeController controller = Fresco.newDraweeControllerBuilder()
                    .setImageRequest(imageRequest)
                    .setOldController(ivFileImage.getController())
                    .build();
            ivFileImage.setController(controller);
        } else {
            ivFileImage.setVisibility(View.GONE);
            vgFileExtensions.setVisibility(View.VISIBLE);
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
