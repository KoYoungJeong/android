package com.tosslab.jandi.app.ui.interfaces.actions;

import android.support.test.espresso.intent.Intents;
import android.support.test.espresso.intent.matcher.IntentMatchers;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;
import com.tosslab.jandi.app.ui.intro.IntroActivity_;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@Ignore
@RunWith(AndroidJUnit4.class)
public class UnknownActionTest {

    @Rule
    public IntentsTestRule<BaseAppCompatActivity> rule = new IntentsTestRule<>(BaseAppCompatActivity.class);
    private Action action;

    @Before
    public void setUp() throws Exception {
        action = UnknownAction.create(rule.getActivity());

    }

    @Test
    public void testExecute() throws Throwable {

        rule.runOnUiThread(() -> action.execute(null));

        assertThat(rule.getActivity().isFinishing(), is(true));
        Intents.intending(IntentMatchers.hasComponent(IntroActivity_.class.getName()));
    }
}