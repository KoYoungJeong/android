package com.tosslab.jandi.app.ui.share.multi;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.app.FragmentManager;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity;
import com.tosslab.jandi.app.ui.share.views.ShareSelectTeamActivity;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;

// Fragment add 과정에서 자꾸 에러 나는데 원인 파악이 안됨
@Ignore
@RunWith(AndroidJUnit4.class)
public class MultiShareFragmentTest {

    @Rule
    public IntentsTestRule<BaseAppCompatActivity> rule = new IntentsTestRule<>(BaseAppCompatActivity.class);
    private MultiShareFragment fragment;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Throwable {

        ArrayList<Uri> uris = new ArrayList<>();
        Observable.from(getImagePathList(2))
                .map(Uri::parse)
                .collect(() -> uris, ArrayList::add)
                .subscribe();
        fragment = MultiShareFragment.create(uris);

        FragmentManager fragmentManager = rule.getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .add(android.R.id.content, fragment, fragment.getClass().getSimpleName())
                .commit();

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        await().until(() -> fragment.tvTitle != null && fragment.tvTitle.length() > 0 && fragment.tvTeamName.length() > 0);

        rule.runOnUiThread(() -> {
            fragment.multiSharePresenter.onFilePageChanged(1, "hello1");
            fragment.multiSharePresenter.onFilePageChanged(0, "hello2");
        });
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
    }

    @Test
    public void testOnScrollButtonClick() throws Throwable {
        rule.runOnUiThread(() -> fragment.onScrollButtonClick(fragment.getView().findViewById(R.id.iv_multi_share_next)));
        assertThat(fragment.vpShare.getCurrentItem(), is(equalTo(1)));
    }

    @Test
    public void testOnTeamNameClick() throws Throwable {
        rule.runOnUiThread(fragment::onTeamNameClick);

        Intents.intending(IntentMatchers.hasComponent(ShareSelectTeamActivity.class.getName()));

    }

    @Test
    public void testOnRoomNameClick() throws Throwable {
        rule.runOnUiThread(fragment::onRoomNameClick);

        Intents.intending(IntentMatchers.hasExtra("teamId", TeamInfoLoader.getInstance().getTeamId()));

    }

    @Test
    public void testCallRoomSelector() throws Throwable {
        rule.runOnUiThread(fragment::onRoomNameClick);

        Intents.intending(IntentMatchers.hasExtra("teamId", TeamInfoLoader.getInstance().getTeamId()));

    }

    @Test
    public void testUpdateFiles() throws Throwable {
        rule.runOnUiThread(() -> fragment.updateFiles(2));

        assertThat(fragment.vpShare.getAdapter(), is(notNullValue()));
    }

    @Ignore
    @Test
    public void testMoveIntro() throws Throwable {
        rule.runOnUiThread(fragment::moveIntro);
        Intents.intending(IntentMatchers.hasComponent(IntroActivity.class.getName()));

    }

    @Test
    public void testSetTeamName() throws Throwable {
        String text = "hello";
        rule.runOnUiThread(() -> fragment.setTeamName(text));

        assertThat(fragment.tvTeamName.getText(), is(equalTo(text)));
    }

    @Test
    public void testSetRoomName() throws Throwable {
        String text = "hello";
        rule.runOnUiThread(() -> fragment.setRoomName(text));

        assertThat(fragment.tvRoomName.getText(), is(equalTo(text)));
    }

    @Test
    public void testSetFileTitle() throws Throwable {
        String text = "title";
        rule.runOnUiThread(() -> fragment.setFileName(text));

        assertThat(fragment.tvTitle.getText().toString(), is(equalTo(text)));
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