package com.tosslab.jandi.app.ui.album;

import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.ui.album.fragment.ImageAlbumFragment_;
import com.tosslab.jandi.app.ui.album.fragment.vo.SelectPictures;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OptionsItem;
import org.androidannotations.annotations.ViewById;

/**
 * Created by Steve SeongUg Jung on 15. 6. 12..
 */
@EActivity(R.layout.activity_image_album)
public class ImageAlbumActivity extends AppCompatActivity {

    @ViewById(R.id.vg_image_album_content)
    ViewGroup contentLayout;

    @AfterViews
    void initViews() {

        SelectPictures.getSelectPictures().clear();

        setupActionbar();

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        Fragment fragment = ImageAlbumFragment_.builder().build();
        fragmentTransaction.replace(R.id.vg_image_album_content, fragment);
        fragmentTransaction.commit();

    }

    private void setupActionbar() {

        if (getSupportActionBar() == null) {
            Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_image_album);
            setSupportActionBar(toolbar);

            ActionBar actionBar = getSupportActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayUseLogoEnabled(false);
            actionBar.setIcon(new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        }

        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle(R.string.jandI_select_gallery);

    }

    @OptionsItem(android.R.id.home)
    void onHomeMenuClick() {
        onBackPressed();
    }
}

