package com.tosslab.jandi.app.ui.commonviewmodels.mention;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.LargeTest;
import android.text.Editable;
import android.text.Spanned;
import android.widget.AutoCompleteTextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.LeftSideMenuRepository;
import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

import setup.BaseInitUtil;

import static junit.framework.Assert.assertTrue;

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

    @Before
    public void setUp() throws Exception {

        BaseInitUtil.initData();

    }

    private void init() {
        textView = new AutoCompleteTextView(JandiApplication.getContext());
        int t_defaultChannelId = LeftSideMenuRepository.getRepository().getCurrentLeftSideMenu().team.t_defaultChannelId;
        mentionControlViewModel = MentionControlViewModel.newInstance(rule.getActivity(), textView, Arrays.asList(t_defaultChannelId), MentionControlViewModel.MENTION_TYPE_MESSAGE);
    }

    @Test
    public void testSetUpMention() throws Exception {

        rule.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                init();

                mentionControlViewModel.setUpMention("hahahah");
                Editable text = textView.getText();
                assertTrue(text instanceof Spanned);

            }
        });
//
//        FormattedEntity me = EntityManager.getInstance().getMe();
//
//        StringBuffer message = new StringBuffer();
//        message.append("@").append(me.getName()).append("\u2063").append(me.getId()).append(⁣"\u2063");
//
//        mentionControlViewModel.setUpMention(message.toString());


    }
}