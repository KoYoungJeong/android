package com.tosslab.jandi.app.utils;

import android.graphics.Color;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class ColoredToast {

    private static ColoredToast instance;
    private SuperToast superToast;

    private ColoredToast() {
        superToast = SuperToast.create(JandiApplication.getContext(), "", SuperToast.Duration.VERY_SHORT);
    }

    private synchronized static ColoredToast getInstance() {
        if (instance == null) {
            instance = new ColoredToast();
        }
        return instance;
    }

    public static void show(String message) {
        SuperToast superToast = getInstance().superToast;
        superToast.cancelAllSuperToasts();
        superToast.setText(message);
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setTextColor(Color.WHITE);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.show();

    }

    public static void show(int strResId) {
        SuperToast superToast = getInstance().superToast;
        superToast.cancelAllSuperToasts();
        superToast.setText(JandiApplication.getContext().getResources().getString(strResId));
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setTextColor(Color.WHITE);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.show();
    }

    public static void showGray(String message) {
        SuperToast superToast = getInstance().superToast;
        superToast.cancelAllSuperToasts();
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(R.drawable.bg_color_toast_gray);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showGray(int strResId) {
        SuperToast superToast = getInstance().superToast;
        superToast.cancelAllSuperToasts();
        superToast.setText(JandiApplication.getContext().getString(strResId));
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(R.drawable.bg_color_toast_gray);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showLong(String message) {
        SuperToast superToast = getInstance().superToast;
        superToast.cancelAllSuperToasts();
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.LONG);
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showWarning(String message) {
        SuperToast superToast = getInstance().superToast;
        superToast.cancelAllSuperToasts();
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(SuperToast.Background.ORANGE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showWarning(int resId) {
        SuperToast superToast = getInstance().superToast;
        superToast.cancelAllSuperToasts();
        superToast.setText(JandiApplication.getContext().getString(resId));
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(SuperToast.Background.ORANGE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showError(String message) {
        SuperToast superToast = getInstance().superToast;
        superToast.cancelAllSuperToasts();
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showError(int strResId) {
        SuperToast superToast = getInstance().superToast;
        superToast.cancelAllSuperToasts();
        superToast.setText(JandiApplication.getContext().getResources().getString(strResId));
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }
}
