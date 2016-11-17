package com.tosslab.jandi.app.ui.maintab.tabs.file.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
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
        tvFileName.setText(searchedFileName);
        tvFileName.post(() -> {
            while (tvFileName.getLineCount() == 0 && !tvFileName.getText().toString().isEmpty()) {
            }
            if (tvFileName.getLineCount() > 2) {
                int lineEndIndex = tvFileName.getLayout().getLineEnd(1);
                String text1 = tvFileName.getText().subSequence(0, lineEndIndex - 16).toString();
                String text2 = "...";
                String text3 = tvFileName.getText().subSequence(tvFileName.length() - 12,
                        tvFileName.length()).toString();
                StringBuilder sb = new StringBuilder(text1);
                sb.append(text2);
                sb.append(text3);
                tvFileName.setText(sb.toString().replace(" ", "\u00A0"));
            }
        });

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
            tvComment.setText(String.valueOf(content.getCommentCount()));
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
        String thumbnailUrl = ImageUtil.getLargeProfileUrl(fileUrl);
        ImageUtil.setResourceIconOrLoadImage(
                ivFileType, vFileRound,
                fileUrl, thumbnailUrl,
                serverUrl, fileType);

        tvFileSize.setText(FileUtil.formatFileSize(searchedFile.getFile().getSize()));

    }

}
