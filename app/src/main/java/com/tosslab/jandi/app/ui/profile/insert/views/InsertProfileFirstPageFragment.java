package com.tosslab.jandi.app.ui.profile.insert.views;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.dialogs.EditTextDialogFragment;
import com.tosslab.jandi.app.events.ConfirmModifyProfileEvent;
import com.tosslab.jandi.app.events.entities.ProfileChangeEvent;
import com.tosslab.jandi.app.files.upload.FileUploadController;
import com.tosslab.jandi.app.permissions.OnRequestPermissionsResult;
import com.tosslab.jandi.app.permissions.PermissionRetryDialog;
import com.tosslab.jandi.app.permissions.Permissions;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.team.member.User;
import com.tosslab.jandi.app.ui.profile.insert.InsertProfileActivity;
import com.tosslab.jandi.app.ui.profile.insert.dagger.DaggerInsertProfileFirstPageComponent;
import com.tosslab.jandi.app.ui.profile.insert.dagger.InsertProfileFirstPageModule;
import com.tosslab.jandi.app.ui.profile.insert.presenter.InsertProfileFirstPagePresenter;
import com.tosslab.jandi.app.utils.AlertUtil;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.image.ImageUtil;
import com.tosslab.jandi.app.utils.network.NetworkCheckUtil;

import java.io.File;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

/**
 * Created by tee on 16. 3. 16..
 */

public class InsertProfileFirstPageFragment extends Fragment
        implements InsertProfileFirstPagePresenter.View {

    public static final String MODE = "MODE";
    public static final String MODE_TEAM_CREATE = "MODE_TEAM_CREATE";
    public static final String MODE_INSERT_PROFILE = "MODE_INSERT_PROFILE";

    public static final String EXTRA_NEW_PHOTO_FILE = "new_photo_file";
    public static final int REQ_STORAGE_PERMISSION = 101;
    public static final int REQUEST_CHARACTER = 0x11;
    public static final int REQUEST_CROP = 11;

    @Inject
    InsertProfileFirstPagePresenter presenter;

    @Bind(R.id.tv_welcome)
    TextView tvWelcome;

    @Bind(R.id.iv_profile_picture)
    ImageView ivProfilePicture;

    @Bind(R.id.tv_name)
    TextView tvName;

    @Bind(R.id.tv_name_length)
    TextView tvNameLength;

    @Bind(R.id.tv_profile_introduce)
    TextView tvProfileIntro;

    private AlertDialog profileChoosedialog = null;

    private File photoFile;

    private ProgressWheel progressWheel;

    private OnChangePageClickListener onChangePageClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_insert_profile_first, container, false);
        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnChangePageClickListener) {
            onChangePageClickListener = (OnChangePageClickListener) context;
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        presenter.requestProfile();
        Bundle bundle = getArguments();
        String pageMode = bundle.getString(InsertProfileFirstPageFragment.MODE);
        if (TextUtils.equals(pageMode, MODE_TEAM_CREATE)) {
            tvWelcome.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerInsertProfileFirstPageComponent.builder()
                .insertProfileFirstPageModule(new InsertProfileFirstPageModule(this))
                .build()
                .inject(this);

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
        JandiSocketService.stopService(getActivity().getApplication());
        EventBus.getDefault().unregister(this);
    }

    @OnClick(R.id.iv_edit_profile_picture)
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

    @OnClick(R.id.tv_name)
    void onClickInputName(View view) {
        if (NetworkCheckUtil.isConnected()) {
            launchEditDialog(
                    EditTextDialogFragment.ACTION_MODIFY_PROFILE_MEMBER_NAME,
                    ((TextView) view)
            );
        }
    }

    @OnClick(R.id.iv_next_page)
    void onClickNextPage() {
        if (onChangePageClickListener != null) {
            onChangePageClickListener.onClickMoveProfileNextPage();
        }
    }

    @Override
    public void displayProfileName(String name) {
        tvName.setText(name);
        tvNameLength.setText(name.length() + "/30");
    }

    public void updateLocalProfileImage(File tempPhotoFile) {
        if (tempPhotoFile == null || !tempPhotoFile.exists()) {
            return;
        }

        Uri uri = FileProvider.getUriForFile(getContext(),
                getString(R.string.jandi_file_authority), tempPhotoFile);

        ImageUtil.loadProfileImage(ivProfilePicture, uri, R.drawable.profile_img);
    }

    @Override
    public void displayProfileImage(User user) {
        String profileImageUrlPath = user.getPhotoUrl();

        if (!TextUtils.isEmpty(profileImageUrlPath) && !getActivity().isFinishing()) {
            ImageUtil.loadProfileImage(ivProfilePicture, profileImageUrlPath, R.drawable.profile_img);
        }
    }

    public void showCheckNetworkDialog() {
        Observable.just(1)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(i -> {
                    AlertUtil.showCheckNetworkDialog(getActivity(), null);
                });
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
            presenter.onRequestCropImage(InsertProfileFirstPageFragment.this);
            profileChoosedialog.dismiss();

        });
        view.findViewById(R.id.tv_from_camera).setOnClickListener(v -> {
            presenter.onRequestCamera(InsertProfileFirstPageFragment.this);
            profileChoosedialog.dismiss();
        });
        view.findViewById(R.id.tv_from_character).setOnClickListener(v -> {
            presenter.onRequestCharacter(InsertProfileFirstPageFragment.this);
            profileChoosedialog.dismiss();
        });
    }

    public void showProfileChooseDialog() {
        if (profileChoosedialog == null) {
            initProfileChooseDialog();
        }
        profileChoosedialog.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_CROP:
                onImageCropResult(resultCode, data);
                break;
            case FileUploadController.TYPE_UPLOAD_TAKE_PHOTO:
                onCameraActivityResult(resultCode, data);
                break;
            case REQUEST_CHARACTER:
                onCharacterActivityResult(resultCode);
                break;
        }
    }

    private void onImageCropResult(int resultCode, Intent imageData) {
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

    private void onCameraActivityResult(int resultCode, Intent intent) {
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

    private void onCharacterActivityResult(int resultCode) {
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

    @Override
    public void showFailProfile() {
        // TODO AccountHome, MainTab 각각의 접근하는 경우가 달라야 함
        if (getActivity() instanceof InsertProfileActivity) {
            ColoredToast.showError(JandiApplication.getContext().getString(R.string.err_profile_get_info));
        }
    }

    @Override
    public void showProgressWheel() {
        dismissProgressWheel();
        if (progressWheel == null) {
            progressWheel = new ProgressWheel(getActivity());
        }
        progressWheel.show();
    }

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
    public void setTeamName(String teamName) {
        String intro = getResources().getString(R.string.jandi_profile_main_introduce, teamName);
        SpannableString spannable = new SpannableString(intro);
        int startIndex = intro.lastIndexOf(teamName);
        spannable.setSpan(new StyleSpan(Typeface.BOLD),
                startIndex, startIndex + teamName.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        tvProfileIntro.setText(spannable);
    }

    @Override
    public void updateProfileFailed() {
        ColoredToast.showError(JandiApplication.getContext()
                .getString(R.string.err_profile_update));
    }

    public void onEventMainThread(ProfileChangeEvent event) {
        presenter.onProfileImageChange(new User(event.getMember()));
    }

    public void onEventMainThread(ConfirmModifyProfileEvent event) {
        presenter.updateProfileName(event.inputMessage);
    }

    public interface OnChangePageClickListener {
        void onClickMoveProfileNextPage();
    }

}