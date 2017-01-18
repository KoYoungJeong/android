package com.tosslab.jandi.app.ui.share.multi.adapter.items;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import java.io.File;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShareFileItemFragment extends Fragment {
    public static final String EXTRA_FILE_PATH = "file_path";
    @Bind(R.id.iv_item_share_file_image)
    ImageView ivImageThumb;
    @Bind(R.id.vg_item_share_file_icon)
    View vgFileType;
    @Bind(R.id.iv_item_share_file_icon)
    ImageView ivFileType;
    @Bind(R.id.tv_item_share_file_type)
    TextView tvFileType;
    private String filePath;

    public static ShareFileItemFragment create(String filePath) {
        ShareFileItemFragment shareFileItemFragment = new ShareFileItemFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_FILE_PATH, filePath);
        shareFileItemFragment.setArguments(bundle);
        return shareFileItemFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_item_share_file, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @OnClick(value = {R.id.iv_item_share_file_image,
            R.id.vg_item_share_file_icon})
    void onContentClick() {
        ((InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
                .hideSoftInputFromWindow(getActivity().getWindow().getDecorView().getWindowToken(), 0);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        if (bundle != null && bundle.containsKey(EXTRA_FILE_PATH)) {
            filePath = bundle.getString(EXTRA_FILE_PATH);
        }

        if (isImageFile(filePath)) {
            vgFileType.setVisibility(View.GONE);
            ivImageThumb.setVisibility(View.VISIBLE);
            Uri uri = FileProvider.getUriForFile(getContext(),
                    getString(R.string.jandi_file_authority), new File(filePath));
            ImageLoader.newInstance()
                    .fragment(this)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .uri(uri)
                    .into(ivImageThumb);
        } else {
            ivImageThumb.setVisibility(View.GONE);
            tvFileType.setText(FileExtensionsUtil.getFileTypeText(filePath));
            int resId = FileExtensionsUtil.getFileTypeBigImageResource(filePath);
            ImageLoader.loadFromResources(ivFileType, resId);
        }

    }

    private boolean isImageFile(String filePath) {
        return FileExtensionsUtil.getExtensions(filePath) == FileExtensionsUtil.Extensions.IMAGE;
    }

}
