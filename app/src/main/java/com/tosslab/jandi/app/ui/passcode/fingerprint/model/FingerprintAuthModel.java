package com.tosslab.jandi.app.ui.passcode.fingerprint.model;

import android.annotation.TargetApi;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.inject.Inject;

import rx.Observable;

public class FingerprintAuthModel {

    @Inject
    public FingerprintAuthModel() { }

    public Observable<KeyStore> getKeyStoreObservable() {
        return Observable.create(subscriber -> {
            try {
                KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
                subscriber.onNext(keyStore);
            } catch (Exception e) {
                subscriber.onError(e);
            }
            subscriber.onCompleted();
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    public Observable<SecretKey> getSecretKeyObservable(final KeyStore keyStore) {
        return Observable.create(subscriber -> {
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
        });
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void initKeyGenerator(KeyGenerator keyGenerator) throws InvalidAlgorithmParameterException {
        int purposes = KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT;
        keyGenerator.init(
                new KeyGenParameterSpec.Builder("jandi_auth_key", purposes)
                        .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                        .setUserAuthenticationRequired(true)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                        .build());
    }

    @TargetApi(Build.VERSION_CODES.M)
    public Observable<Cipher> initCipherObservable(final SecretKey secretKey) {
        return Observable.create(subscriber -> {
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
        });
    }

}
