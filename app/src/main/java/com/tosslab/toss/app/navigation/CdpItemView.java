package com.tosslab.toss.app.navigation;/**
 * Created by justinygchoi on 2014. 5. 27..
 */

import android.content.Context;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.toss.app.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_cdp_entity)
public class CdpItemView extends LinearLayout {
    @ViewById(R.id.tv_cdp_item_name)
    TextView txtCdpItemName;

    public CdpItemView(Context context) {
        super(context);
    }

    public void bind(CdpItem cdp) {
        txtCdpItemName.setText(cdp.name);
    }
}
