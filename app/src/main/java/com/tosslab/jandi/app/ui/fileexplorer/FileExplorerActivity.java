package com.tosslab.jandi.app.ui.fileexplorer;

import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.tosslab.jandi.app.R;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

//@EActivity(R.layout.activity_file_explorer)
public class FileExplorerActivity extends ActionBarActivity {
    private Logger log = Logger.getLogger(FileExplorerActivity.class);

    Switch storageChange;

    String microSdCardPath;


    /*@AfterViews
    public void initView() {

        File[] sdcards = ContextCompat.getExternalFilesDirs(getApplicationContext(), null);
        File storageDir = new File("/storage");

        File[] originFiles = storageDir.listFiles();

        String inStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath().substring(0, Environment.getExternalStorageDirectory().getAbsolutePath().lastIndexOf("/") + 1);
        log.info("inStoragePath!!  : " + inStoragePath);
        log.info("==========");

        microSdCardPath = null;

        for (int i = 0; i < originFiles.length; i++) {
            try {
                if (originFiles[i].canRead() && originFiles[i].isDirectory() && !TextUtils.equals(originFiles[i].getName(), "emulated")
                        && !originFiles[i].getCanonicalPath().contains(inStoragePath)) {
                    log.info("originFiles[i]  microSdCardPath!!");
                    microSdCardPath = originFiles[i].getAbsolutePath();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("===============!!  : ");
        }

        FileExplorerFragment fragment = FileExplorerFragment_.builder()
                .currentPath(microSdCardPath).microSdCardPath(microSdCardPath)
                .build();

        getFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, fragment, microSdCardPath)
                        //.add(R.id.file_explorer_container, FileExplorerFragment_.builder().build())
                .commit();
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_file_explorer);

        File storageDir = new File("/storage");

        File[] originFiles = storageDir.listFiles();

        String inStoragePath = Environment.getExternalStorageDirectory().getAbsolutePath().substring(0, Environment.getExternalStorageDirectory().getAbsolutePath().lastIndexOf("/") + 1);
        log.info("inStoragePath!!  : " + inStoragePath);
        log.info("==========");

        microSdCardPath = null;

        for (int i = 0; i < originFiles.length; i++) {

            try {
                if (originFiles[i].canRead() && originFiles[i].isDirectory() && !TextUtils.equals(originFiles[i].getName(), "emulated")
                        && !originFiles[i].getCanonicalPath().contains(inStoragePath)) {
                    log.info("originFiles[i]  microSdCardPath!!");
                    microSdCardPath = originFiles[i].getAbsolutePath();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            log.info("===============!!  : ");
        }


        getFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, FileExplorerFragment_.builder().build())
                .commit();

        storageChange = (Switch) findViewById(R.id.file_explorer_change);

        if (microSdCardPath == null) {
            storageChange.setVisibility(View.GONE);
        }

        storageChange.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                log.info("switch buotton microSdCardPath : " + microSdCardPath);
                log.info("switch buotton isChecked : " + isChecked);

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

    /*@CheckedChange(R.id.file_explorer_change)
    public void setStorageChangehange(CompoundButton button, boolean isChecked) {
        log.info("switch buotton microSdCardPath : " + microSdCardPath);
        log.info("switch buotton isChecked : " + isChecked);
        if (isChecked) {
            microSdCardPath = null;
            storageChange.setChecked(false);
        } else {
            storageChange.setChecked(true);
        }

        FileExplorerFragment fragment = FileExplorerFragment_.builder()
                .currentPath(microSdCardPath).microSdCardPath(microSdCardPath)
                .build();

        getFragmentManager().beginTransaction()
                .add(R.id.file_explorer_container, fragment, microSdCardPath)
                .commit();
    }*/

    @Override
    public void onBackPressed() {
        log.info("activity onBackPressed");
        //super.onBackPressed();
        //OnItemClicked();

        log.info("activity getBackStackEntryCount() : " + getFragmentManager().getBackStackEntryCount());
        if (getFragmentManager().getBackStackEntryCount() > 0) {
            log.info("activity getBackStackEntryCount() > 0!!");
            getFragmentManager().popBackStack();
        } else {
            log.info("activity getFragmentManager().getBackStackEntryCount() : " + getFragmentManager().getBackStackEntryCount());
            log.info("activity activity finish!!");
            finish();
        }
    }
}
