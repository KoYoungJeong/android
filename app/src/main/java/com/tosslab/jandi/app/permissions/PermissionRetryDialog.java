package com.tosslab.jandi.app.permissions;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;

public class PermissionRetryDialog {
    private static void showDialog(Context context, int stringResId, int drawableResId) {

        View view = LayoutInflater.from(context).inflate(R.layout.dialog_permission_retry, null);
        ((TextView) view.findViewById(R.id.tv_dialog_permission_retry)).setText(stringResId);
        ((ImageView) view.findViewById(R.id.iv_dialog_permission_retry)).setImageResource(drawableResId);

        new AlertDialog.Builder(context, R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setView(view)
                .setPositiveButton(R.string.jandi_go_to_setting, (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.setData(Uri.parse("package:" + context.getPackageName()));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    context.startActivity(intent);
                })
                .create()
                .show();
    }

    public static void showExternalPermissionDialog(Context context) {
        showDialog(context, R.string.jandi_storage_access_permission, R.drawable.icon_popup_folder);
    }

    public static void showCallPermissionDialog(Context context) {
        showDialog(context, R.string.jandi_call_acess_permission, R.drawable.icon_popup_phone);
    }
}
