package com.tosslab.jandi.app.ui.file.upload.preview;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileUploadPreviewImageClickEvent;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;

public class FileUploadPreviewFragment extends Fragment {

    @InjectExtra
    String realFilePath;

    @Bind(R.id.iv_file_upload_preview)
    ImageView ivFileImage;

    @Bind(R.id.vg_file_extensions)
    ViewGroup vgFileExtensions;

    @Bind(R.id.iv_file_extensions)
    ImageView ivFileExtensions;

    @Bind(R.id.tv_file_extenstions)
    TextView tvFileExtensions;

    public static FileUploadPreviewFragment create(String realFilePath) {
        FileUploadPreviewFragment frag = new FileUploadPreviewFragment();
        Bundle args = new Bundle();
        args.putString("realFilePath", realFilePath);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_file_upload_insert_comment, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Dart.inject(this, getArguments());
        initView();
    }

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
            ivFileExtensions.setImageResource(FileExtensionsUtil.getFileThumbByExt(extensions));
            vgFileExtensions.setBackgroundColor(FileExtensionsUtil.getFileDetailBackground(extensions));
            tvFileExtensions.setText(extensions.name().toUpperCase());
        }
    }

    @OnClick(R.id.iv_file_upload_preview)
    void onImageClick() {
        EventBus.getDefault().post(new FileUploadPreviewImageClickEvent());
    }

}
