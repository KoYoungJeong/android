package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.BitmapUtil;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;
import com.tosslab.jandi.app.utils.mimetype.filter.IconFilterUtil;

/**
 * Created by tee on 15. 7. 29..
 */
public class FileStarMentionViewHolder extends CommonStarMentionViewHolder {

    private TextView tvName;
    private ImageView ivFile;

    public FileStarMentionViewHolder(View itemView) {
        super(itemView);
        tvName = (TextView) itemView.findViewById(R.id.tv_star_mention_file_name);
        ivFile = (ImageView) itemView.findViewById(R.id.iv_star_file);
    }

    @Override
    public String toString() {
        return "FileStarMentionViewHolder{" +
                ", starMentionFileName=" + tvName +
                '}';
    }

    @Override
    public void bindView(StarMentionVO starMentionVO) {
        super.bindView(starMentionVO);
        tvName.setText(starMentionVO.getFileName());

        StarMentionedMessageObject.Message.Content content = starMentionVO.getContent();

        String icon = content.icon;
        MimeTypeUtil.FilterType filterType = IconFilterUtil.getMimeType(icon);
        if (filterType == MimeTypeUtil.FilterType.Image) {
            String thumbnailUrl = !TextUtils.isEmpty(content.smallThumbnailUrl)
                    ? content.smallThumbnailUrl
                    : !TextUtils.isEmpty(content.mediumThumbnailUrl)
                    ? content.mediumThumbnailUrl
                    : !TextUtils.isEmpty(content.largeThumbnailUrl)
                    ? content.largeThumbnailUrl
                    : content.fileUrl;
            if (!TextUtils.isEmpty(thumbnailUrl)) {
                BitmapUtil.loadImageByGlideOrIonWhenGif(
                        ivFile, thumbnailUrl,
                        R.drawable.jandi_fl_icon_img, R.drawable.jandi_fl_icon_img);
            } else {
                ivFile.setImageResource(R.drawable.jandi_fl_icon_img);
            }
        } else {
            // 파일 타입에 해당하는 아이콘 연결
            int mimeTypeResource = MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, icon);
            ivFile.setImageResource(mimeTypeResource);
        }
    }
}
