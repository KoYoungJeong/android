package com.tosslab.jandi.app.ui.message.v2.model.file.action;

import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.koushikdutta.async.future.FutureCallback;
import com.koushikdutta.ion.Ion;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.ColoredToast;

import java.io.File;

/**
 * Created by Steve SeongUg Jung on 15. 4. 20..
 */
public class DownloadAction implements FileAction {
    private final Context context;

    public DownloadAction(Context context) {
        this.context = context;
    }

    @Override
    public void action(ResMessages.Link link) {

        ResMessages.FileMessage message = (ResMessages.FileMessage) link.message;
        String fileUrl = message.content.fileUrl;

        ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage("Downloading " + message.content.name);
        progressDialog.show();

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/Jandi");
        dir.mkdirs();

        String url = BitmapUtil.getFileeUrl(fileUrl);
        Ion.with(context)
                .load(url)
                .progressDialog(progressDialog)
                .write(new File(dir, message.content.name))
                .setCallback(new FutureCallback<File>() {
                    @Override
                    public void onCompleted(Exception e, File result) {
                        if (e == null) {
                            // 성공
                            showExecuteDialog(result);
                        } else {
                            // Fail
                            ColoredToast.showError(context, context.getString(R.string.err_download));
                        }
                    }
                });
    }

    private void showExecuteDialog(File result) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(context);
        builder.content("다운로드한 파일이 {} 에 저장되었습니다. 파일 바로보기를 하시겠습니까?")
                .positiveText(R.string.jandi_confirm)
                .negativeText(R.string.jandi_cancel)
                .callback(new MaterialDialog.ButtonCallback() {
                    @Override
                    public void onPositive(MaterialDialog dialog) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.fromFile(result));
                        try {
                            context.startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            e.printStackTrace();
                            ColoredToast.showError(context, "실행가능한 앱이 없습니다. 다운로드한 파일은 {} 에서 확인하실 수 있습니다.");
                        }
                    }
                })
                .show();
    }
}
