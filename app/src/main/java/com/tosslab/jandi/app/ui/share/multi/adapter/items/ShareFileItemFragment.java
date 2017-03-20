package com.tosslab.jandi.app.ui.share.multi.adapter.items;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.share.multi.interaction.FileShareInteractor;
import com.tosslab.jandi.app.utils.UriUtil;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class ShareFileItemFragment extends Fragment implements FileShareInteractor.Content {
    public static final String EXTRA_FILE_PATH = "filePath";
    @Bind(R.id.iv_item_share_file_image)
    ImageView ivImageThumb;
    @Bind(R.id.vg_item_share_file_icon)
    View vgFileType;
    @Bind(R.id.iv_item_share_file_icon)
    ImageView ivFileType;
    @Bind(R.id.tv_item_share_file_type)
    TextView tvFileType;
    @InjectExtra
    String filePath;
    @InjectExtra
    boolean needTopMargin;

    private FileShareInteractor fileInterator;

    public static ShareFileItemFragment create(String filePath, boolean needTopMargin) {
        ShareFileItemFragment shareFileItemFragment = new ShareFileItemFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_FILE_PATH, filePath);
        bundle.putBoolean("needTopMargin", needTopMargin);
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
        if (fileInterator != null) {
            fileInterator.onClickContent();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle bundle = getArguments();
        Dart.inject(this, bundle);

        if (isImageFile(filePath)) {
            vgFileType.setVisibility(View.GONE);
            ivImageThumb.setVisibility(View.VISIBLE);
            ImageLoader.newInstance()
                    .fragment(this)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .uri(UriUtil.getFileUri(filePath))
                    .into(ivImageThumb);
        } else {
            ivImageThumb.setVisibility(View.GONE);
            tvFileType.setText(FileExtensionsUtil.getFileTypeText(filePath));
            FileExtensionsUtil.Extensions extensions = FileExtensionsUtil.getExtensions(filePath);
            int resId = FileExtensionsUtil.getFileThumbByExt(extensions);
            ImageLoader.loadFromResources(ivFileType, resId);

            vgFileType.setBackgroundColor(FileExtensionsUtil.getFileDetailBackground(extensions));
        }

        onFocusContent(!(getActivity() != null && ((AppCompatActivity) getActivity()).getSupportActionBar().isShowing()));

    }

    private boolean isImageFile(String filePath) {
        return FileExtensionsUtil.getExtensions(filePath) == FileExtensionsUtil.Extensions.IMAGE;
    }

    @Override
    public void onFocusContent(boolean focus) {
        FragmentActivity activity = getActivity();
        if (activity == null) {
            return;
        }
        if (vgFileType.getVisibility() != View.VISIBLE) {
            return;
        }

        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) vgFileType.getLayoutParams();

        if (focus) {
            lp.height = RelativeLayout.LayoutParams.MATCH_PARENT;
            lp.topMargin = 0;
            lp.bottomMargin = 0;
        } else {
            TypedValue typedValue = new TypedValue();
            int actionBarSize = 0;
            DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
            if (activity.getTheme().resolveAttribute(android.R.attr.actionBarSize, typedValue, true)) {
                actionBarSize = TypedValue.complexToDimensionPixelSize(typedValue.data, displayMetrics);
            }
            lp.topMargin = actionBarSize;
            lp.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 230f, displayMetrics);
        }

        vgFileType.setLayoutParams(lp);
    }

    public void setFileInterator(FileShareInteractor fileInterator) {
        this.fileInterator = fileInterator;
    }
}
