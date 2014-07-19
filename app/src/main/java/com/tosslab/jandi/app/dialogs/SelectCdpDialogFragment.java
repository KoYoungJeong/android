package com.tosslab.jandi.app.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;

import com.tosslab.jandi.app.FileDetailActivity2;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.ConfirmShareEvent;
import com.tosslab.jandi.app.lists.CdpArrayAdapter;
import com.tosslab.jandi.app.lists.CdpItem;

import org.apache.log4j.Logger;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 6. 26..
 */
public class SelectCdpDialogFragment extends DialogFragment {
    private final Logger log = Logger.getLogger(SelectCdpDialogFragment.class);
    private CdpArrayAdapter cdpArrayAdapter;

    static private int selectedCdpIdToBeShared;    // Share 할 CDP

    public static SelectCdpDialogFragment newInstance() {
        selectedCdpIdToBeShared = -1;
        SelectCdpDialogFragment frag = new SelectCdpDialogFragment();
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_select_cdp_tbs, null);

        // CDP
        final Spinner spinner = (Spinner)mainView.findViewById(R.id.spinner_select_cdp_tbs);
        spinner.setPrompt("공유");
        cdpArrayAdapter = new CdpArrayAdapter(getActivity(), android.R.layout.simple_spinner_item,
                ((FileDetailActivity2)getActivity()).cdpItemManager.retrieveWithoutTitle());
        spinner.setAdapter(cdpArrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedCdpIdToBeShared = ((CdpItem)adapterView.getItemAtPosition(i)).id;
                log.debug("Select cdp ID to be shared : " + selectedCdpIdToBeShared);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setView(mainView)
                .setIcon(android.R.drawable.ic_menu_agenda)
                .setTitle(R.string.title_cdp_to_be_shared)

                .setPositiveButton(R.string.share,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EventBus.getDefault().post(
                                        new ConfirmShareEvent(selectedCdpIdToBeShared));
                                dismiss();
                            }
                        }
                )
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
