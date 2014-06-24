package com.tosslab.toss.app.lists;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.toss.app.R;
import com.tosslab.toss.app.TossConstants;
import com.tosslab.toss.app.network.models.ResMessages;
import com.tosslab.toss.app.utils.DateTransformator;
import com.tosslab.toss.app.utils.FormatConverter;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;

/**
 * Created by justinygchoi on 2014. 6. 24..
 */
@EViewGroup(R.layout.item_file_detail)
public class FileDetailView extends FrameLayout {
    @ViewById(R.id.img_file_detail_user_profile)
    ImageView imageViewUserProfile;
    @ViewById(R.id.txt_file_detail_user_name)
    TextView textViewUserName;
    @ViewById(R.id.txt_file_detail_create_date)
    TextView textViewFileCreateDate;
    @ViewById(R.id.txt_file_detail_name)
    TextView textViewFileName;

    @ViewById(R.id.txt_file_detail_file_info)
    TextView textViewFileContentInfo;

    @ViewById(R.id.img_file_detail_photo)
    ImageView imageViewPhotoFile;
    @ViewById(R.id.ly_file_detail_unit)
    LinearLayout fileDetailLayout;

    Context mContext;

    public FileDetailView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(ResMessages.OriginalMessage fileDetail) {
        if (fileDetail instanceof ResMessages.FileMessage) {
            fileDetailLayout.setVisibility(VISIBLE);
            ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;
            // 사용자
            ResMessages.Writer writer = fileMessage.writer;
            String profileUrl = TossConstants.SERVICE_ROOT_URL + writer.u_photoUrl;
            Picasso.with(mContext).load(profileUrl).centerCrop().fit().into(imageViewUserProfile);
            String userName = writer.u_firstName + " " + writer.u_lastName;
            textViewUserName.setText(userName);
            // 파일
            String createTime = DateTransformator.getTimeDifference(fileMessage.updateTime);
            textViewFileCreateDate.setText(createTime);
            textViewFileName.setText(fileMessage.content.name);

            String fileSizeString = FormatConverter.formatFileSize(fileMessage.content.size);
            textViewFileContentInfo.setText(fileSizeString + " " + fileMessage.content.type);

            // 이미지일 경우
            if (fileMessage.content.type != null && fileMessage.content.type.startsWith("image")) {
                imageViewPhotoFile.setVisibility(View.VISIBLE);
                String photoUrl = TossConstants.SERVICE_ROOT_URL + fileMessage.content.fileUrl;
                Picasso.with(mContext).load(photoUrl).centerCrop().fit().into(imageViewPhotoFile);
            }
        } else if (fileDetail instanceof ResMessages.CommentMessage) {
            fileDetailLayout.setVisibility(GONE);
        }
    }
}
