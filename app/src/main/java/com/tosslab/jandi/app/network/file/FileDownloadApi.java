package com.tosslab.jandi.app.network.file;


import android.support.annotation.NonNull;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.file.body.ProgressCallback;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Streaming;
import rx.Observable;
import rx.Subscription;
import rx.subjects.PublishSubject;

public class FileDownloadApi {

    public Call<ResponseBody> download(String url, String saveFile, ProgressCallback progressCallback) {
        String downloadUrl = getDownloadUrl(url);
        Api api = new Retrofit.Builder()
                .client(JandiApplication.getOkHttpClient())
                .baseUrl(downloadUrl)
                .build().create(Api.class);


        Call<ResponseBody> download = api.download(downloadUrl);
        download.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                writeResponseBodyToDisk(response.body(), saveFile, progressCallback);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                if (progressCallback != null) {
                    progressCallback.callback(Observable.error(t));
                }
            }
        });

        return download;
    }

    public File downloadImmediatly(String url, String saveFile, ProgressCallback progressCallback) throws IOException {
        String downloadUrl = getDownloadUrl(url);
        Api api = new Retrofit.Builder()
                .client(JandiApplication.getOkHttpClient())
                .baseUrl(downloadUrl)
                .build().create(Api.class);

        ResponseBody body = api.download(downloadUrl).execute().body();
        writeResponseBodyToDisk(body, saveFile, progressCallback);
        return new File(saveFile);
    }

    @NonNull
    protected String getDownloadUrl(String url) {
        String downloadUrl;
        if (!url.endsWith("/")) {
            downloadUrl = url + "/";
        } else {
            downloadUrl = url;
        }
        return downloadUrl;
    }

    private boolean writeResponseBodyToDisk(ResponseBody body, String targetFile, ProgressCallback progressCallback) {
        PublishSubject<Integer> callback = PublishSubject.create();
        Subscription callbackSubscription;
        if (progressCallback != null) {
            callbackSubscription = progressCallback.callback(callback);
        } else {
            callbackSubscription = callback.subscribe(it -> {}, t -> {});
        }
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(targetFile);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                callback.onNext(0);
                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                    callback.onNext((int) ((fileSizeDownloaded * 100) / fileSize));
                }

                outputStream.flush();
                callback.onCompleted();
                return true;
            } catch (IOException e) {
                callback.onError(e);
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            callback.onError(e);
            return false;
        } finally {
            if (!callbackSubscription.isUnsubscribed()) {
                callbackSubscription.unsubscribe();
            }
        }
    }

    interface Api {

        @Streaming
        @GET("{path}")
        Call<ResponseBody> download(@Path("path") String path);
    }
}
