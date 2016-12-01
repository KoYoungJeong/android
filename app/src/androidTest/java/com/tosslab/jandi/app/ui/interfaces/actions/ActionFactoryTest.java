package com.tosslab.jandi.app.ui.interfaces.actions;

import android.net.Uri;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.ui.base.BaseAppCompatActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(AndroidJUnit4.class)
public class ActionFactoryTest {

    @Rule
    public ActivityTestRule<BaseAppCompatActivity> rule = new ActivityTestRule<>(BaseAppCompatActivity.class);

    @Test
    public void testGetAction() throws Throwable {
        {
            Uri parse = null;
            Action action = ActionFactory.getAction(rule.getActivity(), parse);
            assertThat(action, is(notNullValue()));
            assertThat(action.getClass(), is(equalTo(UnknownAction.class)));
        }

        {
            Uri parse = Uri.parse("tosslabjandi://xxx");
            Action action = ActionFactory.getAction(rule.getActivity(), parse);
            assertThat(action, is(notNullValue()));
            assertThat(action.getClass(), is(equalTo(UnknownAction.class)));
        }
        {
            Uri parse = Uri.parse("tosslabjandi://");
            Action action = ActionFactory.getAction(rule.getActivity(), parse);
            assertThat(action, is(notNullValue()));
            assertThat(action.getClass(), is(equalTo(UnknownAction.class)));
        }

        {
            rule.runOnUiThread(() -> {
                Uri parse = Uri.parse("tosslabjandi://open");
                Action action = ActionFactory.getAction(rule.getActivity(), parse);
                assertThat(action, is(notNullValue()));
                assertThat(action.getClass(), is(equalTo(OpenAction.class)));
            });
        }
    }
}