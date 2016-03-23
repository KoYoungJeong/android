package com.tosslab.jandi.app.ui.share.multi;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.FragmentManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

@RunWith(AndroidJUnit4.class)
public class MultiShareFragmentTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<>(BaseAppCompatActivity.class, false, false);

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();
        rule.launchActivity(null);

        FragmentManager supportFragmentManager = rule.getActivity().getSupportFragmentManager();
        ArrayList<Uri> uris = new ArrayList<>();
        Observable.from(getImagePathList(2))
                .map(s -> Uri.parse(s))
                .collect(() -> uris, ArrayList::add)
                .subscribe();
        MultiShareFragment.create(uris);

    }

    @Test
    public void testOnFilePageSelected() throws Exception {

    }

    @Test
    public void testOnScrollButtonClick() throws Exception {

    }

    @Test
    public void testOnTeamNameClick() throws Exception {

    }

    @Test
    public void testOnRoomNameClick() throws Exception {

    }

    @Test
    public void testCallRoomSelector() throws Exception {

    }

    @Test
    public void testUpdateFiles() throws Exception {

    }

    @Test
    public void testMoveIntro() throws Exception {

    }

    @Test
    public void testSetTeamName() throws Exception {

    }

    @Test
    public void testSetRoomName() throws Exception {

    }

    @Test
    public void testSetMentionInfo() throws Exception {

    }

    @Test
    public void testSetFileTitle() throws Exception {

    }

    @Test
    public void testMoveRoom() throws Exception {

    }

    private List<String> getImagePathList(int limit) {
        String[] projection = {
                MediaStore.Images.ImageColumns._ID,
                MediaStore.Images.ImageColumns.DATA};

        String orderBy = String.format("%s DESC", MediaStore.Images.ImageColumns._ID);

        // Get the base URI for the People table in the Contacts content provider.
        Uri images = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                .buildUpon()
                .appendQueryParameter("limit", String.valueOf(limit))
                .build();

        // Make the query.
        StringBuilder sb = new StringBuilder();


        ContentResolver contentResolver = JandiApplication.getContext().getContentResolver();
        Cursor cursor = contentResolver.query(images,
                projection, // Which columns to return
                null,       // Which rows to return (all rows)
                null,       // Selection arguments (none)
                orderBy        // Ordering
        );

        List<String> photos = new ArrayList<>();

        if (cursor == null || cursor.getCount() <= 0) {
            return photos;
        }

//        int idxData = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
        int idxId = cursor.getColumnIndex(MediaStore.Images.ImageColumns._ID);

        while (cursor.moveToNext()) {
            int _id = cursor.getInt(idxId);

            photos.add(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString() + "/" + _id);
        }
        cursor.close();
        return photos;
    }
}