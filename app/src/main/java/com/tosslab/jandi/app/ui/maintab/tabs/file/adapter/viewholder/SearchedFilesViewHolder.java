package com.tosslab.jandi.app.ui.maintab.tabs.file.adapter.viewholder;

import android.support.v7.widget.RecyclerView;
import android.view.View;
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

    @Bind(R.id.tv_searched_file_type)
    TextView tvFileType;

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

    public SearchedFilesViewHolder(View itemView) {
        super(itemView);
        ButterKnife.bind(this, itemView);
    }

    public void bind(ResSearch.SearchRecord searchedFile) {
        ResSearch.File content = searchedFile.getFile();

        String searchedFileName = content.getTitle();
        tvFileName.setText(searchedFileName);

        User entity = TeamInfoLoader.getInstance().getUser(searchedFile.getWriterId());

        if (entity != null) {
            String searchedFileOwnerName = entity.getName();
            tvFileOwner.setText(searchedFileOwnerName);
        } else {
            tvFileOwner.setText("");
        }

        if (content.getSize() > 0) {
            String fileSize = FileUtil.formatFileSize(content.getSize());
            tvFileType.setText(String.format("%s, %s", fileSize, content.getExt()));
        } else {
            tvFileType.setText(content.getExt());
        }

        String searchedFileDate = DateTransformator.getTimeString(searchedFile.getCreatedAt());
        tvDate.setText(searchedFileDate);

        tvComment.setText(String.valueOf(content.getCommentCount()));

        if (content.getCommentCount() > 0) {
            ivComment.setVisibility(View.VISIBLE);
            tvComment.setVisibility(View.VISIBLE);
        } else {
            ivComment.setVisibility(View.INVISIBLE);
            tvComment.setVisibility(View.INVISIBLE);
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
    }

}
