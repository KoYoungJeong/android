package com.tosslab.jandi.app.ui.invites.email.presenter;

import com.tosslab.jandi.app.ui.invites.email.model.InviteByEmailModel;
import com.tosslab.jandi.app.ui.invites.email.model.InvitedEmailDataModel;
import com.tosslab.jandi.app.ui.invites.email.model.bean.EmailVO;
import com.tosslab.jandi.app.ui.invites.email.view.InviteByEmailView;

import org.junit.Before;
import org.junit.Test;

import static com.jayway.awaitility.Awaitility.await;
import static org.junit.Assert.*;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by tonyjs on 16. 4. 5..
 */
public class InviteByEmailPresenterImplTest {

    private InviteByEmailPresenterImpl inviteByEmailPresenter;
    private InviteByEmailModel inviteByEmailModel;
    private InviteByEmailView view;
    private InvitedEmailDataModel invitedEmailDataModel;

    @Before
    public void setUp() throws Exception {
        view = mock(InviteByEmailView.class);
        invitedEmailDataModel = mock(InvitedEmailDataModel.class);
        inviteByEmailModel = new InviteByEmailModel();
        inviteByEmailPresenter =
                new InviteByEmailPresenterImpl(inviteByEmailModel, view, invitedEmailDataModel);

    }

    @Test
    public void testOnInviteListAddClickAlreadyInDataModel() throws Exception {
        // Given
        when(invitedEmailDataModel.findEmailVoByEmail(anyString())).thenReturn(EmailVO.create(""));

        // When
        inviteByEmailPresenter.onInviteListAddClick("");

        // Then
        verify(view).showInviteSuccessToast();
        verify(view).clearEmailInput();
    }

    @Test
    public void testOnInviteListAddClick() throws Exception {
        // Given
        when(invitedEmailDataModel.findEmailVoByEmail(anyString())).thenReturn(EmailVO.create(""));

        // When
        inviteByEmailPresenter.onInviteListAddClick("");

        // Then
        verify(view).showInviteSuccessToast();
        verify(view).clearEmailInput();
    }

    @Test
    public void testInvite() throws Exception {
        // Given
        when(invitedEmailDataModel.findEmailVoByEmail(anyString())).thenReturn(null);

        InviteByEmailModel spy = spy(inviteByEmailModel);
        when(spy.isInvitedEmail(anyString())).thenReturn(false);

        // When
        inviteByEmailPresenter.onInviteListAddClick("pkjun09@naver.com");

        // Then
        verify(invitedEmailDataModel).add(anyObject());

        verify(view).notifyItemInserted(anyInt());
        verify(view).moveToPosition(0);

        boolean[] finish = {false};

        doAnswer(invocationOnMock -> {
            finish[0] = true;
            return invocationOnMock;
        }).when(view).showSendEmailSuccessView();

        await().until(() -> finish[0]);
    }
}