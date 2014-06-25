package com.tosslab.toss.app.lists;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.tosslab.toss.app.R;

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
}
