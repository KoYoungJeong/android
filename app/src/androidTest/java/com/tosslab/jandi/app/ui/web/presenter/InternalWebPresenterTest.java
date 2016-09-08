package com.tosslab.jandi.app.ui.web.presenter;

import android.app.Activity;
import android.support.test.rule.ActivityTestRule;

import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.ui.web.InternalWebActivity_;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mockito;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Created by tee on 16. 1. 13..
 */
public class InternalWebPresenterTest {

    @Rule
    public ActivityTestRule<InternalWebActivity_> rule = new ActivityTestRule<>(InternalWebActivity_.class, false, false);

    public InternalWebPresenter internalWebPresenter;
    public InternalWebPresenter.View mockView;

    @Before
    public void init() throws Throwable {
        internalWebPresenter = new InternalWebPresenter();
        mockView = Mockito.mock(InternalWebPresenter.View.class);
        internalWebPresenter.setView(mockView);
    }

    @Test
    public void testGetAvailableUrl() {
        //When
        String url = internalWebPresenter.getAvailableUrl("abc.com");
        //Then
        Assert.assertTrue(url.contains("http://"));
    }

    @Ignore
    @Test
    public void testSendMessage() throws RetrofitException {
        // 본 테스트는 MessageManipulator에 대한 테스트가 완전하다고 가정하에 작성

        // Given
        InternalWebPresenter presenter = spy(internalWebPresenter);
        ShareSelectRoomEvent shareSelectRoomEvent = Mockito.mock(ShareSelectRoomEvent.class);
        Activity activity = rule.getActivity();
        String title = "title";
        String url = "http://url.com";

        {
            // given 성공 케이스
            Mockito.doNothing().when(presenter).sendMessageToRoom(anyInt(), anyInt(), anyString(), anyObject());
            String message = presenter.createMessage(title, url);

            // When
            presenter.sendMessage(activity, title, url, shareSelectRoomEvent);

            //then
            verify(mockView).showProgressWheel();
            verify(presenter).sendMessageToRoom(anyInt(), anyInt(), eq(message), anyObject());
            verify(mockView).showSuccessToast(anyObject(), anyObject());
            verify(mockView).dismissProgressWheel();
        }

        {
            // given 실패 케이스
            Mockito.doThrow(RetrofitException.class).when(presenter).sendMessageToRoom(anyInt(), anyInt(), anyString(), anyObject());

            // When
            presenter.sendMessage(activity, title, url, shareSelectRoomEvent);

            // then
            verify(mockView, times(2)).showProgressWheel();
            verify(mockView).showErrorToast(anyObject(), anyObject());
            verify(mockView, times(2)).dismissProgressWheel();
        }
    }

}