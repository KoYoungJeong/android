package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;

/**
 * Created by tee on 15. 7. 29..
 */
public class StarredFileViewHolder extends BaseViewHolder<StarredMessage> {

    private ImageView ivProfile;
    private TextView tvWriter;

    private View vFileRound;
    private TextView tvFileName;
    private ImageView ivFile;
    private TextView tvFileSize;

    private StarredFileViewHolder(View itemView) {
        super(itemView);
        ivProfile = (ImageView) itemView.findViewById(R.id.iv_starred_profile);
        tvWriter = (TextView) itemView.findViewById(R.id.tv_starred_name);

        tvFileName = (TextView) itemView.findViewById(R.id.tv_starred_file_name);
        ivFile = (ImageView) itemView.findViewById(R.id.iv_star_file);
        vFileRound = itemView.findViewById(R.id.v_star_file_round);

        tvFileSize = (TextView) itemView.findViewById(R.id.tv_file_size);
    }

    public static StarredFileViewHolder newInstance(ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View itemView = inflater.inflate(R.layout.item_starred_file, parent, false);
        return new StarredFileViewHolder(itemView);
    }

    @Override
    public void onBindView(StarredMessage starredMessage) {
        User user = TeamInfoLoader.getInstance().getUser(starredMessage.getMessage().writerId);
        StarredMessageProfileBinder.newInstance(tvWriter, ivProfile)
                .bind(user);

        StarredMessage.Message.Content content = starredMessage.getMessage().content;
        tvFileName.setText(content.title);

        String serverUrl = content.serverUrl;
        String fileType = content.icon;
        String fileUrl = content.fileUrl;

        String thumbnailUrl = content.extraInfo != null ?
                content.extraInfo.smallThumbnailUrl : null;

        ImageUtil.setResourceIconOrLoadImage(
                ivFile, vFileRound,
                fileUrl, thumbnailUrl,
                serverUrl, fileType);

        tvFileSize.setText(FileUtil.formatFileSize(Integer.parseInt(content.size)));
    }

}
