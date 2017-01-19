package com.tosslab.jandi.app.network.models;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import com.j256.ormlite.android.apptools.OpenHelperManager;
import com.j256.ormlite.dao.Dao;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.OrmDatabaseHelper;
import com.tosslab.jandi.app.local.orm.repositories.MessageRepository;
import com.tosslab.jandi.app.network.json.JsonMapper;
import com.tosslab.jandi.app.network.models.dynamicl10n.PollFinished;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class FormatParamTest {
    @Test
    public void testFormatParams() throws Exception {
        ResMessages.Link link = getLink();
        String rawLink = JsonMapper.getInstance().getObjectMapper().writeValueAsString(link);

        ResMessages.Link link1 = JsonMapper.getInstance().getObjectMapper().readValue(rawLink, ResMessages.Link.class);
        assertThat(link1.message.formatMessage).isNotNull()
                .isInstanceOf(PollFinished.class);
        assertThat(((PollFinished) link1.message.formatMessage))
                .isEqualToComparingFieldByField(getPollFinishedObject());

    }

    @Test
    public void testFormatParams_Database() throws Exception {
        ResMessages.Link link = getLink();
        String rawLink = JsonMapper.getInstance().getObjectMapper().writeValueAsString(link);

        ResMessages.Link link1 = JsonMapper.getInstance().getObjectMapper().readValue(rawLink, ResMessages.Link.class);

        MessageRepository.getRepository().upsertMessage(link1);

        OrmDatabaseHelper helper = OpenHelperManager.getHelper(JandiApplication.getContext(), OrmDatabaseHelper.class);
        Dao<ResMessages.Link, Long> dao = helper.getDao(ResMessages.Link.class);
        ResMessages.Link link2 = dao.queryForId(link.id);

        assertThat(link2.message.isFormatted).isTrue();
        assertThat(link2.message.formatKey).isEqualToIgnoringCase("poll_finished");
        assertThat(link2.message.formatMessage).isNotNull();
        assertThat(link2.message.formatMessage).isInstanceOf(PollFinished.class);
        assertThat(((PollFinished) link2.message.formatMessage).getElectedItems()).hasSize(2);
        assertThat(((PollFinished) link2.message.formatMessage).getVotedCount()).isEqualTo(3);

    }

    @NonNull
    private ResMessages.Link getLink() throws IOException {
        ResMessages.Link link = new ResMessages.Link();
        link.id = 1;
        link.message = new ResMessages.CommentMessage();
        link.message.contentType = "comment";
        link.message.id = 2;
        link.messageId = link.message.id;
        link.message.isFormatted = true;
        link.message.formatKey = "poll_finished";
        link.message.formatParams = getPollFinished();
        return link;
    }

    private Map getPollFinished() throws IOException {
        PollFinished pollFinished = getPollFinishedObject();
        String s = JsonMapper.getInstance().getObjectMapper().writeValueAsString(pollFinished);
        return JsonMapper.getInstance().getObjectMapper().readValue(s, Map.class);
    }

    @NonNull
    private PollFinished getPollFinishedObject() {
        PollFinished pollFinished = new PollFinished();
        ArrayList<PollFinished.ElectedItem> electedItems = new ArrayList<>();
        PollFinished.ElectedItem object = new PollFinished.ElectedItem();
        object.setName("haha");
        object.setSeq(0);
        object.setVotedCount(1);
        electedItems.add(object);
        object = new PollFinished.ElectedItem();
        object.setName("haha2");
        object.setSeq(1);
        object.setVotedCount(2);
        electedItems.add(object);
        pollFinished.setElectedItems(electedItems);
        pollFinished.setVotedCount(3);
        return pollFinished;
    }
}