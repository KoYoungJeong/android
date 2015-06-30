package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.mimetype.MimeTypeUtil;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
@EViewGroup(R.layout.item_searched_file)
public class SearchedFileItemView extends RelativeLayout {
    @ViewById(R.id.txt_searched_file_name)
    TextView textViewSearchedFileName;
    @ViewById(R.id.txt_searched_file_owner_name)
    TextView textViewSearchedFileOwnerName;
    @ViewById(R.id.txt_searched_file_type)
    TextView textViewSearchedFileType;
    @ViewById(R.id.txt_searched_file_date)
    TextView textViewSearchedFileDate;
    @ViewById(R.id.img_searched_file_type)
    ImageView imageViewSearchedFileType;
    @ViewById(R.id.img_searched_file_name_line_through)
    View imageViewLineThrough;

    @ViewById(R.id.txt_searched_file_type_comment)
    TextView commentTextView;
    @ViewById(R.id.img_searched_file_type_comment)
    ImageView commentImageView;

    Context mContext;

    public SearchedFileItemView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(ResMessages.FileMessage searchedFile) {
        String searchedFileName = searchedFile.content.title;
        textViewSearchedFileName.setText(searchedFileName);

        FormattedEntity entityById = EntityManager.getInstance(mContext).getEntityById(searchedFile.writerId);

        String searchedFileOwnerName = entityById.getName();
        textViewSearchedFileOwnerName.setText(searchedFileOwnerName);

        textViewSearchedFileType.setText(searchedFile.content.ext);

        String searchedFileDate = DateTransformator.getTimeString(searchedFile.createTime);
        textViewSearchedFileDate.setText(searchedFileDate);
        // 파일 타입에 해당하는 아이콘 연결
        imageViewSearchedFileType.setImageResource(MimeTypeUtil.getMimeTypeIconImage(searchedFile.content.serverUrl, searchedFile.content.icon));

        commentTextView.setText(String.valueOf(searchedFile.commentCount));

        if (searchedFile.commentCount > 0) {
            commentImageView.setVisibility(View.VISIBLE);
            commentTextView.setVisibility(View.VISIBLE);
        } else {
            commentImageView.setVisibility(View.INVISIBLE);
            commentTextView.setVisibility(View.INVISIBLE);
        }

        if (entityById != null && entityById.getUser() != null && TextUtils.equals(entityById.getUser().status, "enabled")) {
            textViewSearchedFileOwnerName.setTextColor(getResources().getColor(R.color.jandi_file_search_item_owner_text));
            imageViewLineThrough.setVisibility(View.GONE);
        } else {
            textViewSearchedFileOwnerName.setTextColor(getResources().getColor(R.color.deactivate_text_color));
            imageViewLineThrough.setVisibility(View.VISIBLE);


        }
    }
}
