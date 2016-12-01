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
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

public class FileDownloadApi {

    public boolean flagCancel = false;

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
                if (response.isSuccessful()) {
                    Observable.just(response)
                            .observeOn(Schedulers.io())
                            .subscribe(it -> {
                                boolean isSuccess =
                                        writeResponseBodyToDisk(it.body(), saveFile, progressCallback);
                                if (!isSuccess) {
                                    if (progressCallback != null) {
                                        progressCallback.callback(Observable.error(new Throwable(response.message())));
                                    }
                                }
                            });
                } else {
                    if (progressCallback != null) {
                        progressCallback.callback(Observable.error(new Throwable(response.message())));
                    }
                }
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

    public File downloadImmediatly(String url, String saveFile, ProgressCallback progressCallback)
            throws IOException {
        String downloadUrl = getDownloadUrl(url);
        Api api = new Retrofit.Builder()
                .client(JandiApplication.getOkHttpClient())
                .baseUrl(downloadUrl)
                .build().create(Api.class);
        Call<ResponseBody> download = api.download(downloadUrl);
        ResponseBody body = download.execute().body();
        boolean isSuccess = writeResponseBodyToDisk(body, saveFile, progressCallback);
        if (!isSuccess) {
            return null;
        } else {
            return new File(saveFile);
        }
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

    public void cancel() {
        flagCancel = true;
    }

    public boolean writeResponseBodyToDisk(ResponseBody body, String targetFile, ProgressCallback progressCallback) {

        if (body == null) {
            progressCallback.callback(Observable.error(new Exception("It cannot be download")));
            return false;
        }

        PublishSubject<Integer> callback = PublishSubject.create();

        if (progressCallback != null) {
            progressCallback.callback(callback.onBackpressureBuffer());
        } else {
            callback.subscribe(it -> {
            }, t -> {
            });
        }

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
            while (!flagCancel) {
                int read = inputStream.read(fileReader);

                if (read == -1) {
                    break;
                }

                outputStream.write(fileReader, 0, read);

                fileSizeDownloaded += read;

                callback.onNext((int) ((fileSizeDownloaded * 100) / fileSize));
            }

            outputStream.flush();

            if (inputStream != null) {
                inputStream.close();
            }

            if (outputStream != null) {
                outputStream.close();
            }

            if (!flagCancel) {
                callback.onCompleted();
                return true;
            } else {
                futureStudioIconFile.delete();
                return false;
            }
        } catch (IOException e) {
            callback.onError(e);
            return false;
        }
    }

    interface Api {

        @Streaming
        @GET("{path}")
        Call<ResponseBody> download(@Path("path") String path);
    }
}
