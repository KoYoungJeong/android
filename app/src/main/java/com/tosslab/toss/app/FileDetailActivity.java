package com.tosslab.toss.app;

import android.app.Activity;
import android.view.MenuItem;
import android.widget.EditText;

import com.tosslab.toss.app.network.MultipartUtility;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

@EActivity(R.layout.activity_file_detail)
public class FileDetailActivity extends Activity {
    private final Logger log = Logger.getLogger(FileDetailActivity.class);

    @Extra
    public String myToken;
    @Extra
    public String selectedFileUri;
    @Extra
    public int currentCdpId;

    @ViewById(R.id.et_file_name_to_be_uploaded)
    EditText editTextFileName;

    @ViewById(R.id.et_comment_with_file_upload)
    EditText editTextComment;

    @AfterViews
    public void initForm() {
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setDisplayUseLogoEnabled(true);

        File selectedFile = new File(selectedFileUri);
        editTextFileName.setText(selectedFile.getName());
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
}
