package com.tosslab.jandi.app.ui.share.multi.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.tosslab.jandi.app.ui.share.multi.adapter.items.ShareFileItemFragment;
import com.tosslab.jandi.app.ui.share.multi.domain.FileShareData;
import com.tosslab.jandi.app.ui.share.multi.domain.ShareData;
import com.tosslab.jandi.app.ui.share.multi.model.SharesDataModel;

public class ShareFragmentPageAdapter extends FragmentStatePagerAdapter {

    private SharesDataModel sharesDataModel;

    public ShareFragmentPageAdapter(FragmentManager fm, SharesDataModel sharesDataModel) {
        super(fm);
        this.sharesDataModel = sharesDataModel;
    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }

    @Override
    public Fragment getItem(int position) {
        ShareData item = sharesDataModel.getItem(position);
        if (item instanceof FileShareData) {
            return ShareFileItemFragment.create(item.getData());
        } else {
            return new Fragment();
        }
    }

    @Override
    public int getCount() {
        if (sharesDataModel == null) {
            return 0;
        }
        return sharesDataModel.size();
    }
}
