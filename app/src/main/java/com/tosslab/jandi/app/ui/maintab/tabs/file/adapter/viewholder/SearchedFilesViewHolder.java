package com.tosslab.jandi.app.ui.maintab.tabs.file.adapter.viewholder;

import android.graphics.Paint;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.search.ResSearch;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.UiUtils;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import butterknife.Bind;
import butterknife.ButterKnife;

public class SearchedFilesViewHolder extends RecyclerView.ViewHolder {

    @Bind(R.id.tv_searched_file_name)
    TextView tvFileName;

    @Bind(R.id.tv_searched_file_owner_name)
    TextView tvFileOwner;

    @Bind(R.id.tv_searched_file_date)
    TextView tvDate;

    @Bind(R.id.iv_searched_file_type)
    ImageView ivFileType;

    @Bind(R.id.v_searched_file_round)
    View vFileRound;

    @Bind(R.id.v_searched_file_name_line_through)
    View vLineThrough;

    @Bind(R.id.tv_searched_file_type_comment)
    TextView tvComment;

    @Bind(R.id.iv_searched_file_type_comment)
    ImageView ivComment;

    @Bind(R.id.vg_comment_cnt)
    ViewGroup vgCommentCnt;

    @Bind(R.id.tv_file_size)
    TextView tvFileSize;

    public SearchedFilesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(ResSearch.SearchRecord searchedFile) {
        ResSearch.File content = searchedFile.getFile();

        String searchedFileName = content.getTitle();
        setFileName(searchedFileName);

        User entity = TeamInfoLoader.getInstance().getUser(searchedFile.getWriterId());

        if (entity != null) {
            String searchedFileOwnerName = entity.getName();
            tvFileOwner.setText(searchedFileOwnerName);
        } else {
            tvFileOwner.setText("");
        }

        String searchedFileDate = DateTransformator.getTimeString(searchedFile.getCreatedAt());
        tvDate.setText(searchedFileDate);

        if (content.getCommentCount() > 0) {
            vgCommentCnt.setVisibility(View.VISIBLE);
            if (content.getCommentCount() > 99) {
                tvComment.setText(String.valueOf(99));
            } else {
                tvComment.setText(String.valueOf(content.getCommentCount()));
            }


        } else {
            vgCommentCnt.setVisibility(View.INVISIBLE);
        }

        if (entity != null && entity.isEnabled()) {
            tvFileOwner.setTextColor(JandiApplication.getContext().
                    getResources().getColor(R.color.jandi_file_search_item_owner_text));
            vLineThrough.setVisibility(View.GONE);
        } else {
            tvFileOwner.setTextColor(JandiApplication.getContext().
                    getResources().getColor(R.color.deactivate_text_color));
            vLineThrough.setVisibility(View.VISIBLE);
        }

        String serverUrl = content.getServerUrl();
        String fileType = content.getIcon();
        String fileUrl = content.getFileUrl();
        String thumbnailUrl = ImageUtil.getLargeProfileUrl(content);
        ImageUtil.setResourceIconOrLoadImage(
                ivFileType, vFileRound,
                fileUrl, thumbnailUrl,
                serverUrl, fileType);

        tvFileSize.setText(FileUtil.formatFileSize(searchedFile.getFile().getSize()));
    }

    private void setFileName(String searchedFileName) {
        String fileName = convertNoLineBreakText(searchedFileName);

        tvFileName.setText(fileName);

        Paint fileNamePaint = tvFileName.getPaint();

        int fileNameWidth = (int) fileNamePaint.measureText(fileName);

        DisplayMetrics displayMetrics = JandiApplication.getContext().getResources().getDisplayMetrics();

        int displayWidth = displayMetrics.widthPixels;

        int fileNameAreaWidth = displayWidth - (int) UiUtils.getPixelFromDp(176);

        if (fileNameWidth > 2 * fileNameAreaWidth) {
            String text1 = fileName.subSequence(0, 15).toString();
            String text2 = "...";
            String text3 = fileName.subSequence(tvFileName.length() - 11,
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
