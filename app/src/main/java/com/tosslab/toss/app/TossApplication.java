package com.tosslab.toss.app;

import android.app.Application;
import android.util.Log;

import com.tosslab.toss.app.utils.ConfigureLog4J;

import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class TossApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        try {
            ConfigureLog4J.configure(getApplicationContext());

            Logger logger = Logger.getLogger(TossApplication.class);
            logger.info("initialize log file");
        } catch (Exception e) {
            Log.e("android-log4j", e.getMessage());
        }
    }
}
