package com.tosslab.jandi.app.ui.maintab.tabs.mypage.starred.adapter.viewholder;

import android.graphics.Paint;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.commonobject.StarredMessage;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.base.adapter.viewholder.BaseViewHolder;
import com.tosslab.jandi.app.utils.UiUtils;
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
    private View vFullDivider;
    private View vSemiDivider;

    private StarredFileViewHolder(View itemView) {
        super(itemView);
        ivProfile = (ImageView) itemView.findViewById(R.id.iv_starred_profile);
        tvWriter = (TextView) itemView.findViewById(R.id.tv_starred_name);

        tvFileName = (TextView) itemView.findViewById(R.id.tv_starred_file_name);
        ivFile = (ImageView) itemView.findViewById(R.id.iv_star_file);
        vFileRound = itemView.findViewById(R.id.v_star_file_round);

        tvFileSize = (TextView) itemView.findViewById(R.id.tv_file_size);
        vSemiDivider = itemView.findViewById(R.id.v_semi_divider);
        vFullDivider = itemView.findViewById(R.id.v_full_divider);
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

        setFileName(content.title);

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

        if (starredMessage.hasSemiDivider()) {
            vSemiDivider.setVisibility(View.VISIBLE);
            vFullDivider.setVisibility(View.GONE);
        } else {
            vFullDivider.setVisibility(View.VISIBLE);
            vSemiDivider.setVisibility(View.GONE);
        }
    }

    private void setFileName(String starredFileName) {

        String fileName = convertNoLineBreakText(starredFileName);

        tvFileName.setText(fileName);

        Paint fileNamePaint = tvFileName.getPaint();

        int fileNameWidth = (int) fileNamePaint.measureText(fileName);

        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();

        int displayWidth = displayMetrics.widthPixels;

        int fileNameAreaWidth = displayWidth - (int) UiUtils.getPixelFromDp(168);

        if (fileNameWidth > 3 * fileNameAreaWidth) {

            String text1 = fileName.subSequence(0, 30).toString();
            String text2 = "...";
            String text3 = fileName.subSequence(tvFileName.length() - 12,
                    tvFileName.length()).toString();

            StringBuilder sb = new StringBuilder(text1);
            sb.append(text2);
            sb.append(text3);
            tvFileName.setText(sb.toString());
        }
    }

    private String convertNoLineBreakText(String s) {
        return s.replace('-', '\u2011')
                .replace(' ', '\u00A0')
                .replace('/', '\u2215').toString();
    }

}
