package org.robolectric;

import android.os.Build;

import org.robolectric.res.FsFile;
import org.robolectric.res.ResourceLoader;

import java.util.Map;

/**
 * Created by Steve SeongUg Jung on 14. 12. 4..
 */
public class AndroidManifestExt extends AndroidManifest {
    private static final String R = ".R";
    private String mPackageName;
    private boolean isPackageSet;

    public AndroidManifestExt(final FsFile androidManifestFile, final FsFile resDirectory,
                              final FsFile assetsDirectory) {
        super(androidManifestFile, resDirectory, assetsDirectory);
    }

    @Override
    public String getRClassName() throws Exception {
        if (isPackageSet) {
            parseAndroidManifest();
            return mPackageName + R;
        }
        return super.getRClassName();
    }

    @Override
    public String getPackageName() {
        if (isPackageSet) {
            parseAndroidManifest();
            return mPackageName;
        } else {
            return super.getPackageName();
        }
    }

    public void setPackageName(final String packageName) {
        mPackageName = packageName;
        isPackageSet = packageName != null;
    }

    @Override
    public String getApplicationName() {
        String applicationName = ".TestJandiApplication";
        System.out.println("===============================");
        System.out.println(applicationName);
        System.out.println("===============================");
        return applicationName;
    }

    @Override
    public void initMetaData(ResourceLoader resLoader) {
        super.initMetaData(resLoader);

        Map<String, Object> applicationMetaData = getApplicationMetaData();
        for (String key : applicationMetaData.keySet()) {
            if (applicationMetaData.get(key) == null) {
                System.out.println("Key is Null : " + key);
                applicationMetaData.put(key, "VALUE_TYPE.RESOURCE");
            }
        }
    }

    @Override
    public int getTargetSdkVersion() {
        return Build.VERSION_CODES.JELLY_BEAN_MR2;
    }
}