package com.tosslab.jandi.app.ui.login.adapter;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.login.login.IntroLoginFragment_;
import com.tosslab.jandi.app.ui.login.tutorial.IntroTutorialFragment_;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * Created by jsuch2362 on 15. 11. 10..
 */
@RunWith(AndroidJUnit4.class)
public class IntroMainPagerAdapterTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<BaseAppCompatActivity>(BaseAppCompatActivity.class);
    private IntroMainPagerAdapter adapter;

    @Before
    public void setUp() throws Exception {
        FragmentManager fragmentManager = rule.getActivity().getFragmentManager();
        adapter = new IntroMainPagerAdapter(fragmentManager);

    }

    @Test
    public void testGetItem() throws Exception {
        // when
        Fragment item = adapter.getItem(0);
        // then
        assertThat(item.getClass(), is(equalTo(IntroTutorialFragment_.class)));

        // when
        item = adapter.getItem(3);
        // then
        assertThat(item.getClass(), is(equalTo(IntroLoginFragment_.class)));
    }

    @Test
    public void testGetCount() throws Exception {
        int count = adapter.getCount();
        assertThat(count, is(equalTo(adapter.NUM_OF_PAGES_WITH_TUTORIAL)));
    }
}