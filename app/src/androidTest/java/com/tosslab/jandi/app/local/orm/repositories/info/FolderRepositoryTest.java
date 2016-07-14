package com.tosslab.jandi.app.local.orm.repositories.info;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.local.orm.domain.FolderExpand;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.start.Folder;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import rx.Observable;
import setup.BaseInitUtil;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class FolderRepositoryTest {

    private static InitialInfo initializeInfo;
    private static long teamId;

    @org.junit.BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
        teamId = TeamInfoLoader.getInstance().getTeamId();
        initializeInfo = new StartApi(RetrofitBuilder.getInstance()).getInitializeInfo(teamId);
    }

    @Before
    public void setUp() throws Exception {
        FolderRepository.getInstance().deleteFolder(getFolder().getId());
        InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        TeamInfoLoader.getInstance().refresh();

    }

    @Test
    public void testGetFolders() throws Exception {
        InitialInfoRepository.getInstance().upsertInitialInfo(initializeInfo);
        TeamInfoLoader.getInstance().refresh();
    }


    @Test
    public void testAddFolder() throws Exception {
        Folder folder = getFolder();
        boolean success = FolderRepository.getInstance().addFolder(teamId, folder);
        assertThat(success).isTrue();

        Folder folder1 = getFolderFromDatabase(folder.getId());

        assertThat(folder1.getId()).isEqualTo(folder.getId());
        assertThat(folder1.getSeq()).isEqualTo(folder.getSeq());
        assertThat(folder1.getName()).isEqualTo(folder.getName());
        assertThat(folder1.isOpened()).isEqualTo(folder.isOpened());
        assertThat(folder1.getRooms()).hasSize(folder.getRooms().size());

        // restore
        FolderRepository.getInstance().deleteFolder(folder.getId());

    }

    private Folder getFolderFromDatabase(long folderId) {
        return Observable.from(FolderRepository.getInstance().getFolders(teamId))
                .takeFirst(folder2 -> folder2.getId() == folderId)
                .toBlocking().firstOrDefault(null);
    }

    @NonNull
    private Folder getFolder() {
        Folder folder = new Folder();
        folder.setId(1l);
        folder.setSeq(1);
        folder.setName("hello");
        folder.setOpened(true);
        folder.setRooms(new ArrayList<>());
        return folder;
    }

    @Test
    public void testUpdateFolderName() throws Exception {
        Folder folder = getFolder();
        FolderRepository.getInstance().addFolder(teamId, folder);

        String name = "hello 2";
        boolean success = FolderRepository.getInstance().updateFolderName(folder.getId(), name);

        assertThat(success).isTrue();

        Folder folder1 = getFolderFromDatabase(folder.getId());

        assertThat(folder1.getName()).isEqualToIgnoringCase(name);

    }

    @Test
    public void testUpdateFolderSeq() throws Exception {
        Folder folder = getFolder();
        FolderRepository.getInstance().addFolder(teamId, folder);

        int newSeq = 2;
        boolean success = FolderRepository.getInstance().updateFolderSeq(teamId, folder.getId(), newSeq);
        assertThat(success).isTrue();

        int seq = getFolderFromDatabase(folder.getId()).getSeq();

        assertThat(seq).isEqualTo(newSeq);

    }

    @Test
    public void testDeleteFolder() throws Exception {
        FolderRepository.getInstance().addFolder(teamId, getFolder());

        boolean success = FolderRepository.getInstance().deleteFolder(getFolder().getId());
        assertThat(success).isTrue();

    }

    @Test
    public void testAddTopic() throws Exception {
        FolderRepository.getInstance().addFolder(teamId, getFolder());
        FolderRepository.getInstance().addTopic(getFolder().getId(), 1);

        Folder folder = getFolderFromDatabase(getFolder().getId());
        assertThat(folder.getRooms()).contains(1L);

    }

    @Test
    public void testRemoveTopic() throws Exception {
        FolderRepository.getInstance().addFolder(teamId, getFolder());
        FolderRepository.getInstance().addTopic(getFolder().getId(), 1);
        FolderRepository.getInstance().addTopic(getFolder().getId(), 2);

        FolderRepository.getInstance().removeTopic(getFolder().getId(), 2);

        Collection<Long> rooms = getFolderFromDatabase(getFolder().getId()).getRooms();
        assertThat(rooms).contains(1L);
        assertThat(rooms).doesNotContain(2L);
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
        folder.setRooms(Arrays.asList(1L, 2L));
        FolderRepository.getInstance().addFolder(teamId, folder);
        FolderRepository.getInstance().removeTopicOfTeam(teamId, Arrays.asList(2L));

        Folder folder1 = getFolderFromDatabase(folder.getId());
        assertThat(folder1.getRooms()).contains(1L);
        assertThat(folder1.getRooms()).doesNotContain(2L);
    }
}