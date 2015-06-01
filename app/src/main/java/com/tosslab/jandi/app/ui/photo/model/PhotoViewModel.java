package com.tosslab.jandi.app.ui.photo.model;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;

import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.EBean;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by tonyjs on 15. 5. 28..
 */
@EBean
public class PhotoViewModel {

    public int getExifOrientationDegree(String filePath) {
        int degree = 0;
        ExifInterface exifInterface = null;
        try {
            exifInterface = new ExifInterface(filePath);
        } catch (IOException e) {
            // EXIF 가 없는 경우
            e.printStackTrace();
            return degree;
        }

        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
        switch (orientation) {
            case ExifInterface.ORIENTATION_ROTATE_90:
                degree = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                degree = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                degree = 270;
                break;
        }

        return degree;
    }

    public Bitmap getBitmapFromFileAvoidOOM(String filePath) {
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeFile(filePath);
        } catch (OutOfMemoryError e0) {
            e0.printStackTrace();
            bitmap.recycle();
            try {
                bitmap = getInSampleSizedBitmap(filePath, 2);
            } catch (OutOfMemoryError e1) {
                e1.printStackTrace();
                bitmap.recycle();
                try {
                    bitmap = getInSampleSizedBitmap(filePath, 4);
                } catch (OutOfMemoryError e2) {
                    e2.printStackTrace();
                    bitmap.recycle();
                    try {
                        bitmap = getInSampleSizedBitmap(filePath, 8);
                    } catch (OutOfMemoryError e3) {
                        e3.printStackTrace();
                        try {
                            bitmap = getInSampleSizedBitmap(filePath, 16);
                        } catch (OutOfMemoryError e4) {
                            bitmap.recycle();
                            bitmap = null;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bitmap;
    }

    private Bitmap getInSampleSizedBitmap(String filePath, int sampleSize) throws OutOfMemoryError {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public Bitmap getRotateBitmap(Bitmap originalBitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.setRotate(degree);

        Bitmap rotateBitmap = Bitmap.createBitmap(originalBitmap,
                0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(),
                matrix, true);

        if (originalBitmap != rotateBitmap) {
            originalBitmap.recycle();
        }

        return rotateBitmap;
    }

    public File getFileFromBitmap(Bitmap bitmap, String filePath) {
        File file = null;
        FileOutputStream fos = null;
        try {
            file = new File(filePath);
            fos = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return file;
    }

    private File imageFile;

    public void setImageFile(File file) {
        imageFile = file;
    }

    public File getImageFile() {
        return imageFile;
    }

    public void deleteImageFile() {
        if (imageFile == null || !imageFile.exists()) {
            return;
        }

        LogUtil.e("!! delete image file");
        imageFile.delete();
        imageFile = null;
    }

}
