package com.tosslab.jandi.app.ui.album.fragment;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.fragment.adapter.DefaultAlbumAdapter;
import com.tosslab.jandi.app.ui.album.fragment.adapter.ImagePictureAdapter;
import com.tosslab.jandi.app.ui.album.fragment.presenter.ImageAlbumPresenter;
import com.tosslab.jandi.app.ui.album.fragment.presenter.ImageAlbumPresenterImpl;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImagePicture;
import com.tosslab.jandi.app.utils.AnimationModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EFragment(R.layout.fragment_image_album)
public class ImageAlbumFragment extends Fragment implements ImageAlbumPresenter.View {

    public static final int GRID_ROW_COLUMN = 3;
    public static final int BUCKET_ALL_IMAGE_ALBUM = -2;
    public static final int BUCKET_ALBUM_LIST = -1;
    @ViewById(R.id.rv_image_album)
    RecyclerView recyclerView;

    AnimationModel animationModel;

    @Bean(ImageAlbumPresenterImpl.class)
    ImageAlbumPresenter imageAlbumPresenter;

    @FragmentArg
    int buckerId = BUCKET_ALBUM_LIST;

    @AfterInject
    void initObject() {
        imageAlbumPresenter.setView(this);
        animationModel = new AnimationModel();
    }

    @AfterViews
    void initViews() {
        imageAlbumPresenter.onLoadImageAlbum(buckerId);
        imageAlbumPresenter.onSetupActionbar(buckerId);
    }

    @Override
    public void showDefaultAlbumList(List<ImageAlbum> defaultAlbumList) {

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity()));
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
                return new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, displayWidth / 3);
            }
        };
        recyclerView.setItemAnimator(null);
        recyclerView.setLayoutManager(layoutManager);
        ImagePictureAdapter adapter = new ImagePictureAdapter(getActivity(), photoList);
        adapter.setOnRecyclerItemCheckClickListener((view, adapter1, position) -> {
            ImagePicture item = ((ImagePictureAdapter) adapter1).getItem(position);

            imageAlbumPresenter.onSelectPicture(item, position);
            imageAlbumPresenter.onSetupActionbar(buckerId);

        });

        adapter.setOnRecyclerItemImageClickListener((view, adapter1, position) -> {
            ImagePicture item = ((ImagePictureAdapter) adapter1).getItem(position);

            imageAlbumPresenter.onSelectPicture(item, position);
            imageAlbumPresenter.onSetupActionbar(buckerId);

        });
        recyclerView.setAdapter(adapter);

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
        Fragment fragment = ImageAlbumFragment_.builder().buckerId(bucketId).build();
        fragmentTransaction.replace(R.id.vg_image_album_content, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showWarningToast(String message) {
        ColoredToast.showWarning(getActivity(), message);
    }

}
