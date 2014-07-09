package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.os.Environment;

import com.tosslab.jandi.app.R;

import org.apache.log4j.Level;

import java.io.File;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by justinygchoi on 2014. 6. 19..
 */
public class ConfigureLog4J {
    public static void configure(Context context) {
        LogConfigurator configurator = new LogConfigurator();

        // path
        String appName = context.getString(R.string.app_name);
        String logPath = Environment.getExternalStorageDirectory() + File.separator + appName;

        // create directory, which directory is not exists
        new File(logPath).mkdirs();

        logPath += File.separator + appName + ".log";

        configurator.setFileName(logPath);
        configurator.setFilePattern("%d - [%p::%C] - %m%n");     // log pattern
        configurator.setMaxFileSize(512 * 1024);                 // file size(byte)
        configurator.setMaxBackupSize(5);                        // number of backup file

        configurator.setRootLevel(Level.DEBUG);                  // set log level
        configurator.setUseLogCatAppender(true);                 // and use Logcat

        // set log level of a specific logger
        configurator.setLevel("org.apache", Level.ERROR);
        configurator.configure();
    }
}
