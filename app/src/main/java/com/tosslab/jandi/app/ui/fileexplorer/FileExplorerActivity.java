package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.Activity;
import android.os.Bundle;

public class FileExplorerActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction()
                .add(android.R.id.content, FileExplorerFragment_.builder().build())
                .commit();
    }

}
