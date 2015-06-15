package com.tosslab.jandi.app.ui.album.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.fragment.adapter.DefaultAlbumAdapter;
import com.tosslab.jandi.app.ui.album.fragment.adapter.ImagePictureAdapter;
import com.tosslab.jandi.app.ui.album.fragment.presenter.ImageAlbumPresenter;
import com.tosslab.jandi.app.ui.album.fragment.presenter.ImageAlbumPresenterImpl;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImageAlbum;
import com.tosslab.jandi.app.ui.album.fragment.vo.ImagePicture;
import com.tosslab.jandi.app.utils.AnimationModel;
import com.tosslab.jandi.app.views.SimpleDividerItemDecoration;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.util.List;

@EFragment(R.layout.fragment_image_album)
public class ImageAlbumFragment extends Fragment implements ImageAlbumPresenter.View {

    public static final int GRID_ROW_COLUMN = 3;
    public static final int BUCKET_ALL_IMAGE_ALBUM = -2;
    public static final int BUCKET_ALBUM_LIST = -1;
    @ViewById(R.id.rv_image_album)
    RecyclerView recyclerView;

    ImageView ivPreview;

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
        ivPreview = (ImageView) getActivity().findViewById(R.id.iv_image_album_preview);
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


            imageAlbumPresenter.onPreviewImage(view, item.getImagePath());

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

    @Override
    public void showPreview(View thumbView, String imagePath) {
        Glide.with(ImageAlbumFragment.this)
                .load(imagePath)
                .asBitmap()
                .placeholder(new ColorDrawable(Color.TRANSPARENT))
                .fitCenter()
                .into(new SimpleTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(Bitmap resource, GlideAnimation<? super Bitmap> glideAnimation) {

                        ivPreview.setVisibility(View.VISIBLE);
                        animationModel.zoomImageFromThumb(getActivity().findViewById(android.R.id.content), thumbView, ivPreview, resource);
                        ivPreview.setImageBitmap(resource);

                    }
                });

    }
}
