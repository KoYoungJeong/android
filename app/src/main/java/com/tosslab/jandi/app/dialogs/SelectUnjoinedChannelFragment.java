package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

import com.tosslab.jandi.app.MainActivity;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ConfirmJoinChannelEvent;
import com.tosslab.jandi.app.lists.CdpArrayAdapter;
import com.tosslab.jandi.app.lists.CdpItem;

import org.apache.log4j.Logger;

import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 7. 9..
 */
public class SelectUnjoinedChannelFragment extends DialogFragment {
    private final Logger log = Logger.getLogger(SelectUnjoinedChannelFragment.class);

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<CdpItem> unjoinedChList = ((MainActivity)getActivity()).mCdpItemManager.mUnJoinedChannels;

        final CdpArrayAdapter unjoinedChannels = new CdpArrayAdapter(getActivity()
//                , R.layout.spinner_simple
                , android.R.layout.simple_spinner_item
                , unjoinedChList);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(unjoinedChList.size() + " unjoined channels")
                .setAdapter(unjoinedChannels, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        log.info("Select " + i + "th channel to join");
                        CdpItem unjoinedChannel = unjoinedChannels.getItem(i);
                        EventBus.getDefault().post(new ConfirmJoinChannelEvent(unjoinedChannel.id));
                        dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                // Do Nothing
                            }
                        }
                );
        return builder.create();
    }
}
