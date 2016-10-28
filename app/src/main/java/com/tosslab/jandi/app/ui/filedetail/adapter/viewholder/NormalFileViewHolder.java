package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.placeholder.PlaceholderUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

/**
 * Created by tonyjs on 16. 2. 1..
 */
public class NormalFileViewHolder extends FileViewHolder {
    private View vContent;
    private ImageView ivFileThumb;
    private OnFileClickListener onFileClickListener;

    private NormalFileViewHolder(View itemView, OnFileClickListener onFileClickListener) {
        super(itemView);
        this.onFileClickListener = onFileClickListener;
    }

    public static NormalFileViewHolder newInstance(ViewGroup parent, OnFileClickListener onFileClickListener) {
        return new NormalFileViewHolder(FileViewHolder.getItemView(parent), onFileClickListener);
    }

    @Override
    public void addContentView(ViewGroup parent) {
        vContent = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_file_detail_file_content, parent, true);
        ivFileThumb = (ImageView) vContent.findViewById(R.id.iv_file_detail_thumb);
    }

    @Override
    public void bindFileContent(ResMessages.FileMessage fileMessage) {
        final ResMessages.FileContent content = fileMessage.content;

        String serverUrl = content.serverUrl;
        String icon = content.icon;

        MimeTypeUtil.PlaceholderType placeholderType =
                PlaceholderUtil.getPlaceholderType(serverUrl, icon);


        if (isDeleted(fileMessage.status)) {
            ivFileThumb.setImageResource(R.drawable.file_icon_delete_198);
        } else {
            ivFileThumb.setImageResource(getResource(placeholderType));
        }

        String fileUrl = ImageUtil.getImageFileUrl(content.fileUrl);
        if (TextUtils.isEmpty(fileUrl)) {
            vContent.setOnClickListener(null);
        } else {
            final MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);
            vContent.setOnClickListener(v -> {
                if (onFileClickListener != null) {
                    onFileClickListener.onFileClick(fileUrl, sourceType);
                }
            });
        }
    }

    private int getResource(MimeTypeUtil.PlaceholderType placeholderType) {
        int imageResourceId;
        switch (placeholderType) {
            default:
            case Etc:
                imageResourceId = R.drawable.file_icon_etc_198;
                break;
            case Dropbox:
                imageResourceId = R.drawable.file_icon_dropbox_198;
                break;
            case Google:
                imageResourceId = R.drawable.file_icon_google_198;
                break;
            case Audio:
                imageResourceId = R.drawable.file_icon_audio_198;
                break;
            case Video:
                imageResourceId = R.drawable.file_icon_video_198;
                break;
            case SpreadSheet:
                imageResourceId = R.drawable.file_icon_exel_198;
                break;
            case Presentation:
                imageResourceId = R.drawable.file_icon_ppt_198;
                break;
            case Pdf:
                imageResourceId = R.drawable.file_icon_pdf_198;
                break;
            case Hwp:
                imageResourceId = R.drawable.file_icon_hwp_198;
                break;
            case Document:
                imageResourceId = R.drawable.file_icon_txt_198;
                break;
            case ZIP:
                imageResourceId = R.drawable.file_icon_zip_198;
                break;
        }
        return imageResourceId;
    }

    public interface OnFileClickListener {
        void onFileClick(String fileUrl, MimeTypeUtil.SourceType sourceType);
    }

}
