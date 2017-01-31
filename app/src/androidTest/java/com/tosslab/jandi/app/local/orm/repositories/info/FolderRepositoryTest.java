package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.RawInitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.room.TopicRoom;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class FolderRepositoryTest {

    private static String initializeInfo;
    private static long teamId;
    private List<TopicRoom> topicList;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getRawInitializeInfo(teamId);
    }

    @Before
    public void setUp() throws Exception {
        FolderRepository.getInstance().clear();
        InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
        TeamInfoLoader.getInstance().refresh();
        topicList = TeamInfoLoader.getInstance().getTopicList();

    }

    @Test
    public void testGetFolders() throws Exception {
        InitialInfoRepository.getInstance().upsertRawInitialInfo(new RawInitialInfo(teamId, initializeInfo));
        TeamInfoLoader.getInstance().refresh();
    }


    @Test
    public void testAddFolder() throws Exception {
        Folder folder = getFolder();
        boolean success = FolderRepository.getInstance().addFolder(folder);
        assertThat(success).isTrue();

        Folder folder1 = getFolderFromDatabase(folder.getId());

        assertThat(folder1.getId()).isEqualTo(folder.getId());
        assertThat(folder1.getSeq()).isEqualTo(folder.getSeq());
        assertThat(folder1.getName()).isEqualTo(folder.getName());
        assertThat(folder1.isOpened()).isEqualTo(folder.isOpened());

        // restore
        FolderRepository.getInstance().deleteFolder(folder.getId());

    }

    private Folder getFolderFromDatabase(long folderId) {
        return Observable.from(FolderRepository.getInstance(teamId).getFolders())
                .takeFirst(folder2 -> folder2.getId() == folderId)
                .toBlocking().firstOrDefault(null);
    }

    @NonNull
    private Folder getFolder() {
        Folder folder = new Folder();
        folder.setId(1);
        folder.setSeq(1);
        folder.setName("hello");
        folder.setOpened(true);
        return folder;
    }

    @Test
    public void testUpdateFolderName() throws Exception {
        Folder folder = getFolder();
        FolderRepository.getInstance().addFolder(folder);

        String name = "hello 2";
        boolean success = FolderRepository.getInstance().updateFolderName(folder.getId(), name);

        assertThat(success).isTrue();

        Folder folder1 = getFolderFromDatabase(folder.getId());

        assertThat(folder1.getName()).isEqualToIgnoringCase(name);

    }

    @Test
    public void testUpdateFolderSeq() throws Exception {
        Folder folder = getFolder();
        FolderRepository.getInstance().addFolder(folder);

        int newSeq = 2;
        boolean success = FolderRepository.getInstance().updateFolderSeq(teamId, folder.getId(), newSeq);
        assertThat(success).isTrue();

        int seq = getFolderFromDatabase(folder.getId()).getSeq();

        assertThat(seq).isEqualTo(newSeq);

    }

    @Test
    public void testDeleteFolder() throws Exception {
        FolderRepository.getInstance().addFolder(getFolder());

        boolean success = FolderRepository.getInstance().deleteFolder(getFolder().getId());
        assertThat(success).isTrue();

    }

    @Test
    public void testAddTopic() throws Exception {
        FolderRepository.getInstance().addFolder(getFolder());
        FolderRepository.getInstance().addTopic(getFolder().getId(), topicList.get(0).getId());

        Folder folder = getFolderFromDatabase(getFolder().getId());
        boolean contains = false;

        for (Long roomId : folder.getRooms()) {
            if (roomId == 1L) {
                contains = true;
            }
        }
        assertThat(contains).isTrue();

    }

    @Test
    public void testRemoveTopic() throws Exception {
        FolderRepository.getInstance().addFolder(getFolder());
        long topicId1 = topicList.get(0).getId();
        long topicId2 = topicList.get(1).getId();
        FolderRepository.getInstance().addTopic(getFolder().getId(), topicId1);
        FolderRepository.getInstance().addTopic(getFolder().getId(), topicId2);

        FolderRepository.getInstance().removeTopic(getFolder().getId(), topicId2);

        List<Long> rooms = getFolderFromDatabase(getFolder().getId()).getRooms();
        boolean containValue = false;
        boolean notContainValue = false;
        for (Long roomId : rooms) {
            if (roomId == topicId1) {
                containValue = true;
            }

            if (roomId == topicId2) {
                notContainValue = true;
            }
        }
        assertThat(containValue).isTrue();
        assertThat(notContainValue).isFalse();
    }

    @Test
    public void testGetFolderExpands() throws Exception {

        FolderRepository.getInstance().upsertFolderExpands(getFolder().getId(), true);
        List<FolderExpand> folderExpands = FolderRepository.getInstance().getFolderExpands();
        assertThat(folderExpands).isNotNull();
        assertThat(folderExpands.size()).isGreaterThanOrEqualTo(1);

    }

    @Test
    public void testUpsertFolderExpands() throws Exception {
        FolderRepository.getInstance().upsertFolderExpands(getFolder().getId(), true);
        FolderExpand folderExpand = Observable.from(FolderRepository.getInstance().getFolderExpands())
                .takeFirst(expand -> expand.getFolderId() == getFolder().getId())
                .toBlocking().firstOrDefault(null);

        assertThat(folderExpand.isExpand()).isTrue();
    }

    @Test
    public void testRemoveTopicOfTeam() throws Exception {
        Folder folder = getFolder();
        long topicId1 = topicList.get(0).getId();
        long topicId2 = topicList.get(1).getId();
        folder.getRooms().add(topicId1);
        folder.getRooms().add(topicId2);
        FolderRepository.getInstance().addFolder(folder);
        FolderRepository.getInstance().removeTopicOfTeam(teamId, Arrays.asList(topicId2));

        List<Long> rooms = getFolderFromDatabase(getFolder().getId()).getRooms();
        boolean containValue = false;
        boolean notContainValue = false;
        for (Long roomId : rooms) {
            if (roomId == topicId1) {
                containValue = true;
            }

            if (roomId == topicId2) {
                notContainValue = true;
            }
        }
        assertThat(containValue).isTrue();
        assertThat(notContainValue).isFalse();
    }
}