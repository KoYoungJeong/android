package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;

import java.util.List;

/**
 * Created by justinygchoi on 2014. 6. 25..
 */
public class CdpArrayAdapter extends ArrayAdapter<CdpItem> {
    private List<CdpItem> cdpItems;
    private LayoutInflater vi;

    public CdpArrayAdapter(Context context, int textViewResourceId, List<CdpItem> cdpItems) {
        super(context, textViewResourceId, cdpItems);
        this.cdpItems = cdpItems;
    }

    @Override
    public CdpItem getItem(int position) {
        return cdpItems.get(position);
    }

    public int getPosition(int cdpId) {
        for (int i = 0; i < cdpItems.size(); i++) {
            CdpItem cdpItem = cdpItems.get(i);
            if (cdpItem.id == cdpId) {
                return i;
            }
        }
        return -1;
    }
}
