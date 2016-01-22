package com.tosslab.jandi.app.ui.filedetail.adapter.viewholder;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.files.FileDownloadStartEvent;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.filter.IconFilterUtil;
import com.tosslab.jandi.app.utils.mimetype.placeholder.PlaceholderUtil;
import com.tosslab.jandi.app.utils.mimetype.source.SourceTypeUtil;

import de.greenrobot.event.EventBus;

/**
 * Created by tonyjs on 16. 2. 1..
 */
public class NormalFileViewHolder extends FileViewHolder {
    private ImageView ivFileThumb;
    private TextView tvFileMimeType;

    private NormalFileViewHolder(View itemView) {
        super(itemView);
    }

    public static NormalFileViewHolder newInstance(ViewGroup parent) {
        return new NormalFileViewHolder(FileViewHolder.getItemView(parent));
    }

    @Override
    public void addContentView(ViewGroup parent) {
        View contentView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.layout_file_detail_file_content, parent, true);
        ivFileThumb = (ImageView) contentView.findViewById(R.id.iv_file_detail_thumb);
        tvFileMimeType = (TextView) contentView.findViewById(R.id.tv_file_detail_mime_type);
    }

    @Override
    public void bindFileContent(ResMessages.FileMessage fileMessage) {
        final ResMessages.FileContent content = fileMessage.content;

        String serverUrl = content.serverUrl;
        String icon = content.icon;

        MimeTypeUtil.PlaceholderType placeholderType =
                PlaceholderUtil.getPlaceholderType(serverUrl, icon);

        Pair<String, Integer> resource = getResource(placeholderType);

        tvFileMimeType.setText(resource.first);
        ivFileThumb.setImageResource(resource.second);

        String fileUrl = ImageUtil.getImageFileUrl(content.fileUrl);
        if (TextUtils.isEmpty(fileUrl)) {
            ivFileThumb.setOnClickListener(null);
        } else {
            final MimeTypeUtil.SourceType sourceType = SourceTypeUtil.getSourceType(serverUrl);
            ivFileThumb.setOnClickListener(v -> {
                if (MimeTypeUtil.isFileFromGoogleOrDropbox(sourceType)) {
                    getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(fileUrl)));
                } else {
                    EventBus.getDefault().post(
                            new FileDownloadStartEvent(
                                    fileUrl, content.title, content.type, content.fileUrl));
                }
                AnalyticsUtil.sendEvent(
                        AnalyticsValue.Screen.FileDetail, AnalyticsValue.Action.ViewFile);
            });
        }
    }

    private Pair<String, Integer> getResource(MimeTypeUtil.PlaceholderType placeholderType) {
        int stringResourceId;
        int imageResourceId;
        switch (placeholderType) {
            default:
            case Etc:
                stringResourceId = R.string.jandi_file_category_all;
                imageResourceId = R.drawable.file_icon_etc_198;
                break;
            case Dropbox:
                stringResourceId = R.string.jandi_file_category_all;
                imageResourceId = R.drawable.file_icon_dropbox_198;
                break;
            case Google:
                stringResourceId = R.string.jandi_google_docs;
                imageResourceId = R.drawable.file_icon_google_198;
                break;
            case Audio:
                stringResourceId = R.string.jandi_file_category_audio;
                imageResourceId = R.drawable.file_icon_audio_198;
                break;
            case Video:
                stringResourceId = R.string.jandi_file_category_video;
                imageResourceId = R.drawable.file_icon_video_198;
                break;
            case SpreadSheet:
                stringResourceId = R.string.jandi_file_category_spreadsheet;
                imageResourceId = R.drawable.file_icon_exel_198;
                break;
            case Presentation:
                stringResourceId = R.string.jandi_file_category_presentation;
                imageResourceId = R.drawable.file_icon_ppt_198;
                break;
            case Pdf:
                stringResourceId = R.string.jandi_file_category_pdf;
                imageResourceId = R.drawable.file_icon_pdf_198;
                break;
            case Hwp:
                stringResourceId = R.string.jandi_file_category_document;
                imageResourceId = R.drawable.file_icon_hwp_198;
                break;
            case Document:
                stringResourceId = R.string.jandi_file_category_document;
                imageResourceId = R.drawable.file_icon_txt_198;
                break;
            case ZIP:
                stringResourceId = R.string.jandi_file_category_zip;
                imageResourceId = R.drawable.file_icon_zip_198;
                break;
        }
        return Pair.create(getContext().getResources().getString(stringResourceId), imageResourceId);
    }

}
