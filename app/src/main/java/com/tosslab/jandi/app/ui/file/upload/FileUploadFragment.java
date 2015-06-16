package com.tosslab.jandi.app.ui.file.upload;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.lists.FormattedEntity;
import com.tosslab.jandi.app.lists.entities.EntityManager;
import com.tosslab.jandi.app.lists.entities.EntitySimpleListAdapter;
import com.tosslab.jandi.app.utils.logger.LogUtil;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import rx.Observable;

/**
 * Created by Bill MinWook Heo on 15. 6. 15..
 */
@EFragment(R.layout.fragment_file_upload_insert_comment)
public class FileUploadFragment extends Fragment {

    static private int selectedEntityIdToBeShared;    // Share 할 chat-room
    private EntitySimpleListAdapter mEntityArrayAdapter;

    private OnSendImageListener onSendImageListener;

    @FragmentArg
    String realFilePath;
    @FragmentArg
    int currentEntityId;

    @ViewById(R.id.tv_file_preview_name)
    TextView tvFileNameTextView;

    @ViewById(R.id.spinner_cdps)
    Spinner shareSpinner;

    @ViewById(R.id.iv_file_preview)
    ImageView ivFileImage;

    @ViewById(R.id.et_comment_with_file_upload)
    EditText etComment;

    @AfterViews
    void initView() {

        // 파일 이름
        File uploadFile = new File(realFilePath);
        String fileName = uploadFile.getName();
        if (realFilePath.length() > 0) {
            tvFileNameTextView.setText(fileName);
        }

        // CDP
        shareSpinner.setPrompt(getString(R.string.jandi_action_share));
        EntityManager entityManager = EntityManager.getInstance(getActivity());
        List<FormattedEntity> unsharedEntities = entityManager.retrieveExclusivedEntities(Arrays.asList(entityManager.getMe().getId()));

        Iterator<FormattedEntity> enabledEntities = Observable.from(unsharedEntities)
                .filter(entity -> !entity.isUser() || TextUtils.equals(entity.getUser().status, "enabled")).toBlocking()
                .getIterator();

        List<FormattedEntity> formattedEntities = new ArrayList<>();

        while (enabledEntities.hasNext()) {
            formattedEntities.add(enabledEntities.next());
        }

        mEntityArrayAdapter = new EntitySimpleListAdapter(getActivity(), formattedEntities);
        shareSpinner.setAdapter(mEntityArrayAdapter);

        int size = mEntityArrayAdapter.getCount();
        int currentIndex = 0;
        for (int idx = 0; idx < size; ++idx) {
            if (mEntityArrayAdapter.getItem(idx).getId() == currentEntityId) {
                currentIndex = idx;
                break;
            }
        }

        shareSpinner.setSelection(currentIndex);
        shareSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                selectedEntityIdToBeShared
                        = ((FormattedEntity) adapterView.getItemAtPosition(i)).getEntity().id;
                LogUtil.d("Change to cdp ID to be shared : " + selectedEntityIdToBeShared);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Glide.with(getActivity())
                .load(uploadFile)
                .crossFade()
                .centerCrop()
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .into(ivFileImage);
    }

    @Click(R.id.btn_file_send)
    void onFileSend() {
        onSendImageListener.onSendImage(tvFileNameTextView.getText().toString().trim(),
                selectedEntityIdToBeShared,
                realFilePath,
                etComment.getText().toString().trim());
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        onSendImageListener = (OnSendImageListener) activity;
    }

    public interface OnSendImageListener {
        void onSendImage(String title, int entityId, String realFilePath, String comment);
    }

}
