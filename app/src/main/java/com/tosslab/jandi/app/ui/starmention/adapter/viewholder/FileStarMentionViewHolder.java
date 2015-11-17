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

    private final View vFileRound;
    private TextView tvName;
    private ImageView ivFile;

    public FileStarMentionViewHolder(View itemView) {
        super(itemView);
        tvName = (TextView) itemView.findViewById(R.id.tv_star_mention_file_name);
        ivFile = (ImageView) itemView.findViewById(R.id.iv_star_file);
        vFileRound = itemView.findViewById(R.id.v_star_file_round);
    }

    @Override
    public void bindView(StarMentionVO starMentionVO) {
        super.bindView(starMentionVO);
        tvName.setText(starMentionVO.getFileName());

        StarMentionedMessageObject.Message.Content content = starMentionVO.getContent();

        String icon = content.icon;
        MimeTypeUtil.FilterType filterType = IconFilterUtil.getMimeType(icon);
        ivFile.setScaleType(ImageView.ScaleType.FIT_CENTER);
        if (filterType == MimeTypeUtil.FilterType.Image) {
            String thumbnailUrl = !TextUtils.isEmpty(content.smallThumbnailUrl)
                    ? content.smallThumbnailUrl
                    : !TextUtils.isEmpty(content.mediumThumbnailUrl)
                    ? content.mediumThumbnailUrl
                    : !TextUtils.isEmpty(content.largeThumbnailUrl)
                    ? content.largeThumbnailUrl
                    : content.fileUrl;
            if (!TextUtils.isEmpty(thumbnailUrl)) {
                BitmapUtil.loadCropBitmapByIon(ivFile,
                        thumbnailUrl,
                        R.drawable.file_icon_img);
                vFileRound.setVisibility(View.VISIBLE);
            } else {
                ivFile.setImageResource(R.drawable.file_icon_img);
                vFileRound.setVisibility(View.GONE);
            }
        } else {
            // 파일 타입에 해당하는 아이콘 연결
            int mimeTypeResource = MimeTypeUtil.getMimeTypeIconImage(content.serverUrl, icon);
            ivFile.setImageResource(mimeTypeResource);
            vFileRound.setVisibility(View.GONE);
        }
    }

    @Override
    public String toString() {
        return "FileStarMentionViewHolder{" +
                ", starMentionFileName=" + tvName +
                '}';
    }
}
