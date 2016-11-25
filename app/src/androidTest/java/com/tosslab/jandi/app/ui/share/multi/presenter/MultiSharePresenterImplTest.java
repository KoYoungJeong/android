package com.tosslab.jandi.app.ui.share.multi.presenter;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.share.multi.dagger.MultiShareModule;
import com.tosslab.jandi.app.ui.share.multi.domain.FileShareData;
import com.tosslab.jandi.app.ui.share.multi.model.ShareAdapterDataModel;
import com.tosslab.jandi.app.utils.file.ImageFilePath;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dagger.Component;
import setup.BaseInitUtil;

import static com.jayway.awaitility.Awaitility.await;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(AndroidJUnit4.class)
public class MultiSharePresenterImplTest {


    private ShareAdapterDataModel mockDataModel;
    private MultiSharePresenter.View mockView;
    private MultiSharePresenter multiSharePresenter;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Before
    public void setUp() throws Exception {

        mockDataModel = mock(ShareAdapterDataModel.class);
        mockView = mock(MultiSharePresenter.View.class);
//        multiSharePresenter = new MultiSharePresenterImpl(mockView, mockDataModel, shareModel);
//        ((MultiSharePresenterImpl) multiSharePresenter).teamInfoLoader = ShareModel_.getInstance_(JandiApplication.getContext()).getTeamInfoLoader(TeamInfoLoader.getInstance().getTeamId());
    }

    @Test
    public void testOnRoomChange() throws Exception {
        multiSharePresenter.onRoomChange();

        verify(mockView).callRoomSelector(anyLong());
    }

    @Test
    public void testOnSelectTeam() throws Exception {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        long defaultTopicId = TeamInfoLoader.getInstance().getDefaultTopicId();

        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).setMentionInfo(eq(teamId), eq(defaultTopicId));

        multiSharePresenter.onSelectTeam(teamId);

        await().until(() -> finish[0]);

        verify(mockView).setTeamName(anyString());
        verify(mockView).setRoomName(anyString());
        verify(mockView).setMentionInfo(eq(teamId), eq(defaultTopicId));
    }

    @Test
    public void testInitShareData() throws Exception {
        final boolean[] finish = {false};
        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(mockView).setFileTitle(anyString());

        int limit = 2;
        List<String> imagePathList = getImagePathList(limit);

        if (imagePathList == null || imagePathList.isEmpty()) return;

        multiSharePresenter.initShareData(imagePathList);
        doReturn(new FileShareData(ImageFilePath.getPath(JandiApplication.getContext(), Uri.parse(imagePathList.get(0))))).when(mockDataModel).getShareData(eq(0));
        doReturn(2).when(mockDataModel).size();

        await().timeout(1, TimeUnit.MINUTES).until(() -> finish[0]);

        verify(mockView).setFileTitle(anyString());
        verify(mockView).updateFiles(eq(2));
        verify(mockDataModel).clear();
        verify(mockDataModel).addAll(any());
    }

    @Test
    public void testOnSelectRoom() throws Exception {
        multiSharePresenter.onSelectRoom(TeamInfoLoader.getInstance().getDefaultTopicId(), JandiConstants.TYPE_PUBLIC_TOPIC);

        verify(mockView).setRoomName(anyString());
        verify(mockView).setMentionInfo(anyLong(), anyLong());
    }

    @Test
    public void testOnFilePageChanged() throws Exception {
        String filePath = "/hello.txt";
        ((MultiSharePresenterImpl) multiSharePresenter).comments.add("");
        when(mockDataModel.getShareData(0)).thenReturn(new FileShareData(filePath));
        multiSharePresenter.onFilePageChanged(0, "ads");
        verify(mockView).setFileTitle(eq("hello.txt"));
        verify(mockView).setCommentText(eq("ads"));
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

    @Component(modules = {MultiShareModule.class, ApiClientModule.class})
    public interface TestComponent {
        void inject(MultiSharePresenterImplTest test);
    }
}