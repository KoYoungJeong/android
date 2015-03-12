package org.robolectric;

import org.junit.runners.model.InitializationError;
import org.robolectric.annotation.Config;
import org.robolectric.res.Fs;

/**
 * Created by Steve SeongUg Jung on 14. 12. 01.
 */
public class RobolectricGradleTestRunner extends RobolectricTestRunner {
    public RobolectricGradleTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {

        final String _manifestProperty = System.getProperty("android.manifest");
        final String _resProperty = System.getProperty("android.resources");
        final String _assetsProperty = System.getProperty("android.assets");
        final String _androidPackage = System.getProperty("android.package");

        String buildPath = "./app/build/intermediates";
        String flavorPath = "/dev/debug";
        final String manifestProperty = _manifestProperty == null ? "app/src/main/AndroidManifest.xml" : _manifestProperty;
        final String resProperty = _resProperty == null ? buildPath + "/res" + flavorPath : _resProperty;
        final String assetsProperty = _assetsProperty == null ? buildPath + "/assets" + flavorPath : _assetsProperty;
//        final String packageProperty = _androidPackage == null ? "com.tosslab.jandi.app.dev" : _androidPackage; // System.getProperty("android.package");

        System.out.println(manifestProperty);
        System.out.println(resProperty);
        System.out.println(assetsProperty);
//        System.out.println(packageProperty);

        final AndroidManifestExt a = new AndroidManifestExt(
                Fs.fileFromPath(manifestProperty),
                Fs.fileFromPath(resProperty),
                Fs.fileFromPath(assetsProperty));
//        a.setPackageName(packageProperty);

        return a;
    }


}