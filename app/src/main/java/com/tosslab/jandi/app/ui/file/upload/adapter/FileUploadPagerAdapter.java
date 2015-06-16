package com.tosslab.jandi.app.ui.file.upload.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tosslab.jandi.app.ui.file.upload.FileUploadFragment_;

import java.util.ArrayList;

/**
 * Created by Bill MinWook Heo on 15. 6. 15..
 */

public class FileUploadPagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<String> realFilePath;
    private int currentEntityId;

    public FileUploadPagerAdapter(FragmentManager fm, ArrayList<String> realFilePath, int
            currentEntityId) {
        super(fm);
        this.realFilePath = realFilePath;
        this.currentEntityId = currentEntityId;
    }

    @Override
    public Fragment getItem(int position) {
        return FileUploadFragment_
                .builder()
                .realFilePath(realFilePath.get(position))
                .currentEntityId(currentEntityId)
                .build();
    }

    @Override
    public int getCount() {
        return realFilePath.size();
    }


}
