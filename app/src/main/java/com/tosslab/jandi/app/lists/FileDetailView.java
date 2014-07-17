package com.tosslab.jandi.app.lists;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.JandiConstants;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.RequestSelectionOfCdpToBeShared;
import com.tosslab.jandi.app.events.RequestViewFile;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.utils.DateTransformator;
import com.tosslab.jandi.app.utils.FormatConverter;

import org.androidannotations.annotations.EViewGroup;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import de.greenrobot.event.EventBus;

/**
 * Created by justinygchoi on 2014. 6. 24..
 */
@EViewGroup(R.layout.item_file_detail)
public class FileDetailView extends FrameLayout {
    private final Logger log = Logger.getLogger(FileDetailView.class);

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
    @ViewById(R.id.btn_file_detail_share)
    ImageButton buttonFileDetailShare;

    // Comment 일 경우
    @ViewById(R.id.ly_file_detail_comment)
    LinearLayout fileDetailCommentLayout;
    @ViewById(R.id.img_file_detail_comment_user_profile)
    ImageView imageViewCommentUserProfile;
    @ViewById(R.id.txt_file_detail_comment_user_name)
    TextView textViewCommentUserName;
    @ViewById(R.id.txt_file_detail_comment_create_date)
    TextView textViewCommentFileCreateDate;
    @ViewById(R.id.txt_file_detail_comment_content)
    TextView textViewCommentContent;


    Context mContext;

    public FileDetailView(Context context) {
        super(context);
        mContext = context;
    }

    public void bind(ResMessages.OriginalMessage fileDetail) {
        if (fileDetail instanceof ResMessages.FileMessage) {
            fileDetailLayout.setVisibility(VISIBLE);
            fileDetailCommentLayout.setVisibility(GONE);

            final ResMessages.FileMessage fileMessage = (ResMessages.FileMessage) fileDetail;
            // 사용자
            ResMessages.Writer writer = fileMessage.writer;
            String profileUrl = JandiConstants.SERVICE_ROOT_URL + writer.u_photoUrl;
            Picasso.with(mContext).load(profileUrl).centerCrop().fit().into(imageViewUserProfile);
            String userName = writer.u_firstName + " " + writer.u_lastName;
            textViewUserName.setText(userName);
            // 파일
            String createTime = DateTransformator.getTimeDifference(fileMessage.updateTime);
            textViewFileCreateDate.setText(createTime);
            textViewFileName.setText(fileMessage.content.name);
            // 파일 이름을 터치하면 파일 연결
            textViewFileName.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    String serverUrl = (fileMessage.content.serverUrl.equals("root"))?JandiConstants.SERVICE_ROOT_URL:fileMessage.content.serverUrl;
                    EventBus.getDefault().post(new RequestViewFile(serverUrl + fileMessage.content.fileUrl, fileMessage.content.type));
                }
            });

            String fileSizeString = FormatConverter.formatFileSize(fileMessage.content.size);
            textViewFileContentInfo.setText(fileSizeString + " " + fileMessage.content.type);

            // 이미지일 경우
            if (fileMessage.content.type != null && fileMessage.content.type.startsWith("image")) {
                imageViewPhotoFile.setVisibility(View.VISIBLE);
                String photoUrl = (JandiConstants.SERVICE_ROOT_URL + fileMessage.content.fileUrl).replaceAll(" ", "%20");
                Picasso.with(mContext).load(photoUrl).centerCrop().fit().into(imageViewPhotoFile);
            }
            buttonFileDetailShare.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    EventBus.getDefault().post(new RequestSelectionOfCdpToBeShared());
                }
            });
        } else if (fileDetail instanceof ResMessages.CommentMessage) {
            fileDetailLayout.setVisibility(GONE);
            fileDetailCommentLayout.setVisibility(VISIBLE);

            ResMessages.CommentMessage commentMessage = (ResMessages.CommentMessage) fileDetail;

            // 프로필
            ResMessages.Writer writer = commentMessage.writer;
            String profileUrl = JandiConstants.SERVICE_ROOT_URL + writer.u_photoUrl;
            Picasso.with(mContext).load(profileUrl).centerCrop().fit().into(imageViewCommentUserProfile);
            // 이름
            String userName = writer.u_firstName + " " + writer.u_lastName;
            textViewCommentUserName.setText(userName);
            // 날짜
            String createTime = DateTransformator.getTimeDifference(commentMessage.updateTime);
            textViewCommentFileCreateDate.setText(createTime);
            // 댓글 내용
            textViewCommentContent.setText(commentMessage.content.body);
        }
    }
}
