package com.tosslab.jandi.app.lists.files;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FormatConverter;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

/**
 * Created by justinygchoi on 2014. 7. 5..
 */
@EViewGroup(R.layout.item_searched_file)
public class SearchedFileItemView extends RelativeLayout {
    private final Logger log = Logger.getLogger(SearchedFileItemView.class);
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

        String searchedFileOwnerName = searchedFile.writer.name;
        textViewSearchedFileOwnerName.setText(searchedFileOwnerName);

        textViewSearchedFileType.setText(searchedFile.content.ext);

        String searchedFileDate = DateTransformator.getTimeString(searchedFile.createTime);
        textViewSearchedFileDate.setText(searchedFileDate);
        // 파일 타입에 해당하는 아이콘 연결
        String fileType = searchedFile.content.type;
        if (fileType != null) {
            if (fileType.startsWith("image")) {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_img);
            } else if (fileType.startsWith("audio")) {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_audio);
            } else if (fileType.startsWith("video")) {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_video);
            } else if (fileType.startsWith("application/pdf")) {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_pdf);
            } else if (fileType.startsWith("text")) {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_txt);
            } else if (TextUtils.equals(fileType, "application/x-hwp")) {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_hwp);
            } else if (FormatConverter.isDocmentMimeType(fileType)) {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_txt);
            } else if (FormatConverter.isPresentationMimeType(fileType)) {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_ppt);
            } else if (FormatConverter.isSpreadSheetMimeType(fileType)) {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_exel);
            } else {
                imageViewSearchedFileType.setImageResource(R.drawable.jandi_fl_icon_etc);
            }
        }

        commentTextView.setText(String.valueOf(searchedFile.commentCount));

        if (searchedFile.commentCount > 0) {
            commentImageView.setVisibility(View.VISIBLE);
            commentTextView.setVisibility(View.VISIBLE);
        } else {
            commentImageView.setVisibility(View.INVISIBLE);
            commentTextView.setVisibility(View.INVISIBLE);
        }
    }
}
