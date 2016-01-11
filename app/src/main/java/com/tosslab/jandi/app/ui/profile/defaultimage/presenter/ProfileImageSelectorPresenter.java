package com.tosslab.jandi.app.ui.profile.defaultimage.presenter;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;

import com.koushikdutta.async.future.Future;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.ui.profile.defaultimage.model.ProfileImageSelectorModel;

import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by tee on 16. 1. 6..
 */

@EBean
public class ProfileImageSelectorPresenter {

    @Bean
    ProfileImageSelectorModel profileImageSelectorModel;

    View view;

    public void setView(View view) {
        this.view = view;
    }

    @Background
    public void initLists() {
        List<String> characterUrls = profileImageSelectorModel.getCharactersInfo();
        view.showCharacterList(characterUrls);
        List<Integer> colorRGBs = profileImageSelectorModel.getColors();
        view.showColorList(colorRGBs);
        view.showInitialImage();
    }

    public File makeCustomProfileImageFile(Uri fileUri, String imageUrl, int color) {

        File profileImageFile = new File(fileUri.getPath());
        Bitmap profileImageBitmap = null;

        Future<Bitmap> bitmapFuture = Ion.with(JandiApplication.getContext())
                .load(imageUrl)
                .asBitmap();

        try {
            profileImageBitmap = bitmapFuture.get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
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
                try {
                    fileOutpuStream.close();
                } catch (IOException e) {

                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
            return profileImageFile;
        } else {
            return null;
        }

    }

    public interface View {
        void showCharacterList(List<String> characterUrls);

        void showColorList(List<Integer> colorRGBs);

        void showInitialImage();
    }

}
