package com.tosslab.jandi.app.ui;

import android.app.Activity;

import org.jsoup.Jsoup;

/**
 * Created by justinygchoi on 2014. 9. 10..
 */
public class BaseActivity extends Activity {
//    private final Logger log = Logger.getLogger(BaseActivity.class);
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//
//        super.onCreate(savedInstanceState);
//
//        MarketService ms = new MarketService(this);
//        ms.level(MarketService.REVISION).checkVersion();
//
//    }

    protected boolean isLatestVersion() {
        try {
            String packageName = getPackageName();
            String curVersion = getPackageManager().getPackageInfo(packageName, 0).versionName;
            String newVersion = curVersion;
            newVersion = Jsoup.connect("https://play.google.com/store/apps/details?id=" + packageName + "&hl=en")
                    .timeout(30000)
                    .userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6")
                    .referrer("http://www.google.com")
                    .get()
                    .select("div[itemprop=softwareVersion]")
                    .first()
                    .ownText();
            return (value(curVersion) >= value(newVersion)) ? true : false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private long value(String string) {
        string = string.trim();
        if( string.contains( "." )){
            final int index = string.lastIndexOf( "." );
            return value( string.substring( 0, index ))* 100 + value( string.substring( index + 1 ));
        }
        else {
            return Long.valueOf( string );
        }
    }
}
