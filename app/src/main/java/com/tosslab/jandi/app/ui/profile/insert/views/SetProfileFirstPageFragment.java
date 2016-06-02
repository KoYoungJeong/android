package com.tosslab.jandi.app.ui.profile.insert.views;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.ui.profile.insert.presenter.SetProfileFirstPagePresenter;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Bean;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import de.greenrobot.event.EventBus;

/**
 * Created by tee on 16. 3. 16..
 */

@EFragment(R.layout.fragment_insert_profile_first)
public class SetProfileFirstPageFragment extends Fragment
        implements SetProfileFirstPagePresenter.View {

    public static final String EXTRA_NEW_PHOTO_FILE = "new_photo_file";
    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQUEST_CHARACTER = 0x11;
    public static final int REQUEST_CROP = 11;

    @Bean
    SetProfileFirstPagePresenter presenter;

    @ViewById(R.id.iv_profile_picture)
    ImageView ivProfilePicture;
    @ViewById(R.id.tv_name)
    TextView tvName;
    @ViewById(R.id.tv_name_length)
    TextView tvNameLength;
    @ViewById(R.id.tv_profile_introduce)
    TextView tvProfileIntro;

    private AlertDialog profileChoosedialog = null;

    private File photoFile;

    private ProgressWheel progressWheel;

    private OnChangePageClickListener onChangePageClickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChangePageClickListener) {
            onChangePageClickListener = (OnChangePageClickListener) context;
        }
    }

    @AfterInject
    void init() {
        presenter.setView(this);
    }

    @AfterViews
    void initViews() {
        presenter.requestProfile();
    }

    @Click(R.id.iv_edit_profile_picture)
    void onClickEditProfilePicture() {
        Permissions.getChecker()
                .activity(getActivity())
                .permission(() -> Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .hasPermission(this::showProfileChooseDialog)
                .noPermission(() -> {
                    String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
                    requestPermissions(permissions, REQ_STORAGE_PERMISSION);
                })
                .check();

    }

    @Click(R.id.tv_name)
    void onClickInputName(View view) {
        if (NetworkCheckUtil.isConnected()) {
            launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME,
                    ((TextView) view)
            );
        }
    }

    @Click(R.id.iv_next_page)
    void onClickNextPage() {
        if (onChangePageClickListener != null) {
            onChangePageClickListener.onClickChangePage();
        }
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void displayProfileName(String name) {
        tvName.setText(name);
        tvNameLength.setText(name.length() + "/30");
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void updateLocalProfileImage(File tempPhotoFile) {
        if (tempPhotoFile == null || !tempPhotoFile.exists()) {
            return;
        }
        ImageUtil.loadProfileImage(ivProfilePicture,
                Uri.fromFile(tempPhotoFile), R.drawable.profile_img);
    }

    @Override
    @UiThread
    public void displayProfileImage(ResLeftSideMenu.User user) {
        String profileImageUrlPath = null;
        if (user.u_photoThumbnailUrl != null) {
            profileImageUrlPath = !TextUtils.isEmpty(user.u_photoThumbnailUrl.largeThumbnailUrl) ?
                    user.u_photoThumbnailUrl.largeThumbnailUrl : user.u_photoUrl;
        } else if (!TextUtils.isEmpty(user.u_photoUrl)) {
            profileImageUrlPath = user.u_photoUrl;
        }

        if (!TextUtils.isEmpty(profileImageUrlPath) && !getActivity().isFinishing()) {
            ImageUtil.loadProfileImage(ivProfilePicture,
                    profileImageUrlPath, R.drawable.profile_img);
        }
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showCheckNetworkDialog() {
        AlertUtil.showCheckNetworkDialog(getActivity(), null);
    }

    public void initProfileChooseDialog() {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_profile_image_selector, null);
        profileChoosedialog = new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_300)
                .setView(view)
                .setNegativeButton(this.getResources().getString(R.string.jandi_cancel),
                        (dialog, id) -> dialog.dismiss())
                .create();

        TextView tvTitle = (TextView) view.findViewById(R.id.tv_dialog_title);
        tvTitle.setText(this.getResources().getString(R.string.jandi_member_profile_edit));

        view.findViewById(R.id.tv_from_galary).setOnClickListener(v -> {
            presenter.onRequestCropImage(SetProfileFirstPageFragment.this);
            profileChoosedialog.dismiss();

        });
        view.findViewById(R.id.tv_from_camera).setOnClickListener(v -> {
            presenter.onRequestCamera(SetProfileFirstPageFragment.this);
            profileChoosedialog.dismiss();
        });
        view.findViewById(R.id.tv_from_character).setOnClickListener(v -> {
            presenter.onRequestCharacter(SetProfileFirstPageFragment.this);
            profileChoosedialog.dismiss();
        });
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void showProfileChooseDialog() {
        if (profileChoosedialog == null) {
            initProfileChooseDialog();
        }
        profileChoosedialog.show();
    }

    @OnActivityResult(REQUEST_CROP)
    public void onImageCropResult(int resultCode, Intent imageData) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        Uri output = imageData.getParcelableExtra("output");

        String filePath = output.getPath();
        if (!TextUtils.isEmpty(filePath)) {
            presenter.startUploadProfileImage(getActivity(), filePath);
        }
    }

    @OnActivityResult(FileUploadController.TYPE_UPLOAD_TAKE_PHOTO)
    void onCameraActivityResult(int resultCode, Intent intent) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        if (presenter.getFilePath() != null) {
            presenter.startUploadProfileImage(getActivity(), presenter.getFilePath().getPath());
        } else {
            presenter.startUploadProfileImage(getActivity(), photoFile.getPath());
        }
    }

    @OnActivityResult(REQUEST_CHARACTER)
    void onCharacterActivityResult(int resultCode) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (!NetworkCheckUtil.isConnected()) {
            showCheckNetworkDialog();
            return;
        }

        if (presenter.getFilePath() != null) {
            presenter.startUploadProfileImage(getActivity(), presenter.getFilePath().getPath());
        } else {
            presenter.startUploadProfileImage(getActivity(), photoFile.getPath());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            photoFile = (File) savedInstanceState.getSerializable(EXTRA_NEW_PHOTO_FILE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);

        updateLocalProfileImage(photoFile);
    }

    @Override
    public void onPause() {
        super.onPause();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (presenter.getFilePath() != null) {
            outState.putSerializable(EXTRA_NEW_PHOTO_FILE, presenter.getFilePath());
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Permissions.getResult()
                .activity(getActivity())
                .addRequestCode(REQ_STORAGE_PERMISSION)
                .addPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, this::onClickEditProfilePicture)
                .neverAskAgain(() -> {
                    PermissionRetryDialog.showExternalPermissionDialog(getActivity());
                })
                .resultPermission(new OnRequestPermissionsResult(requestCode, permissions, grantResults));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showFailProfile() {
        ColoredToast.showError(JandiApplication.getContext().getString(R.string.err_profile_get_info));
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void showProgressWheel() {
        dismissProgressWheel();
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
        }
        progressWheel.show();
    }

    @UiThread(propagation = UiThread.Propagation.REUSE)
    @Override
    public void dismissProgressWheel() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    public void launchEditDialog(int dialogActionType, TextView textView) {
        String currentText = textView.getText().toString();
        DialogFragment newFragment = EditTextDialogFragment.newInstance(
                dialogActionType, currentText);
        newFragment.show(getActivity().getFragmentManager(), "dialog");
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void setTeamName(String teamName) {
        String intro = getResources().getString(R.string.jandi_profile_main_introduce, teamName);
        tvProfileIntro.setText(intro);
    }

    @Override
    @UiThread(propagation = UiThread.Propagation.REUSE)
    public void updateProfileFailed() {
        ColoredToast.showError(JandiApplication.getContext()
                .getString(R.string.err_profile_update));
    }

    public void onEvent(ProfileChangeEvent event) {
        presenter.onProfileImageChange(event.getMember());
    }

    public void onEvent(ConfirmModifyProfileEvent event) {
        presenter.updateProfileName(event.inputMessage);
    }

    public interface OnChangePageClickListener {
        void onClickChangePage();
    }
}