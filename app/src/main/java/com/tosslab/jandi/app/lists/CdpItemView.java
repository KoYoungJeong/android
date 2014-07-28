package com.tosslab.jandi.app.lists;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_cdp_entity)
public class CdpItemView extends FrameLayout {
    @ViewById(R.id.tv_cdp_item_name)
    TextView txtCdpItemName;
    @ViewById(R.id.ly_title_cdp)
    LinearLayout lyTitleCdp;
    @ViewById(R.id.txt_title_cdp_name)
    TextView txtTitleCdpName;


    public CdpItemView(Context context) {
        super(context);
    }

    public void bind(CdpItem cdp) {
        setAllVisibilitiesByGone();
        switch (cdp.type) {
            case JandiConstants.TYPE_TITLE_JOINED_CHANNEL:
                lyTitleCdp.setVisibility(VISIBLE);
                txtTitleCdpName.setText(R.string.jandi_cdp_title_channel);
                break;
            case JandiConstants.TYPE_TITLE_UNJOINED_CHANNEL:
                txtCdpItemName.setVisibility(VISIBLE);
                txtCdpItemName.setText(cdp.name);
                break;
            case JandiConstants.TYPE_TITLE_DIRECT_MESSAGE:
                lyTitleCdp.setVisibility(VISIBLE);
                txtTitleCdpName.setText(R.string.jandi_cdp_title_direct_message);
                break;
            case JandiConstants.TYPE_TITLE_PRIVATE_GROUP:
                lyTitleCdp.setVisibility(VISIBLE);
                txtTitleCdpName.setText(R.string.jandi_cdp_title_private_group);
                break;
            case JandiConstants.TYPE_CHANNEL:
            case JandiConstants.TYPE_DIRECT_MESSAGE:
            default:
                txtCdpItemName.setVisibility(VISIBLE);
                txtCdpItemName.setText(cdp.toString());
                break;
        }
    }

    private void setAllVisibilitiesByGone() {
        txtCdpItemName.setVisibility(GONE);
        lyTitleCdp.setVisibility(GONE);
    }
}
