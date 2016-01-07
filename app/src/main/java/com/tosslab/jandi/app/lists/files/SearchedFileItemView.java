package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.facebook.drawee.view.SimpleDraweeView;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.image.ImageUtil;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
@EViewGroup(R.layout.item_searched_file)
public class SearchedFileItemView extends RelativeLayout {
    @ViewById(R.id.tv_searched_file_name)
    TextView tvFileName;
    @ViewById(R.id.tv_searched_file_owner_name)
    TextView tvFileOwner;
    @ViewById(R.id.tv_searched_file_type)
    TextView tvFileType;
    @ViewById(R.id.tv_searched_file_date)
    TextView tvDate;
    @ViewById(R.id.iv_searched_file_type)
    SimpleDraweeView ivFileType;
    @ViewById(R.id.v_searched_file_name_line_through)
    View vLineThrough;

    @ViewById(R.id.tv_searched_file_type_comment)
    TextView tvComment;
    @ViewById(R.id.iv_searched_file_type_comment)
    ImageView ivComment;

    Context context;

    public SearchedFileItemView(Context context) {
        super(context);
        this.context = context;
    }

    public void bind(ResMessages.FileMessage searchedFile) {
        ResMessages.FileContent content = searchedFile.content;

        String searchedFileName = content.title;
        tvFileName.setText(searchedFileName);

        FormattedEntity entity = EntityManager.getInstance().getEntityById(searchedFile.writerId);

        if (entity != EntityManager.UNKNOWN_USER_ENTITY) {
            String searchedFileOwnerName = entity.getName();
            tvFileOwner.setText(searchedFileOwnerName);
        } else {
            tvFileOwner.setText("");
        }

        tvFileType.setText(content.ext);

        String searchedFileDate = DateTransformator.getTimeString(searchedFile.createTime);
        tvDate.setText(searchedFileDate);

        tvComment.setText(String.valueOf(searchedFile.commentCount));

        if (searchedFile.commentCount > 0) {
            ivComment.setVisibility(View.VISIBLE);
            tvComment.setVisibility(View.VISIBLE);
        } else {
            ivComment.setVisibility(View.INVISIBLE);
            tvComment.setVisibility(View.INVISIBLE);
        }

        if (entity != null && entity.getUser() != null && TextUtils.equals(entity.getUser().status, "enabled")) {
            tvFileOwner.setTextColor(getResources().getColor(R.color.jandi_file_search_item_owner_text));
            vLineThrough.setVisibility(View.GONE);
        } else {
            tvFileOwner.setTextColor(getResources().getColor(R.color.deactivate_text_color));
            vLineThrough.setVisibility(View.VISIBLE);
        }

        String serverUrl = content.serverUrl;
        String fileType = content.icon;
        String fileUrl = content.fileUrl;
        String thumbnailUrl =
                ImageUtil.getThumbnailUrl(content.extraInfo, ImageUtil.Thumbnails.SMALL);
        ImageUtil.setResourceIconOrLoadImage(
                ivFileType, null,
                fileUrl, thumbnailUrl,
                serverUrl, fileType);
    }
}
