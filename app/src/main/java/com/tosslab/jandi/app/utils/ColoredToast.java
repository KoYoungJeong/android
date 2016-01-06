package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.graphics.Color;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class ColoredToast {

    public static SuperToast getToast(String message) {
        return getToast(message, SuperToast.Background.BLUE, Color.WHITE, SuperToast.Duration.VERY_SHORT);
    }

    public static SuperToast getToast(String message, int backgroundColor, int textColor, int duration) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(duration);
        superToast.setBackground(backgroundColor);
        superToast.setTextColor(textColor);
        return superToast;
    }

    public static void show(String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showGray(String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(R.drawable.bg_color_toast_gray);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showLong(String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.LONG);
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showWarning(String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(SuperToast.Background.ORANGE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showError(String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }
}
