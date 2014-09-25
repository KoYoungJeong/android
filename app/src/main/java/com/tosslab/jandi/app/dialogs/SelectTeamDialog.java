package com.tosslab.jandi.app.dialogs;

import android.app.Dialog;
import android.app.DialogFragment;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.SelectMyTeam;
import com.tosslab.jandi.app.lists.team.TeamListAdapter;
import com.tosslab.jandi.app.network.models.ResMyTeam;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.IOException;
import java.util.List;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 9. 25..
 */
public class SelectTeamDialog extends DialogFragment {
    private final static String ARG_TEAM_LIST    = "teamList";
    private final Logger log = Logger.getLogger(SelectTeamDialog.class);

    public static SelectTeamDialog newInstance(String jsonTeamList) {
        SelectTeamDialog frag = new SelectTeamDialog();
        Bundle args = new Bundle();
        args.putString(ARG_TEAM_LIST, jsonTeamList);
        frag.setArguments(args);
        return frag;
    }

    private ResMyTeam convertJsonToPojo(String jsonTeamList) throws IOException {
        log.debug("JSON Extra : " + jsonTeamList);
        return new ObjectMapper().readValue(jsonTeamList, ResMyTeam.class);
    }

    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final String jsonTeamList = getArguments().getString(ARG_TEAM_LIST, "");

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View mainView = inflater.inflate(R.layout.dialog_team_list, null);
        ListView lv = (ListView) mainView.findViewById(R.id.lv_team_list);

        try {
            ResMyTeam myTeamList = convertJsonToPojo(jsonTeamList);
            List<ResMyTeam.Team> myTeams = myTeamList.teamList;
            final TeamListAdapter teamListAdapter = new TeamListAdapter(getActivity(), myTeams);
            lv.setAdapter(teamListAdapter);
            lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    log.debug(teamListAdapter.getItem(i).name + " Selected");
                    EventBus.getDefault().post(new SelectMyTeam(teamListAdapter.getItem(i)));
                }
            });
        } catch (IOException e) {
            log.error("JSON parsing error", e);
        }

        // creating the fullscreen dialog
        final Dialog dialog = new Dialog(getActivity());
        dialog.setCancelable(false);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(mainView);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        return dialog;
    }
}
