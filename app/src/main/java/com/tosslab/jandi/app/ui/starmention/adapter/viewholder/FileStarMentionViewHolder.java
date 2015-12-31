package com.tosslab.jandi.app.ui.starmention.adapter.viewholder;

import android.view.View;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.StarMentionedMessageObject;
import com.tosslab.jandi.app.ui.starmention.vo.StarMentionVO;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tee on 15. 7. 29..
 */
public class FileStarMentionViewHolder extends CommonStarMentionViewHolder {

    private final View vFileRound;
    private TextView tvName;
    private SimpleDraweeView ivFile;

    public FileStarMentionViewHolder(View itemView) {
        super(itemView);
        tvName = (TextView) itemView.findViewById(R.id.tv_star_mention_file_name);
        ivFile = (SimpleDraweeView) itemView.findViewById(R.id.iv_star_file);
        vFileRound = itemView.findViewById(R.id.v_star_file_round);
    }

    @Override
    public void bindView(StarMentionVO starMentionVO) {
        super.bindView(starMentionVO);
        tvName.setText(starMentionVO.getFileName());

        StarMentionedMessageObject.Message.Content content = starMentionVO.getContent();

        String serverUrl = content.serverUrl;
        String fileType = content.icon;
        String fileUrl = content.fileUrl;

        String thumbnailUrl = content.extraInfo != null ?
                content.extraInfo.smallThumbnailUrl : null;

        ImageUtil.setResourceIconOrLoadImage(
                ivFile, vFileRound,
                fileUrl, thumbnailUrl,
                serverUrl, fileType);
    }

    @Override
    public String toString() {
        return "FileStarMentionViewHolder{" +
                ", starMentionFileName=" + tvName +
                '}';
    }
}
