package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel_;
import com.tosslab.jandi.app.views.listeners.SimpleOnItemSelectedListner;

import java.util.ArrayList;
import java.util.List;

public class FileExplorerActivity extends ActionBarActivity {

    private FileExplorerModel fileExplorerModel;

    private boolean mountUnmountStateAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_explorer);

        setupActionbar();

        fileExplorerModel = FileExplorerModel_.getInstance_(FileExplorerActivity.this);

        getFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, FileExplorerFragment_.builder().build())
                .commit();

        sdcardStateReciver();
    }

    public void sdcardStateReciver() {
        // install an intent filter to receive SD card related events.
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_STARTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_SCANNER_FINISHED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);
        intentFilter.addDataScheme("file");

        BroadcastReceiver sdcardStateReceiver1 = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                mountUnmountStateAction = true;
                Boolean state = false;
                String action = intent.getAction();
                if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                    Toast.makeText(context, "SD Card mounted", Toast.LENGTH_LONG).show();
                    state = true;

                } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                    Toast.makeText(context, "SD Card unmounted", Toast.LENGTH_LONG).show();
                    state = false;

                } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_STARTED)) {
                    Toast.makeText(context, "SD Card scanner started", Toast.LENGTH_LONG).show();

                    getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    getFragmentManager().beginTransaction()
                            .add(R.id.file_explorer_container, FileExplorerFragment_.builder().build())
                            .commit();

                    invalidateOptionsMenu();
                    //
                } else if (action.equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
                    Toast.makeText(context, "SD Card scanner finished", Toast.LENGTH_LONG).show();

                } else if (action.equals(Intent.ACTION_MEDIA_EJECT)) {
                    Toast.makeText(context, "SD Card eject", Toast.LENGTH_LONG).show();
                }
            }
        };
        registerReceiver(sdcardStateReceiver1, intentFilter);
    }

    private void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_file_explorer_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.jandi_title_activity_file_explorer));
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                finish();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.clear();

        getMenuInflater().inflate(R.menu.file_explorer_toolbar_switch, menu);

        View actionView = menu.findItem(R.id.file_explorer_switch_item).getActionView();
        Spinner rootPathSpinner = (Spinner) actionView.findViewById(R.id.file_explorer_spinner_button);

        List<String> paths = new ArrayList<String>();

        paths.add(getString(R.string.jandi_device_storage));

        if (fileExplorerModel.hasExternalSdCard()) {
            paths.add(getString(R.string.jandi_sdcard_storage));
        }


        SpinnerAdapter getAdapter = rootPathSpinner.getAdapter();


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(FileExplorerActivity.this, R.layout.layout_file_explorer_spinner_title, paths);
        adapter.setDropDownViewResource(R.layout.layout_file_explorer_spinner_dropdown);
        rootPathSpinner.setAdapter(adapter);
        rootPathSpinner.setOnItemSelectedListener(new SimpleOnItemSelectedListner() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                //if (mountUnmountStateAction) {
                String movePath = null;

                if (position == 1) {
                    movePath = fileExplorerModel.getExternalSdCardPath();
                }

                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                FileExplorerFragment fragment = FileExplorerFragment_.builder()
                        .currentPath(movePath)
                        .build();

                getFragmentManager().beginTransaction()
                        .add(R.id.file_explorer_container, fragment, movePath)
                        .commit();
                //}

            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        menu.clear();

        getMenuInflater().inflate(R.menu.file_explorer_toolbar_switch, menu);

        View actionView = menu.findItem(R.id.file_explorer_switch_item).getActionView();
        Spinner rootPathSpinner = (Spinner) actionView.findViewById(R.id.file_explorer_spinner_button);

        List<String> paths = new ArrayList<String>();

        paths.add(getString(R.string.jandi_device_storage));

        if (fileExplorerModel.hasExternalSdCard()) {
            paths.add(getString(R.string.jandi_sdcard_storage));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(FileExplorerActivity.this, R.layout.layout_file_explorer_spinner_title, paths);
        adapter.setDropDownViewResource(R.layout.layout_file_explorer_spinner_dropdown);
        rootPathSpinner.setAdapter(adapter);
        rootPathSpinner.setOnItemSelectedListener(new SimpleOnItemSelectedListner() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String movePath = null;

                if (position == 1) {
                    movePath = fileExplorerModel.getExternalSdCardPath();
                }

                getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                FileExplorerFragment fragment = FileExplorerFragment_.builder()
                        .currentPath(movePath)
                        .build();

                getFragmentManager().beginTransaction()
                        .add(R.id.file_explorer_container, fragment, movePath)
                        .commit();
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            getFragmentManager().popBackStack();
        } else {
            finish();
        }
    }
}
