package com.tosslab.jandi.app.ui.commonviewmodels.mention;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.text.Editable;
import android.text.Spanned;
import android.widget.AutoCompleteTextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.views.spannable.MentionMessageSpannable;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import rx.Observable;
import setup.BaseInitUtil;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by jsuch2362 on 2015. 11. 5..
 */
@RunWith(AndroidJUnit4.class)
public class MentionControlViewModelTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<>(BaseAppCompatActivity.class);
    private MentionControlViewModel mentionControlViewModel;
    private AutoCompleteTextView textView;

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
        rule.runOnUiThread(this::init);
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    }

    private void init() {
        textView = new AutoCompleteTextView(JandiApplication.getContext());
        long t_defaultChannelId = TeamInfoLoader.getInstance().getDefaultTopicId();
        mentionControlViewModel = MentionControlViewModel.newInstance(rule.getActivity(), textView, Arrays.asList(t_defaultChannelId), MentionControlViewModel.MENTION_TYPE_MESSAGE);
    }

    @Test
    public void testSetUpMention() throws Throwable {

        {
            rule.runOnUiThread(() -> mentionControlViewModel.setUpMention("hahahah"));

            Editable text = textView.getText();
            assertTrue(text instanceof Spanned);
            MentionMessageSpannable[] spans = text.getSpans(0, text.length(), MentionMessageSpannable.class);
            assertThat(spans, is(notNullValue()));
            assertThat(spans.length, is(0));
        }

        {
            rule.runOnUiThread(() -> {
                mentionControlViewModel.refreshMembers(Arrays.asList(TeamInfoLoader.getInstance().getDefaultTopicId()));
                Observable.from(TeamInfoLoader.getInstance().getUserList())
                        .filter(user1 -> !user1.isBot())
                        .takeFirst(user1 -> user1.getId() != TeamInfoLoader.getInstance().getMyId())
                        .map(User::getId)
                        .subscribe(id -> {
                            StringBuffer buffer = new StringBuffer();
                            buffer.append("@").append("hahahah").append("\u2063").append(id).append("\u2063");
                            mentionControlViewModel.setUpMention(buffer.toString());
                        });

            });


            InstrumentationRegistry.getInstrumentation().waitForIdleSync();

            Editable text = textView.getText();
            assertTrue(text instanceof Spanned);

            MentionMessageSpannable[] spans = text.getSpans(0, text.length(), MentionMessageSpannable.class);
            assertThat(spans, is(notNullValue()));
            assertThat(spans.length, is(greaterThanOrEqualTo(1)));
        }

    }

    @Test
    public void testMentionedMemberHighlightInEditText() throws Exception {

        rule.getActivity().runOnUiThread(() -> {
            User user = Observable.from(TeamInfoLoader.getInstance().getUserList())
                    .takeFirst(user1 -> user1.getId() != TeamInfoLoader.getInstance().getMyId())
                    .toBlocking().first();

            SearchedItemVO searchedItemVO = new SearchedItemVO();
            searchedItemVO.setId(user.getId());
            searchedItemVO.setName(user.getName());
            searchedItemVO.setType("member");

            textView.setText("@");
            textView.setSelection(1);
            mentionControlViewModel.currentSearchKeywordString = "";

            mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);

            Editable text = textView.getText();
            assertTrue(text instanceof Spanned);

            for (int idx = 1; idx < text.length() - 1; idx++) {
                MentionMessageSpannable[] spans = text.getSpans(idx, idx, MentionMessageSpannable.class);
                assertThat(spans, is(notNullValue()));
                System.out.println("Span Length : " + spans.length);
                assertThat(spans.length, is(1));
            }

        });

    }
}