package com.tosslab.jandi.app.ui.file.upload.preview.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tosslab.jandi.app.ui.file.upload.preview.FileUploadPreviewFragment_;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Bill MinWook Heo on 15. 6. 15..
 */

public class FileUploadPagerAdapter extends FragmentStatePagerAdapter {

    private List<String> realFilePath;

    private Map<Integer, Fragment> fragmentMap;

    public FileUploadPagerAdapter(FragmentManager fm, List<String> realFilePath) {
        super(fm);
        fragmentMap = new WeakHashMap<Integer, Fragment>();
        this.realFilePath = realFilePath;
    }

    @Override
    public Fragment getItem(int position) {

        if (fragmentMap.get(position) != null) {
            return fragmentMap.get(position);
        } else {
            Fragment fragment = FileUploadPreviewFragment_
                    .builder()
                    .realFilePath(realFilePath.get(position))
                    .build();
            fragmentMap.put(position, fragment);

            return fragment;
        }

    }

    @Override
    public int getCount() {
        return realFilePath.size();
    }


}
