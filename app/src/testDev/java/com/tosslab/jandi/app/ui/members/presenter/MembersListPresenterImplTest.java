package com.tosslab.jandi.app.ui.members.presenter;

import com.jayway.awaitility.Awaitility;
import com.tosslab.jandi.app.ui.entities.chats.domain.ChatChooseItem;

import org.hamcrest.CoreMatchers;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.IsEqual;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.JandiRobolectricGradleTestRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.subjects.PublishSubject;

/**
 * Created by Steve SeongUg Jung on 15. 7. 31..
 */
@RunWith(JandiRobolectricGradleTestRunner.class)
public class MembersListPresenterImplTest {

    @Test
    public void testSearchMember() throws Exception {

        List<ChatChooseItem> chatChooseItems = getMockItems();

        List<ChatChooseItem> testList = new ArrayList<>();
        PublishSubject<String> publishSubject = PublishSubject.create();
        publishSubject
                .throttleWithTimeout(300, TimeUnit.MILLISECONDS)
                .flatMap(s -> Observable.from(chatChooseItems)
                                .filter(chatChooseItem -> chatChooseItem.getName().toLowerCase().contains(s.toLowerCase()))
                                .toSortedList((lhs, rhs) -> lhs.getName().toLowerCase().compareTo(rhs.getName().toLowerCase()))
                )
                .subscribe(testList::addAll);

        publishSubject.onNext("he");

        Awaitility.await().until(() -> testList.size() > 0);

        MatcherAssert.assertThat(testList.size(), CoreMatchers.is(IsEqual.equalTo(2)));
    }

    public List<ChatChooseItem> getMockItems() {
        List<ChatChooseItem> chatChooseItems = new ArrayList<>();

        ChatChooseItem mock1 = Mockito.mock(ChatChooseItem.class);
        Mockito.when(mock1.getName()).thenReturn("hahah1");
        ChatChooseItem mock2 = Mockito.mock(ChatChooseItem.class);
        Mockito.when(mock2.getName()).thenReturn("heheh1");
        ChatChooseItem mock3 = Mockito.mock(ChatChooseItem.class);
        Mockito.when(mock3.getName()).thenReturn("hahah2");
        ChatChooseItem mock4 = Mockito.mock(ChatChooseItem.class);
        Mockito.when(mock4.getName()).thenReturn("heheh2");

        chatChooseItems.addAll(Arrays.asList(mock1, mock2, mock3, mock4));
        return chatChooseItems;
    }
}