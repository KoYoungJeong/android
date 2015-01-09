package org.robolectric;

import com.tosslab.jandi.app.BuildConfig;

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

        String buildPath = "./app/build/intermediates";
        String flavorPath = "/dev/debug";
        final String manifestProperty = _manifestProperty == null ? "app/src/main/AndroidManifest.xml" : _manifestProperty;
        final String resProperty = _resProperty == null ? buildPath + "/res" + flavorPath : _resProperty;
        final String assetsProperty = _assetsProperty == null ? buildPath + "/assets" + flavorPath : _assetsProperty;
        final String packageProperty = "com.tosslab.jandi.app"; // System.getProperty("android.package");

        final AndroidManifestExt a = new AndroidManifestExt(
                Fs.fileFromPath(manifestProperty),
                Fs.fileFromPath(resProperty),
                Fs.fileFromPath(assetsProperty));
        a.setPackageName(packageProperty);

        return a;
    }


}