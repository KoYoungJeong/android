package com.tosslab.jandi.app.local.orm.repositories;

import com.tosslab.jandi.app.network.manager.RequestApiManager;
import com.tosslab.jandi.app.network.models.ResFolder;
import com.tosslab.jandi.app.network.models.ResFolderItem;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.BaseInitUtil;
import org.robolectric.JandiRobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;

import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JandiRobolectricGradleTestRunner.class)
public class TopicFolderRepositoryTest {

    private TopicFolderRepository repository;
    private List<ResFolder> originFolders;
    private List<ResFolderItem> originFolderItems;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData(RuntimeEnvironment.application);

        repository = TopicFolderRepository.getRepository();

        int teamId = AccountRepository.getRepository().getSelectedTeamId();
        originFolders = RequestApiManager.getInstance().getFoldersByTeamApi(teamId);
        originFolderItems = RequestApiManager.getInstance().getFolderItemsByTeamApi(teamId);
    }

    @After
    public void tearDown() throws Exception {
        BaseInitUtil.releaseDatabase();
    }

    @Test
    public void testUpsertFolders() throws Exception {
        boolean success = repository.upsertFolders(originFolders);
        assertTrue(success);
    }

    @Test
    public void testUpsertFolderItems() throws Exception {
        boolean success = repository.upsertFolderItems(originFolderItems);
        assertTrue(success);
    }

    @Test
    public void testGetFolders() throws Exception {
        repository.upsertFolders(originFolders);
        List<ResFolder> savedFolders = repository.getFolders();
        assertThat(originFolders.size(), is(equalTo(savedFolders.size())));
    }

    @Test
    public void testGetFolderItems() throws Exception {
        repository.upsertFolderItems(originFolderItems);
        List<ResFolderItem> savedFolderItems = repository.getFolderItems();
        assertThat(originFolderItems.size(), is(equalTo(savedFolderItems.size())));
    }

    @Test
    public void testDeleteFolder() throws Exception {
        repository.upsertFolders(originFolders);
        repository.removeFolder(originFolders.get(0).id);
        List<ResFolder> savedFolderList = repository.getFolders();
        assertThat(originFolders.size() - 1, is(equalTo(savedFolderList.size())));
    }

    @Test
    public void testDeleteFolderItem() throws Exception {
        repository.upsertFolderItems(originFolderItems);
        repository.removeFolderItem(originFolderItems.get(0).roomId);
        List<ResFolderItem> savedFolderItemList = repository.getFolderItems();
        assertThat(originFolderItems.size() - 1, is(equalTo(savedFolderItemList.size())));
    }

}