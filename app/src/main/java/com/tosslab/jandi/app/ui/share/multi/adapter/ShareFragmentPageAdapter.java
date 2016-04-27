package com.tosslab.jandi.app.ui.share.multi.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tosslab.jandi.app.ui.share.multi.adapter.items.ShareFileItemFragment;
import com.tosslab.jandi.app.ui.share.multi.domain.FileShareData;
import com.tosslab.jandi.app.ui.share.multi.domain.ShareData;
import com.tosslab.jandi.app.ui.share.multi.model.ShareAdapterDataModel;

import java.util.ArrayList;
import java.util.List;

public class ShareFragmentPageAdapter extends FragmentStatePagerAdapter implements ShareAdapterDataModel, ShareAdapterDataView {

    private List<ShareData> shareDatas;

    public ShareFragmentPageAdapter(FragmentManager fm) {
        super(fm);
        shareDatas = new ArrayList<>();
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        ShareData item = getShareData(position);
        if (item instanceof FileShareData) {
            return ShareFileItemFragment.create(item.getData());
        } else {
            return new Fragment();
        }
    }

    @Override
    public int getCount() {
        return size();
    }

    @Override
    public int size() {
        return shareDatas.size();
    }

    @Override
    public ShareData getShareData(int position) {
        return shareDatas.get(position);
    }

    @Override
    synchronized public void clear() {
        shareDatas.clear();
    }

    @Override
    synchronized public void add(ShareData shareData) {
        shareDatas.add(shareData);
    }

    @Override
    public void addAll(List<ShareData> shareDatas) {
        this.shareDatas.addAll(shareDatas);
    }

    @Override
    public void refresh() {
        notifyDataSetChanged();
    }
}
