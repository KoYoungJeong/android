package com.tosslab.jandi.app.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.utils.CircleTransform;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.ProgressWheel;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;

/**
 * Created by justinygchoi on 2014. 8. 27..
 */
@EActivity(R.layout.activity_profile)
public class ProfileActivity extends Activity {
    private final Logger log = Logger.getLogger(ProfileActivity.class);

    private static final int REQ_CODE_PICK_IMAGE = 0;
    private static final String TEMP_PHOTO_FILE = "temp.png";   // 임시 저장파일

    @Extra
    int myEntityId;
    @ViewById(R.id.profile_photo)
    ImageView imageViewProfilePhoto;

    private Context mContext;
    private String mMyToken;
    private ProgressWheel mProgressWheel;

    private File mTempPhotoFile = null;  // 프로필 사진 변경시 선택한 임시 파일

    @AfterViews
    void bindAdapter() {
        final ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setIcon(
                new ColorDrawable(getResources().getColor(android.R.color.transparent)));
        actionBar.setTitle("Profile");

        mContext = getApplicationContext();

        // Progress Wheel 설정
        mProgressWheel = new ProgressWheel(this);
        mProgressWheel.init();

        mMyToken = JandiPreference.getMyToken(mContext);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /************************************************************
     * 프로필 사진
     ************************************************************/
    @Click(R.id.profile_photo)
    void getPicture() {
        Intent intent = new Intent(
                Intent.ACTION_GET_CONTENT,      // 또는 ACTION_PICK
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/*");              // 모든 이미지
        intent.putExtra("crop", "true");        // Crop기능 활성화
        intent.putExtra(MediaStore.EXTRA_OUTPUT, getTempUri());     // 임시파일 생성
        intent.putExtra("outputFormat",         // 포맷방식
                Bitmap.CompressFormat.PNG.toString());

        startActivityForResult(intent, REQ_CODE_PICK_IMAGE);
    }

    /** 임시 저장 파일의 경로를 반환 */
    private Uri getTempUri() {
        mTempPhotoFile = getTempFile();
        return Uri.fromFile(mTempPhotoFile);
    }

    /** 외장메모리에 임시 이미지 파일을 생성하여 그 파일의 경로를 반환  */
    private File getTempFile() {
        if (isSDCARDMOUNTED()) {
            File f = new File(Environment.getExternalStorageDirectory(), // 외장메모리 경로
                    TEMP_PHOTO_FILE);
            try {
                f.createNewFile();      // 외장메모리에 temp.png 파일 생성
            } catch (IOException e) {
            }

            return f;
        } else
            return null;
    }

    /** SD카드가 마운트 되어 있는지 확인 */
    private boolean isSDCARDMOUNTED() {
        String status = Environment.getExternalStorageState();
        if (status.equals(Environment.MEDIA_MOUNTED))
            return true;

        return false;
    }

    /** 다시 액티비티로 복귀하였을때 이미지를 셋팅 */
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent imageData) {
        super.onActivityResult(requestCode, resultCode, imageData);

        switch (requestCode) {
            case REQ_CODE_PICK_IMAGE:
                if (resultCode == RESULT_OK) {
                    if (imageData != null) {
                        String filePath = Environment.getExternalStorageDirectory() + "/" + TEMP_PHOTO_FILE;
                        log.debug("temp profile img : " + filePath);

                        mTempPhotoFile = new File(filePath);
                        Picasso.with(this)
                                .load(mTempPhotoFile)
                                .placeholder(R.drawable.jandi_profile)
                                .transform(new CircleTransform())
                                .skipMemoryCache()              // 메모리 캐시를 쓰지 않는다.
                                .into(imageViewProfilePhoto);
                    }
                }
                break;
        }
    }
}
