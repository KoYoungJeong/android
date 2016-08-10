package com.tosslab.jandi.app.utils;

import android.support.test.runner.AndroidJUnit4;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;

import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;


@org.junit.runner.RunWith(AndroidJUnit4.class)
public class DateTransformatorTest {

    @Test
    public void testGetRemainingDays() throws Exception {
        {
            long time = System.currentTimeMillis() + (1000 * 60 * 60 * 24 * 2);
            String remainingDays = DateTransformator.getRemainingDays(new Date(time));
            assertThat(remainingDays).contains(JandiApplication.getContext().getString(R.string.jandi_date_days));
        }

        {
            long time = System.currentTimeMillis() + (1000 * 60 * 60 * 2);
            String remainingDays = DateTransformator.getRemainingDays(new Date(time));
            assertThat(remainingDays).contains(JandiApplication.getContext().getString(R.string.jandi_date_hours));
        }

        {
            long time = System.currentTimeMillis() + (1000 * 60 * 2);
            String remainingDays = DateTransformator.getRemainingDays(new Date(time));
            assertThat(remainingDays).contains(JandiApplication.getContext().getString(R.string.jandi_date_minutes));
        }

    }
}