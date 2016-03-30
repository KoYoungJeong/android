package com.tosslab.jandi.app.ui.commonviewmodels.mention;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Editable;
import android.text.Spanned;
import android.widget.AutoCompleteTextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.views.spannable.MentionMessageSpannable;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import setup.BaseInitUtil;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

/**
 * Created by jsuch2362 on 2015. 11. 5..
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class MentionControlViewModelTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<>(BaseAppCompatActivity.class);
    private MentionControlViewModel mentionControlViewModel;
    private AutoCompleteTextView textView;

    @BeforeClass
    public static void setUpClass() throws Exception {
        BaseInitUtil.initData();
    }

    @Before
    public void setUp() throws Exception {

    }

    private void init() {
        textView = new AutoCompleteTextView(JandiApplication.getContext());
        long t_defaultChannelId = LeftSideMenuRepository.getRepository().getCurrentLeftSideMenu().team.t_defaultChannelId;
        mentionControlViewModel = MentionControlViewModel.newInstance(rule.getActivity(), textView, Arrays.asList(t_defaultChannelId), MentionControlViewModel.MENTION_TYPE_MESSAGE);
    }

    @Test
    public void testSetUpMention() throws Exception {

        rule.getActivity().runOnUiThread(this::init);

        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mentionControlViewModel.setUpMention("hahahah");
                Editable text = textView.getText();
                assertTrue(text instanceof Spanned);

                for (int idx = 0; idx < text.length() - 1; idx++) {

                    MentionMessageSpannable[] spans = text.getSpans(idx, idx, MentionMessageSpannable.class);
                    assertThat(spans, is(notNullValue()));
                    assertThat(spans.length, is(0));
                }

            }
        });

        rule.getActivity().runOnUiThread(() -> {
            FormattedEntity user = EntityManager.getInstance().getFormattedUsersWithoutMe().get(0);
            StringBuffer buffer = new StringBuffer();
            buffer.append("@").append(user.getName()).append("\u2063").append(user.getId()).append("\u2063");
            mentionControlViewModel.setUpMention(buffer.toString());
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

    @Test
    public void testMentionedMemberHighlightInEditText() throws Exception {

        rule.getActivity().runOnUiThread(this::init);

        rule.getActivity().runOnUiThread(() -> {
            FormattedEntity user = EntityManager.getInstance().getFormattedUsersWithoutMe().get(0);

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