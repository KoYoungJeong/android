package com.tosslab.jandi.app.ui.invites.email.model;

import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;

import org.junit.Before;
import org.junit.Test;

import rx.Observable;
import rx.observers.TestSubscriber;
import setup.BaseInitUtil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by tonyjs on 16. 4. 5..
 */
public class InviteByEmailModelTest {

    private InviteByEmailModel model;

    @Before
    public void setUp() throws Exception {
        BaseInitUtil.initData();

        model = new InviteByEmailModel(() -> new TeamApi(RetrofitBuilder.getInstance()));
    }

    @Test
    public void testIsValidEmailFormat() throws Exception {
        assertFalse(model.isValidEmailFormat("1234"));
        assertFalse(model.isValidEmailFormat("1234@"));
        assertFalse(model.isValidEmailFormat("1234@com"));
        assertFalse(model.isValidEmailFormat("1234@c.c.c.c."));
    }

    @Test
    public void testGetInviteMemberObservable() throws Exception {
        String expectEmail = "pkjun09@naver.com";
        Observable<String> inviteMemberObservable = model.getInviteMemberObservable(expectEmail);

        TestSubscriber<String> testSubscriber = new TestSubscriber<>();
        inviteMemberObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        String email = testSubscriber.getOnNextEvents().get(0);

        assertEquals(expectEmail, email);
    }

    @Test
    public void testIsInvitedEmail() throws Exception {
        assertTrue(model.isInvitedEmail(BaseInitUtil.TEST1_EMAIL));
        assertTrue(model.isInvitedEmail(BaseInitUtil.TEST2_EMAIL));
        assertTrue(model.isInvitedEmail(BaseInitUtil.TEST3_EMAIL));

        assertFalse(model.isInvitedEmail("pkojun09@gmail.com"));
    }

    @Test
    public void testIsInactivedUser() throws Exception {

        // When
        FormattedEntity user = Observable.from(EntityManager.getInstance().getFormattedUsersWithoutMe())
                .filter(entity -> entity.getUserEmail().equals(BaseInitUtil.TEST2_EMAIL))
                .toBlocking()
                .first();
        user.getUser().status = "inactive";

        // Then
        assertTrue(model.isInactivedUser(user.getUserEmail()));

    }
}