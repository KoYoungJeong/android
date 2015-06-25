package com.tosslab.jandi.app.ui.filedetail.fileinfo;

import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.widget.ImageView;

import com.tosslab.jandi.app.events.files.FileDownloadStartEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by Steve SeongUg Jung on 15. 4. 29..
 */
public class NormalThumbLoader implements FileThumbLoader {
    private final ImageView iconFileType;
    private final ImageView imageViewPhotoFile;

    public NormalThumbLoader(ImageView iconFileType, ImageView imageViewPhotoFile) {

        this.iconFileType = iconFileType;
        this.imageViewPhotoFile = imageViewPhotoFile;
    }

    @Override
    public void loadThumb(ResMessages.FileMessage fileMessage, int entityId) {

        MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(fileMessage.content.serverUrl);
        String photoUrl = BitmapUtil.getFileUrl(fileMessage.content.fileUrl);

        iconFileType.setImageResource(MimeTypeUtil.getMimeTypeIconImage(fileMessage.content.serverUrl, fileMessage.content.icon));
        imageViewPhotoFile.setImageResource(MimeTypeUtil.getMimeTypePlaceholderImage(fileMessage.content.serverUrl, fileMessage.content.icon));

        if (TextUtils.isEmpty(photoUrl)) {
            imageViewPhotoFile.setEnabled(false);
        } else {
            imageViewPhotoFile.setEnabled(true);
        }

        // 파일 타입 이미지를 터치하면 다운로드로 넘어감.
        switch (sourceType) {
            case Google:
            case Dropbox:
                imageViewPhotoFile.setOnClickListener(view -> imageViewPhotoFile.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(photoUrl))));
                break;
            default:
                imageViewPhotoFile.setOnClickListener(view -> EventBus.getDefault().post(new FileDownloadStartEvent(BitmapUtil.getFileUrl(fileMessage.content.fileUrl), fileMessage.content.name, fileMessage.content.type)));
                break;
        }

    }
}
