package com.tosslab.jandi.app.utils;

import android.content.Context;
import android.graphics.Color;

import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.JandiApplication;

/**
 * Created by justinygchoi on 2014. 7. 8..
 */
public class ColoredToast {
    public static void show(Context context, String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showGray(Context context, String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(SuperToast.Background.GRAY);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showLong(Context context, String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.LONG);
        superToast.setBackground(SuperToast.Background.BLUE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showWarning(Context context, String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.VERY_SHORT);
        superToast.setBackground(SuperToast.Background.ORANGE);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }

    public static void showError(Context context, String message) {
        SuperToast superToast = new SuperToast(JandiApplication.getContext());
        superToast.setText(message);
        superToast.setDuration(SuperToast.Duration.SHORT);
        superToast.setBackground(SuperToast.Background.RED);
        superToast.setTextColor(Color.WHITE);
        superToast.show();
    }
}
