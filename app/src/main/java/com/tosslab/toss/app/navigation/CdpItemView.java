package com.tosslab.toss.app.navigation;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */

import android.content.Context;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tosslab.toss.app.R;
import com.tosslab.toss.app.TossConstants;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

@EViewGroup(R.layout.item_cdp_entity)
public class CdpItemView extends FrameLayout {
    @ViewById(R.id.tv_cdp_item_name)
    TextView txtCdpItemName;
    @ViewById(R.id.ly_title_direct_message)
    LinearLayout lyTitleDirectMessage;
    @ViewById(R.id.ly_title_joined_channel)
    LinearLayout lyTitleJoinedChannel;
    @ViewById(R.id.ly_title_private_group)
    LinearLayout lyTitlePrivateGroup;

    public CdpItemView(Context context) {
        super(context);
    }

    public void bind(CdpItem cdp) {
        setAllVisibilitiesByGone();
        switch (cdp.type) {
            case TossConstants.TYPE_TITLE_JOINED_CHANNEL:
                lyTitleJoinedChannel.setVisibility(VISIBLE);
                break;
            case TossConstants.TYPE_TITLE_UNJOINED_CHANNEL:
                txtCdpItemName.setVisibility(VISIBLE);
                txtCdpItemName.setText("+ ? more...");
                break;
            case TossConstants.TYPE_TITLE_DIRECT_MESSAGE:
                lyTitleDirectMessage.setVisibility(VISIBLE);
                break;
            case TossConstants.TYPE_TITLE_PRIVATE_GROUP:
                lyTitlePrivateGroup.setVisibility(VISIBLE);
                break;
            case TossConstants.TYPE_CHANNEL:
                txtCdpItemName.setVisibility(VISIBLE);
                txtCdpItemName.setText("#" + cdp.name);
                break;
            case TossConstants.TYPE_DIRECT_MESSAGE:
                setAllVisibilitiesByGone();
                txtCdpItemName.setVisibility(VISIBLE);
                txtCdpItemName.setText("@" + cdp.name);
                break;
            default:
                setAllVisibilitiesByGone();
                txtCdpItemName.setVisibility(VISIBLE);
                txtCdpItemName.setText(cdp.name);
                break;
        }

    }

    private void setAllVisibilitiesByGone() {
        lyTitleJoinedChannel.setVisibility(GONE);
        lyTitleDirectMessage.setVisibility(GONE);
        lyTitlePrivateGroup.setVisibility(GONE);
        txtCdpItemName.setVisibility(GONE);

    }
}
