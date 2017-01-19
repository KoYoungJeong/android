package com.tosslab.jandi.app.ui.share.multi.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tosslab.jandi.app.ui.share.multi.adapter.items.ShareFileItemFragment;
import com.tosslab.jandi.app.ui.share.multi.domain.FileShareData;
import com.tosslab.jandi.app.ui.share.multi.domain.ShareData;
import com.tosslab.jandi.app.ui.share.multi.interaction.FileShareInteractor;
import com.tosslab.jandi.app.ui.share.multi.model.ShareAdapterDataModel;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

public class ShareFragmentPageAdapter extends FragmentStatePagerAdapter implements ShareAdapterDataModel, ShareAdapterDataView, FileShareInteractor {

    private final FileShareInteractor.Wrapper contentWrapper;
    private List<ShareData> shareDatas;
    private WeakHashMap<Integer, ShareFileItemFragment> contents;

    public ShareFragmentPageAdapter(FragmentManager fm, FileShareInteractor.Wrapper contentWrapper) {
        super(fm);
        this.contentWrapper = contentWrapper;
        shareDatas = new ArrayList<>();
        contents = new WeakHashMap<>();
    }

    @Override
    public int getItemPosition(Object object) {

        for (int idx = 0; idx < getCount(); idx++) {
            if (contents.containsKey(idx)) {
                if (contents.get(idx) == object) {
                    return idx;
                }
            }
        }

        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        ShareData item = getShareData(position);
        if (item == null || !(item instanceof FileShareData)) {
            return new Fragment();
        } else {
            ShareFileItemFragment shareFileItemFragment;
            if (contents.containsKey(position)) {
                shareFileItemFragment = contents.get(position);
            } else {
                shareFileItemFragment = ShareFileItemFragment.create(item.getData(), getCount() > 1);
                contents.put(position, shareFileItemFragment);
            }

            shareFileItemFragment.setFileInterator(this);

            return shareFileItemFragment;
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
        int size = size();
        if (size <= 0 || position >= size) {
            return null;
        }
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

    @Override
    public void onClickContent() {
        contentWrapper.toggleContent();
    }

    @Override
    public void onFocusContent(boolean focus) {
        for (Content content : contents.values()) {
            content.onFocusContent(focus);
        }
    }
}
