package com.tosslab.jandi.app.ui.album.imagealbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.imagealbum.adapter.DefaultAlbumAdapter;
import com.tosslab.jandi.app.ui.album.imagealbum.adapter.ImagePictureAdapter;
import com.tosslab.jandi.app.ui.album.imagealbum.dagger.DaggerImageAlbumFragComponent;
import com.tosslab.jandi.app.ui.album.imagealbum.dagger.ImageAlbumFragModule;
import com.tosslab.jandi.app.ui.album.imagealbum.presenter.ImageAlbumPresenter;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.imagealbum.vo.ImagePicture;
import com.tosslab.jandi.app.ui.album.imagecrop.ImageCropPickerActivity;
import com.tosslab.jandi.app.ui.profile.modify.view.ModifyProfileActivity;
import com.tosslab.jandi.app.utils.AnimationModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ImageAlbumFragment extends Fragment implements ImageAlbumPresenter.View {

    public static final int GRID_ROW_COLUMN = 3;
    public static final int BUCKET_ALL_IMAGE_ALBUM = -2;
    public static final int BUCKET_ALBUM_LIST = -1;
    @Bind(R.id.rv_image_album)
    RecyclerView recyclerView;

    @Inject
    AnimationModel animationModel;

    @Inject
    ImageAlbumPresenter imageAlbumPresenter;

    @Nullable
    @InjectExtra
    int buckerId = BUCKET_ALBUM_LIST;

    @Nullable
    @InjectExtra
    int mode;

    private ProgressWheel progressWheel;
    private ImagePictureAdapter imagePictureAdapter;

    public static ImageAlbumFragment create(int buckerId, int mode) {
        ImageAlbumFragment fragment = new ImageAlbumFragment();
        Bundle args = new Bundle();
        args.putInt("buckerId", buckerId);
        args.putInt("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    public static ImageAlbumFragment create(int mode) {
        ImageAlbumFragment fragment = new ImageAlbumFragment();
        Bundle args = new Bundle();
        args.putInt("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_album, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            Dart.inject(this, arguments);
        }
        DaggerImageAlbumFragComponent.builder()
                .imageAlbumFragModule(new ImageAlbumFragModule(this))
                .build()
                .inject(this);

        initViews();
    }

    void initViews() {
        progressWheel = new ProgressWheel(getActivity());

        imageAlbumPresenter.onLoadImageAlbum(buckerId);
        imageAlbumPresenter.onSetupActionbar(buckerId);
    }

    @Override
    public void showProgress() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void hideProgress() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    @Override
    public void showDefaultAlbumList(List<ImageAlbum> defaultAlbumList) {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration());
        recyclerView.setItemAnimator(null);
        DefaultAlbumAdapter adapter = new DefaultAlbumAdapter(getActivity(), defaultAlbumList);

        adapter.setOnRecyclerItemClickListener((view, adapter1, position) -> {

            ImageAlbum item = ((DefaultAlbumAdapter) adapter1).getItem(position);

            imageAlbumPresenter.onSelectAlbum(item);
        });

        recyclerView.setAdapter(adapter);

    }

    @Override
    public void showPhotoList(List<ImagePicture> photoList) {
        int displayWidth = getResources().getDisplayMetrics().widthPixels;
        final int column = 3;
        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(),
                GRID_ROW_COLUMN,
                GridLayoutManager.VERTICAL,
                false) {

            @Override
            public RecyclerView.LayoutParams generateLayoutParams(Context c, AttributeSet attrs) {
                RecyclerView.LayoutParams layoutParams = super.generateLayoutParams(c, attrs);
                layoutParams.height = displayWidth / 3;
                return layoutParams;
            }

            @Override
            public RecyclerView.LayoutParams generateDefaultLayoutParams() {
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, displayWidth / column);
            }
        };
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(layoutManager);
        imagePictureAdapter = new ImagePictureAdapter(getActivity(), photoList, column);
        imagePictureAdapter.setMode(mode);
        imagePictureAdapter.setOnRecyclerItemImageClickListener((view, adapter1, position) -> {
            ImagePicture item = ((ImagePictureAdapter) adapter1).getItem(position);

            if (mode == ImageAlbumActivity.EXTRA_MODE_UPLOAD) {

                imageAlbumPresenter.onSelectPicture(item, position);
                imageAlbumPresenter.onSetupActionbar(buckerId);
            } else {
                callCropActivity(item);
            }
        });

        imagePictureAdapter.setOnLoadMoreCallback(imageId -> {
            imageAlbumPresenter.onLoadMorePhotos(buckerId, imageId);
        });

        recyclerView.setAdapter(imagePictureAdapter);
    }

    @Override
    public void addPhotoList(List<ImagePicture> photoList) {
        imagePictureAdapter.addPhotoList(photoList);
        imagePictureAdapter.notifyDataSetChanged();
    }

    private void callCropActivity(ImagePicture item) {
        String imagePath = item.getImagePath();

        if (TextUtils.isEmpty(imagePath) || imagePath.endsWith("gif")) {
            showWarningToast(JandiApplication.getContext().getString(R.string.jandi_unsupported_type_picture));
            return;
        }

        try {
            Intent cropIntent = new Intent(getContext(), ImageCropPickerActivity.class);
            cropIntent.putExtra("input", Uri.fromFile(new File(imagePath)));
            cropIntent.putExtra("output", Uri.fromFile(File.createTempFile("temp_", ".jpg",
                    new File(FileUtil.getDownloadPath()))));
            startActivityForResult(cropIntent, ModifyProfileActivity.REQUEST_CROP);
        } catch (IOException e) {
            e.printStackTrace();
            Crashlytics.logException(e);
            showWarningToast(JandiApplication.getContext().getString(R.string.jandi_unsupported_type_picture));
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == ModifyProfileActivity.REQUEST_CROP) {
            FragmentActivity activity = getActivity();
            activity.setResult(Activity.RESULT_OK, data);
            activity.finish();
        }
    }

    @Override
    public void setActinbarTitle(String bucketTitle) {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(bucketTitle);
        }
    }

    @Override
    public void notifyItemRangeChanged(int position, int range) {
        recyclerView.getAdapter().notifyItemRangeChanged(position, range);
    }

    @Override
    public void moveImagePicture(int bucketId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = ImageAlbumFragment.create(bucketId, mode);
        fragmentTransaction.replace(R.id.vg_image_album_content, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void showWarningToast(String message) {
        ColoredToast.showWarning(message);
    }

    @Override
    public void notifyItemOptionMenus() {
        getActivity().invalidateOptionsMenu();
    }

}
