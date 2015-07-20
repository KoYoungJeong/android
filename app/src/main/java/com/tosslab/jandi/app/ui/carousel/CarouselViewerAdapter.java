package com.tosslab.jandi.app.ui.carousel;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tosslab.jandi.app.ui.carousel.domain.CarouselFileInfo;
import com.tosslab.jandi.app.ui.photo.PhotoViewFragment;
import com.tosslab.jandi.app.ui.photo.PhotoViewFragment_;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

/**
 * Created by Bill MinWook Heo on 15. 6. 23..
 */
public class CarouselViewerAdapter extends FragmentStatePagerAdapter {

    private List<CarouselFileInfo> carouselFileInfos;

    private WeakHashMap<Integer, Fragment> weakHashMap;
    private boolean isModified;
    private CarouselViewerActivity.OnCarouselImageClickListener carouselImageClickListener;

    public CarouselViewerAdapter(FragmentManager fm) {
        super(fm);
        carouselFileInfos = new ArrayList<CarouselFileInfo>();
        weakHashMap = new WeakHashMap<Integer, Fragment>();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {

        CarouselFileInfo fileInfo = carouselFileInfos.get(position);
        int fileLinkId = fileInfo.getFileLinkId();

        Fragment fragment;

        if (weakHashMap.containsKey(fileLinkId)) {
            fragment = weakHashMap.get(fileLinkId);
        } else {
            fragment = PhotoViewFragment_.builder()
                    .imageType(fileInfo.getFileType())
                    .imageUrl(fileInfo.getFileLinkUrl())
                    .build();

//            weakHashMap.put(fileLinkId, fragment);
        }

        if (fragment instanceof PhotoViewFragment) {
            PhotoViewFragment photoViewFragment = (PhotoViewFragment) fragment;
            photoViewFragment.setOnCarouselImageClickListener(carouselImageClickListener);
        }

        return fragment;
    }

    @Override
    public int getCount() {
        if (carouselFileInfos == null) {
            return 0;
        }
        return carouselFileInfos.size();
    }

    public void addAll(List<CarouselFileInfo> fileInfos) {
        isModified = true;
        carouselFileInfos.addAll(fileInfos);
    }

    public void addAll(int position, List<CarouselFileInfo> imageFiles) {
        isModified = true;
        carouselFileInfos.addAll(position, imageFiles);
    }

    public void add(CarouselFileInfo fileInfo) {
        isModified = true;
        carouselFileInfos.add(fileInfo);
    }

    public CarouselFileInfo getFileInfo(int position) {
        return carouselFileInfos.get(position);
    }

    public void setCarouselImageClickListener(CarouselViewerActivity.OnCarouselImageClickListener
                                                      carouselImageClickListener) {

        this.carouselImageClickListener = carouselImageClickListener;
    }
}
