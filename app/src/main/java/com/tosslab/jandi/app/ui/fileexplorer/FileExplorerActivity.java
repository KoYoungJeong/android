package com.tosslab.jandi.app.ui.fileexplorer;

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

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel_;
import com.tosslab.jandi.app.views.listeners.SimpleOnItemSelectedListner;

import java.util.ArrayList;
import java.util.List;

public class FileExplorerActivity extends ActionBarActivity {

    private FileExplorerModel fileExplorerModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_explorer);

        setupActionbar();

        fileExplorerModel = FileExplorerModel_.getInstance_(FileExplorerActivity.this);

        getFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, FileExplorerFragment_.builder().build())
                .commit();

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
            finish();
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
