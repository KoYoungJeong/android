package com.tosslab.jandi.app.ui.fileexplorer;

import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.fileexplorer.model.FileExplorerModel;

import java.io.File;

public class FileExplorerActivity extends ActionBarActivity {
    private String microSdCardPath;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_explorer);

        setupActionbar();

        File storageDir = new File("/storage");
        File[] originFiles = storageDir.listFiles();
        String inStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath().substring(0, Environment.getExternalStorageDirectory().getAbsolutePath().lastIndexOf("/") + 1);
        FileExplorerModel fileExplorerModel = new FileExplorerModel();

        microSdCardPath = fileExplorerModel.microSdCardPathCheck(originFiles, inStoragePath);

        getFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, FileExplorerFragment_.builder().build())
                .commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    private void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_file_explorer_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("File Explorer");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        if (microSdCardPath != null) {
            getMenuInflater().inflate(R.menu.file_explorer_toolbar_switch, menu);

            View actionView = menu.findItem(R.id.file_explorer_switch_item).getActionView();
            Switch externalSwitch = (Switch) actionView.findViewById(R.id.file_explorer_switch_button);
            externalSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String movePath;

                    if (isChecked) {
                        movePath = microSdCardPath;
                    } else {
                        movePath = null;
                    }

                    FileExplorerFragment fragment = FileExplorerFragment_.builder()
                            .currentPath(movePath).microSdCardPath(movePath)
                            .build();

                    getFragmentManager().beginTransaction()
                            .add(R.id.file_explorer_container, fragment, movePath)
                            .commit();
                }
            });
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onResume() {
        super.onResume();
        invalidateOptionsMenu();
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
