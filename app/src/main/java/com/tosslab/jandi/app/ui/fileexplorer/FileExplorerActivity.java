package com.tosslab.jandi.app.ui.fileexplorer;

import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.views.listeners.SimpleOnItemSelectedListner;

import java.util.ArrayList;
import java.util.List;

public class FileExplorerActivity extends BaseAppCompatActivity {
    public static final String DATA_SCHEME_FILE = "file";
    public static final int SPINNER_POSION_SECOND = 1;

    private boolean mountUnmountStateAction = true;
    private boolean toolbarRenewal = false;
    private BroadcastReceiver sdcardStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                ColoredToast.show(getString(R.string.jandi_sdcard_storage_mounted));
                mountUnmountStateAction = true;
            } else if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED)) {
                ColoredToast.showWarning(getString(R.string.jandi_sdcard_storage_unmounted));
                mountUnmountStateAction = false;
            }

            if (action.equals(Intent.ACTION_MEDIA_UNMOUNTED) || action.equals(Intent.ACTION_MEDIA_MOUNTED)) {
                toolbarRenewal = true;
                invalidateOptionsMenu();

                getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                getSupportFragmentManager().beginTransaction()
                        .add(R.id.file_explorer_container, FileExplorerFragment.create())
                        .commit();

            }
        }
    };
    private FileExplorerModel fileExplorerModel;
    private boolean initSpinner;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_explorer);

        setupActionbar();

        fileExplorerModel = new FileExplorerModel();

        getSupportFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, FileExplorerFragment.create())
                .commit();

        initSpinner = true;

    }

    public void sdcardStateReciver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_MEDIA_MOUNTED);
        intentFilter.addAction(Intent.ACTION_MEDIA_UNMOUNTED);
        intentFilter.addDataScheme(DATA_SCHEME_FILE);

        registerReceiver(sdcardStateReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            unregisterReceiver(sdcardStateReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        sdcardStateReciver();
    }

    private void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_file_explorer_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(getString(R.string.jandi_title_activity_file_explorer));
        toolbar.setNavigationIcon(R.drawable.actionbar_icon_back);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        } else if (item.getItemId() == R.id.file_explorer_switch_item) {
            findViewById(R.id.file_explorer_spinner_button).performClick();
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        menu.clear();

        getMenuInflater().inflate(R.menu.file_explorer_toolbar_switch, menu);

        View actionView = menu.findItem(R.id.file_explorer_switch_item).getActionView();
        Spinner rootPathSpinner = (Spinner) actionView.findViewById(R.id.file_explorer_spinner_button);

        List<String> paths = new ArrayList<String>();

        paths.add(getString(R.string.jandi_device_storage));
        if (fileExplorerModel.hasExternalSdCard() && mountUnmountStateAction) {
            paths.add(getString(R.string.jandi_sdcard_storage));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(FileExplorerActivity.this, R.layout.layout_file_explorer_spinner_title, paths);
        adapter.setDropDownViewResource(R.layout.layout_file_explorer_spinner_dropdown);
        rootPathSpinner.setAdapter(adapter);
        rootPathSpinner.setOnItemSelectedListener(new SimpleOnItemSelectedListner() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (initSpinner) {
                    initSpinner = false;
                    return;
                }

                if (!toolbarRenewal) {
                    String movePath = null;

                    if (position == SPINNER_POSION_SECOND) {
                        movePath = fileExplorerModel.getExternalSdCardPath();
                    }


                    FileExplorerFragment fragment = FileExplorerFragment.create(movePath);

                    getFragmentManager().popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

                    getSupportFragmentManager().beginTransaction()
                            .add(R.id.file_explorer_container, fragment, movePath)
                            .commit();
                }

                toolbarRenewal = false;

            }
        });

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        return false;
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
