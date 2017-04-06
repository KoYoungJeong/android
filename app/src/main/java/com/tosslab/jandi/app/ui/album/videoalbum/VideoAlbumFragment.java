package com.tosslab.jandi.app.ui.album.videoalbum;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.Henson;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.files.upload.model.FilePickerModel;
import com.tosslab.jandi.app.ui.album.videoalbum.adapter.DefaultVideoAlbumAdapter;
import com.tosslab.jandi.app.ui.album.videoalbum.adapter.VideoItemAdapter;
import com.tosslab.jandi.app.ui.album.videoalbum.dagger.DaggerVideoAlbumFragComponent;
import com.tosslab.jandi.app.ui.album.videoalbum.dagger.VideoAlbumFragModule;
import com.tosslab.jandi.app.ui.album.videoalbum.presenter.VideoAlbumPresenter;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.VideoAlbum;
import com.tosslab.jandi.app.ui.album.videoalbum.vo.VideoItem;
import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewActivity;
import com.tosslab.jandi.app.utils.AnimationModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.views.decoration.SimpleDividerItemDecoration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;

public class VideoAlbumFragment extends Fragment implements VideoAlbumPresenter.View {

    public static final int GRID_ROW_COLUMN = 3;
    public static final int BUCKET_ALL_VIDEO_ALBUM = -2;
    public static final int BUCKET_ALBUM_LIST = -1;

    @Bind(R.id.rv_image_album)
    RecyclerView recyclerView;

    @Inject
    AnimationModel animationModel;

    @Inject
    VideoAlbumPresenter videoAlbumPresenter;

    @Nullable
    @InjectExtra
    int buckerId = BUCKET_ALBUM_LIST;

    @Nullable
    @InjectExtra
    long entityId;

    private ProgressWheel progressWheel;
    private VideoItemAdapter videoItemAdapter;

    public static VideoAlbumFragment create(int buckerId, long entityId) {
        VideoAlbumFragment fragment = new VideoAlbumFragment();
        Bundle args = new Bundle();
        args.putInt("buckerId", buckerId);
        args.putLong("entityId", entityId);
        fragment.setArguments(args);
        return fragment;
    }

    public static VideoAlbumFragment create(long entityId) {
        VideoAlbumFragment fragment = new VideoAlbumFragment();
        Bundle args = new Bundle();
        args.putLong("entityId", entityId);
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
        DaggerVideoAlbumFragComponent.builder()
                .videoAlbumFragModule(new VideoAlbumFragModule(this))
                .build()
                .inject(this);

        initViews();
    }

    void initViews() {
        progressWheel = new ProgressWheel(getActivity());

        videoAlbumPresenter.onLoadVideoAlbum(buckerId);
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
    public void showDefaultAlbumList(List<VideoAlbum> defaultAlbumList) {
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration());
        recyclerView.setItemAnimator(null);
        DefaultVideoAlbumAdapter adapter = new DefaultVideoAlbumAdapter(getActivity(), defaultAlbumList);

        adapter.setOnRecyclerItemClickListener((view, adapter1, position) -> {
            VideoAlbum item = ((DefaultVideoAlbumAdapter) adapter1).getItem(position);
            videoAlbumPresenter.onSelectAlbum(item);
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    public void showVideoList(List<VideoItem> videoList) {
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
        videoItemAdapter = new VideoItemAdapter(getActivity(), videoList, column);
        videoItemAdapter.setOnRecyclerItemImageClickListener((view, adapter1, position) -> {
            VideoItem item = ((VideoItemAdapter) adapter1).getItem(position);
            videoAlbumPresenter.onSelectVideo(item);
        });

        videoItemAdapter.setOnLoadMoreCallback(imageId -> {
            videoAlbumPresenter.onLoadMoreVideos(buckerId, imageId);
        });

        recyclerView.setAdapter(videoItemAdapter);
    }

    @Override
    public void addVideoList(List<VideoItem> videoItemList) {
        videoItemAdapter.addVideoList(videoItemList);
        videoItemAdapter.notifyDataSetChanged();
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
    public void moveVideoItem(int bucketId) {
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment fragment = VideoAlbumFragment.create(bucketId, entityId);
        fragmentTransaction.replace(R.id.vg_image_album_content, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commitAllowingStateLoss();
    }

    @Override
    public void showWarningToast(String message) {
        ColoredToast.showWarning(message);
    }

    @Override
    public void notifyItemOptionMenus() {
        getActivity().invalidateOptionsMenu();
    }

    @Override
    public void moveFileUploadActivity(ArrayList<String> filePaths) {
        if (!isOverFileSize(filePaths)) {
            startActivityForResult(Henson.with(getContext())
                    .gotoFileUploadPreviewActivity()
                    .realFilePathList(filePaths)
                    .selectedEntityIdToBeShared(entityId)
                    .from(FileUploadPreviewActivity.FROM_SELECT_VIDEO)
                    .build(), (FileUploadPreviewActivity.REQUEST_CODE));
        } else {
            ColoredToast.showError(getString(R.string.err_file_upload_failed));
        }
    }

    private boolean isOverFileSize(List<String> selectedVideosPathList) {
        File uploadFile;
        for (String filePath : selectedVideosPathList) {
            uploadFile = new File(filePath);
            if (uploadFile.exists()) {
                if (uploadFile.length() > FilePickerModel.MAX_FILE_SIZE) {
                    return true;
                }
            }

        }
        return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FileUploadPreviewActivity.REQUEST_CODE) {
            onFileUploadActivityResult(resultCode);
        }
    }

    void onFileUploadActivityResult(int resultCode) {
        if (resultCode == Activity.RESULT_OK) {
            getActivity().finish();
        }
    }
}
