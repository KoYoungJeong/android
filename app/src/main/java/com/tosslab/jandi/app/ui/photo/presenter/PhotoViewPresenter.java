package com.tosslab.jandi.app.ui.photo.presenter;

import com.koushikdutta.ion.ProgressCallback;
import com.tosslab.jandi.app.ui.photo.model.PhotoViewModel;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by tonyjs on 15. 6. 4..
 */
@EBean
public class PhotoViewPresenter {

    View view;
    @Bean
    PhotoViewModel model;

    public void setView(View photoView) {
        view = photoView;
    }

    @Background
    public void loadImage(String url, String imageType, ProgressCallback callback) {
        if (model.isGif(imageType)) {
            view.loadImageGif();
            return;
        }

        File file = model.getFile(url);
        if (file.exists()) {
            view.loadImage(file);
            return;
        }

        try {
            File downloadedFile = model.downloadFile(url, file, callback);
            view.loadImage(downloadedFile);
        } catch (ExecutionException | InterruptedException
                | IOException e) {
            // 다운로드 로직에서 발생한 Exception (User cancelled and else)
            // Temporary file create 중 발생한 Exception
            e.printStackTrace();
            if (view.isForeground()) {
                view.loadImage(url);
            }
        }
    }

    public interface View {
        void updateProgress(long total, long downloaded);

        void hideProgress();

        void loadImageGif();

        <T> void loadImage(T target);

        boolean isForeground();
    }
}
