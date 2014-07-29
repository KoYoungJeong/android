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
import org.apache.log4j.Logger;

@EViewGroup(R.layout.item_cdp_entity)
public class CdpItemView extends FrameLayout {
    private final Logger log = Logger.getLogger(CdpItemView.class);
    @ViewById(R.id.tv_cdp_item_name)
    TextView txtCdpItemName;
    @ViewById(R.id.ly_title_cdp)
    LinearLayout lyTitleCdp;
    @ViewById(R.id.txt_title_cdp_name)
    TextView txtTitleCdpName;


    public CdpItemView(Context context) {
        super(context);
    }

    public void bind(CdpItem cdp, int selectedCdpId) {
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
                if (selectedCdpId == cdp.id) {
                    txtCdpItemName.setTextColor(getResources().getColor(R.color.jandi_main));
                } else {
                    txtCdpItemName.setTextColor(getResources().getColor(R.color.jandi_text_white));
                }
                break;
        }
    }

    private void setAllVisibilitiesByGone() {
        txtCdpItemName.setVisibility(GONE);
        lyTitleCdp.setVisibility(GONE);
    }
}
