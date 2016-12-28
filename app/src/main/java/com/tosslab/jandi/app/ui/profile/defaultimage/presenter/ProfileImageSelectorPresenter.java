package com.tosslab.jandi.app.ui.profile.defaultimage.presenter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.profile.defaultimage.model.ProfileImageSelectorModel;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

import javax.inject.Inject;

import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ProfileImageSelectorPresenter {
    ProfileImageSelectorModel profileImageSelectorModel;
    View view;

    @Inject
    public ProfileImageSelectorPresenter(ProfileImageSelectorModel profileImageSelectorModel, View view) {
        this.profileImageSelectorModel = profileImageSelectorModel;
        this.view = view;
    }

    public void initLists() {
        Observable.fromCallable(() -> {
            List<String> characterUrls = profileImageSelectorModel.getCharactersInfo();
            List<Integer> colorRGBs = profileImageSelectorModel.getColors();

            return Pair.create(characterUrls, colorRGBs);
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(pair -> {
                    view.showCharacterList(pair.first);
                    view.showColorList(pair.second);
                    view.showInitialImage();

                }, Throwable::printStackTrace);
    }

    public void makeCustomProfileImageFile(Uri fileUri, String imageUrl, int color) {
        view.showProgress();

        Completable.fromAction(() -> {
            File profileImageFile = new File(fileUri.getPath());
            Bitmap profileImageBitmap = null;

            try {
                profileImageBitmap = ImageLoader.newInstance().uri(Uri.parse(imageUrl)).getBitmapRect(JandiApplication.getContext());
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (profileImageBitmap != null) {
                int x = profileImageBitmap.getWidth();
                int y = profileImageBitmap.getHeight();
                Bitmap newProfileBitmap = Bitmap.createBitmap(x, y, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(newProfileBitmap);
                canvas.drawColor(color);
                canvas.drawBitmap(profileImageBitmap, 0, 0, null);

                FileOutputStream fileOutpuStream = null;
                try {
                    fileOutpuStream = new FileOutputStream(profileImageFile);
                    newProfileBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutpuStream);
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    if (fileOutpuStream != null) {
                        try {
                            fileOutpuStream.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    view.finishProgress();
                    view.finishWithOK();
                }, Throwable::printStackTrace);


    }

    public void removeFile(String filePath) {
        File file = new File(filePath);
        file.delete();
    }

    public interface View {
        void showCharacterList(List<String> characterUrls);

        void showColorList(List<Integer> colorRGBs);

        void showInitialImage();

        void finishWithOK();

        void showProgress();

        void finishProgress();
    }

}
