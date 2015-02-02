package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.Activity;
import android.os.Bundle;

import com.tosslab.jandi.app.events.BackPressedEvent;

import de.greenrobot.event.EventBus;

public class FileExplorerActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .add(android.R.id.content, FileExplorerFragment_.builder().build())
                .commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        EventBus.getDefault().post(new BackPressedEvent());
    }
}
