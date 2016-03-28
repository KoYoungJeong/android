package com.tosslab.jandi.app.ui.passcode.fingerprint.model;

import android.support.test.runner.AndroidJUnit4;
import android.support.v4.hardware.fingerprint.FingerprintManagerCompat;

import com.tosslab.jandi.app.JandiApplication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.KeyStore;

import javax.crypto.SecretKey;

import rx.Observable;
import rx.observers.TestSubscriber;

import static org.junit.Assert.*;

/**
 * Created by tonyjs on 16. 3. 28..
 */
@RunWith(AndroidJUnit4.class)
public class FingerprintAuthModelTest {

    private FingerprintAuthModel model;

    @Before
    public void setup() throws Exception {
        model = new FingerprintAuthModel();
    }

    @Test
    public void testGetKeyStoreObservable() throws Exception {
        Observable<KeyStore> keyStoreObservable = model.getKeyStoreObservable();
        TestSubscriber<KeyStore> testSubscriber = new TestSubscriber<>();
        keyStoreObservable.subscribe(testSubscriber);

        testSubscriber.assertNoErrors();
        testSubscriber.assertCompleted();

        KeyStore keyStore = testSubscriber.getOnNextEvents().get(0);
        assertNotNull(keyStore);
    }

    @Test
    public void testGetSecretKeyObservable() throws Exception {
        Observable<SecretKey> secretKeyObservable =
                model.getKeyStoreObservable()
                        .concatMap(model::getSecretKeyObservable);
        TestSubscriber<SecretKey> testSubscriber = new TestSubscriber<>();
        secretKeyObservable.subscribe(testSubscriber);

        FingerprintManagerCompat fingerprintManager =
                FingerprintManagerCompat.from(JandiApplication.getContext());

        if (fingerprintManager.isHardwareDetected() && fingerprintManager.hasEnrolledFingerprints()) {
            testSubscriber.assertNoErrors();
            testSubscriber.assertCompleted();

            SecretKey secretKey = testSubscriber.getOnNextEvents().get(0);
            assertNotNull(secretKey);
        } else {
            testSubscriber.assertError(Exception.class);
            testSubscriber.assertNotCompleted();
        }
    }

}