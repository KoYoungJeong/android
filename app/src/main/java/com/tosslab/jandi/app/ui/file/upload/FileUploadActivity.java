package com.tosslab.jandi.app.ui.file.upload;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.file.upload.adapter.FileUploadPagerAdapter;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;

import java.util.ArrayList;

/**
 * Created by Bill MinWook Heo on 15. 6. 15..
 */
@EActivity(R.layout.activity_file_upload_insert_commnet)
@OptionsMenu(R.menu.file_insert_comment_menu)
public class FileUploadActivity extends AppCompatActivity implements FileUploadFragment.OnSendImageListener {

    @Extra
    int selectedEntityIdToBeShared;    // Share 할 chat-room
    @Extra
    String realFilePath;

    @Extra
    ArrayList<String> realFilePathList;

    @ViewById(R.id.vp_preview_file_comment)
    ViewPager viewPager;

    @AfterViews
    public void initView() {
        FileUploadPagerAdapter fileUploadPagerAdapter = new FileUploadPagerAdapter
                (getSupportFragmentManager(), realFilePathList, selectedEntityIdToBeShared);

        viewPager.setAdapter(fileUploadPagerAdapter);
        setupActionbar();
    }

    @OptionsItem(android.R.id.home)
    void onBackPress() {
        finish();
    }

    @OptionsItem(R.id.action_confirm)
    void onSendFile() {

    }

    private void setupActionbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.layout_file_upload_insert_commnet_bar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(
                    new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        }
    }

    @Override
    public void onSendImage(String title, int entityId, String realFilePath, String comment) {
        LogUtil.d("activity imageSend!!!");

        finish();
    }

}
