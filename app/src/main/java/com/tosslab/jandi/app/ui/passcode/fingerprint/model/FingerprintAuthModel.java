package com.tosslab.jandi.app.ui.passcode.fingerprint.model;

import android.annotation.TargetApi;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import org.androidannotations.annotations.EBean;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import rx.Observable;
import rx.schedulers.Schedulers;

/**
 * Created by tonyjs on 16. 3. 25..
 */
@EBean
public class FingerprintAuthModel {

    public Observable<KeyStore> getKeyStoreObservable() {
        return Observable.<KeyStore>create(subscriber -> {
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                subscriber.onNext(keyStore);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.computation());
    }

    @TargetApi(Build.VERSION_CODES.M)
    public Observable<SecretKey> getSecretKeyObservable(final KeyStore keyStore) {
        return Observable.<SecretKey>create(subscriber -> {
            try {
                keyStore.load(null);

                KeyGenerator keyGenerator =
                        KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
                initKeyGenerator(keyGenerator);

                SecretKey key = keyGenerator.generateKey();
                subscriber.onNext(key);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.trampoline());
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initKeyGenerator(KeyGenerator keyGenerator) throws InvalidAlgorithmParameterException {
        keyGenerator.init(new KeyGenParameterSpec.Builder("jandi_auth_key",
                KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                .setUserAuthenticationRequired(true)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                .build());
    }

    @TargetApi(Build.VERSION_CODES.M)
    public Observable<Cipher> initCipherObservable(final SecretKey secretKey) {
        return Observable.<Cipher>create(subscriber -> {
            try {
                Cipher cipher = Cipher.getInstance(KeyProperties.KEY_ALGORITHM_AES + "/"
                        + KeyProperties.BLOCK_MODE_CBC + "/"
                        + KeyProperties.ENCRYPTION_PADDING_PKCS7);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                subscriber.onNext(cipher);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        }).subscribeOn(Schedulers.trampoline());
    }

}
