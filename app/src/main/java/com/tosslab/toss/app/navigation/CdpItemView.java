package com.tosslab.toss.app.navigation;

/**
 * Created by justinygchoi on 2014. 5. 27..
 */

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
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
    @ViewById(R.id.ly_title_unjoined_channel)
    LinearLayout lyTitleUnJoinedChannel;
    @ViewById(R.id.ly_title_joined_channel)
    LinearLayout lyTitleJoinedChannel;
    @ViewById(R.id.ly_title_private_group)
    LinearLayout lyTitlePrivateGroup;
    @ViewById(R.id.btn_action_add_channel)
    ImageButton btnActionAddChannel;

    public CdpItemView(Context context) {
        super(context);
    }

    public void bind(CdpItem cdp) {
        switch (cdp.type) {
            case TossConstants.TYPE_TITLE_JOINED_CHANNEL:
                lyTitleJoinedChannel.setVisibility(VISIBLE);
                lyTitleUnJoinedChannel.setVisibility(GONE);
                lyTitleDirectMessage.setVisibility(GONE);
                lyTitlePrivateGroup.setVisibility(GONE);
                txtCdpItemName.setVisibility(GONE);
                btnActionAddChannel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.e("HAHAHAHA", "Click");
                    }
                });
                break;
            case TossConstants.TYPE_TITLE_UNJOINED_CHANNEL:
                lyTitleJoinedChannel.setVisibility(GONE);
                lyTitleUnJoinedChannel.setVisibility(VISIBLE);
                lyTitleDirectMessage.setVisibility(GONE);
                lyTitlePrivateGroup.setVisibility(GONE);
                txtCdpItemName.setVisibility(GONE);
                break;
            case TossConstants.TYPE_TITLE_DIRECT_MESSAGE:
                lyTitleJoinedChannel.setVisibility(GONE);
                lyTitleUnJoinedChannel.setVisibility(GONE);
                lyTitleDirectMessage.setVisibility(VISIBLE);
                lyTitlePrivateGroup.setVisibility(GONE);
                txtCdpItemName.setVisibility(GONE);
                break;
            case TossConstants.TYPE_TITLE_PRIVATE_GROUP:
                lyTitleJoinedChannel.setVisibility(GONE);
                lyTitleUnJoinedChannel.setVisibility(GONE);
                lyTitleDirectMessage.setVisibility(GONE);
                lyTitlePrivateGroup.setVisibility(VISIBLE);
                txtCdpItemName.setVisibility(GONE);
                break;
            default:
                lyTitleJoinedChannel.setVisibility(GONE);
                lyTitleUnJoinedChannel.setVisibility(GONE);
                lyTitleDirectMessage.setVisibility(GONE);
                lyTitlePrivateGroup.setVisibility(GONE);
                txtCdpItemName.setVisibility(VISIBLE);

                txtCdpItemName.setText(cdp.name);
                break;
        }

    }
}
