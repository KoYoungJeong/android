package com.tosslab.toss.app.lists;

import android.content.Context;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.tosslab.toss.app.R;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.utils.DateTransformator;

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

    Context mContext;

    public SearchedFileItemView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(ResMessages.FileMessage searchedFile) {
        String searchedFileName = searchedFile.content.name;
        textViewSearchedFileName.setText(searchedFileName);

        String searchedFileOwnerName = searchedFile.writer.name;
        textViewSearchedFileOwnerName.setText(searchedFileOwnerName);

        String searchedFileType = searchedFile.content.type;
        textViewSearchedFileType.setText(searchedFileType);

        String searchedFileDate = DateTransformator.getTimeString(searchedFile.createTime);
        textViewSearchedFileDate.setText(searchedFileDate);
    }
}
