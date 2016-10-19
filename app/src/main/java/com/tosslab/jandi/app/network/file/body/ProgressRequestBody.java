package com.tosslab.jandi.app.network.file.body;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLConnection;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;
import rx.Subscription;
import rx.subjects.PublishSubject;

public class ProgressRequestBody extends RequestBody {
    private static final int DEFAULT_BUFFER_SIZE = 2048;
    private final Subscription subscription;
    private File file;
    private PublishSubject<Integer> progressSubject;

    public ProgressRequestBody(final File file, final ProgressCallback listener) {
        this.file = file;
        progressSubject = PublishSubject.create();
        subscription = listener.callback(progressSubject);
    }

    @Override
    public MediaType contentType() {
        String contentType = URLConnection.guessContentTypeFromName(file.getName());
        return MediaType.parse(contentType);
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        long fileLength = file.length();
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        FileInputStream in = new FileInputStream(file);
        long uploaded = 0;

        try {
            int read;
            while ((read = in.read(buffer)) != -1) {

                progressSubject.onNext((int) ((uploaded * 100) / fileLength));

                uploaded += read;
                try {
                    sink.write(buffer, 0, read);
                } catch (IOException e) {
                    e.printStackTrace();
                    progressSubject.onError(e);
                    throw e;
                }
            }

            progressSubject.onCompleted();
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }

    }

}