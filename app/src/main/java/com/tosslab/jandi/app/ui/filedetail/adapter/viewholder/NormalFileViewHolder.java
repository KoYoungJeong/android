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

import butterknife.Bind;

/**
 * Created by tonyjs on 16. 2. 1..
 */
public class NormalFileViewHolder extends FileViewHolder {
    @Bind(R.id.iv_file_detail_thumb)
    ImageView ivFileThumb;
    @Bind(R.id.vg_background)
    ViewGroup vgBackground;
    private View vContent;
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
    }

    @Override
    protected void initView() { }

    @Override
    public void bindFileContent(ResMessages.FileMessage fileMessage) {
        final ResMessages.FileContent content = fileMessage.content;

        String serverUrl = content.serverUrl;
        String icon = content.icon;

        MimeTypeUtil.PlaceholderType placeholderType =
                PlaceholderUtil.getPlaceholderType(serverUrl, icon);


        if (isDeleted(fileMessage.status)) {
            ivFileThumb.setImageResource(R.drawable.file_detail_deleted);
            vgBackground.setBackgroundColor(0xffa7a7a7);
        } else {
            ivFileThumb.setImageResource(getResource(placeholderType));
            vgBackground.setBackgroundColor(getFileDetailBackground(placeholderType));
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
                imageResourceId = R.drawable.file_detail_etc;
                break;
            case Dropbox:
                imageResourceId = R.drawable.file_detail_dropbox;
                break;
            case Google:
                imageResourceId = R.drawable.file_detail_googledocs;
                break;
            case Audio:
                imageResourceId = R.drawable.file_detail_audio;
                break;
            case Video:
                imageResourceId = R.drawable.file_detail_video;
                break;
            case SpreadSheet:
                imageResourceId = R.drawable.file_detail_excel;
                break;
            case Presentation:
                imageResourceId = R.drawable.file_detail_ppt;
                break;
            case Pdf:
                imageResourceId = R.drawable.file_detail_pdf;
                break;
            case Hwp:
                imageResourceId = R.drawable.file_detail_hwp;
                break;
            case Document:
                imageResourceId = R.drawable.file_detail_text;
                break;
            case ZIP:
                imageResourceId = R.drawable.file_detail_zip;
                break;
        }
        return imageResourceId;
    }

    private int getFileDetailBackground(MimeTypeUtil.PlaceholderType placeholderType) {
        int color;
        switch (placeholderType) {
            default:
            case Etc:
                color = 0xffa7a7a7;
                break;
            case Dropbox:
                color = 0xff2086fa;
                break;
            case Google:
                color = 0xff404040;
                break;
            case Audio:
                color = 0xffff992c;
                break;
            case Video:
                color = 0xff8267c1;
                break;
            case SpreadSheet:
                color = 0xff109d57;
                break;
            case Presentation:
                color = 0xffed6e3c;
                break;
            case Pdf:
                color = 0xffef5050;
                break;
            case Hwp:
                color = 0xff07adad;
                break;
            case Document:
                color = 0xff426bb7;
                break;
            case ZIP:
                color = 0xff828282;
                break;
        }
        return color;
    }

    public interface OnFileClickListener {
        void onFileClick(String fileUrl, MimeTypeUtil.SourceType sourceType);
    }

}
