package com.tosslab.jandi.app.views.preferences;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;

import com.tosslab.jandi.app.R;

import java.lang.reflect.Method;

/**
 * Created by jsuch2362 on 15. 11. 17..
 */
public class ListPreferenceCompat extends ListPreference {
    private Context context;
    private Dialog dialog;

    public ListPreferenceCompat(Context context) {
        super(context);
        this.context = context;
    }

    public ListPreferenceCompat(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    @Override
    public Dialog getDialog() {
        return dialog;
    }

    @Override
    protected void showDialog(Bundle state) {
        if (getEntries() == null || getEntryValues() == null) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array.");
        }

        int preselect = findIndexOfValue(getValue());
        AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setTitle(getDialogTitle())
                .setIcon(getDialogIcon())
                .setSingleChoiceItems(getEntries(), preselect, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which >= 0 && getEntryValues() != null) {
                            String value = getEntryValues()[which].toString();
                            if (callChangeListener(value) && isPersistent())
                                setValue(value);
                        }
                        dialog.dismiss();
                    }
                });

        PreferenceManager pm = getPreferenceManager();
        try {
            Method method = pm.getClass().getDeclaredMethod(
                    "registerOnActivityDestroyListener",
                    PreferenceManager.OnActivityDestroyListener.class);
            method.setAccessible(true);
            method.invoke(pm, this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        dialog = builder.create();
        if (state != null)
            dialog.onRestoreInstanceState(state);
        dialog.show();
    }

    @Override
    public void onActivityDestroy() {
        super.onActivityDestroy();
        if (dialog != null && dialog.isShowing())
            dialog.dismiss();
    }
}
