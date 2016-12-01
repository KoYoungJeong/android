package com.tosslab.jandi.app.ui.share.file;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.github.johnpersano.supertoasts.SuperToast;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.messages.SelectedMemberInfoForMentionEvent;
import com.tosslab.jandi.app.events.share.ShareSelectRoomEvent;
import com.tosslab.jandi.app.events.share.ShareSelectTeamEvent;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.services.upload.UploadNotificationActivity;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.SearchedItemVO;
import com.tosslab.jandi.app.ui.share.MainShareActivity;
import com.tosslab.jandi.app.ui.share.file.dagger.DaggerFileShareComponent;
import com.tosslab.jandi.app.ui.share.file.dagger.FileShareModule;
import com.tosslab.jandi.app.ui.share.file.presenter.ImageSharePresenter;
import com.tosslab.jandi.app.ui.share.file.presenter.ImageSharePresenterImpl;
import com.tosslab.jandi.app.ui.share.model.ScrollViewHelper;
import com.tosslab.jandi.app.ui.share.views.ShareSelectRoomActivity_;
import com.tosslab.jandi.app.ui.share.views.ShareSelectTeamActivity;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.ProgressWheel;
import com.tosslab.jandi.app.utils.TextCutter;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.utils.file.FileExtensionsUtil;
import com.tosslab.jandi.app.utils.image.loader.ImageLoader;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;

public class FileShareFragment extends Fragment implements ImageSharePresenterImpl.View, MainShareActivity.Share {

    @InjectExtra
    String uri;

    @Bind(R.id.iv_share_photo)
    ImageView ivSharePhoto;

    @Bind(R.id.tv_share_image_title)
    TextView tvTitle;

    @Bind(R.id.et_share_comment)
    EditText etComment;

    @Bind(R.id.tv_room_name)
    TextView tvRoomName;

    @Bind(R.id.vg_file_icon)
    LinearLayout vgFileIcon;

    @Bind(R.id.vg_share_content)
    ViewGroup vgShareContent;

    @Bind(R.id.iv_share_file_icon)
    ImageView ivShareFileIcon;

    @Bind(R.id.tv_team_name)
    TextView tvTeamName;

    @Bind(R.id.tv_share_file_type)
    TextView tvShareFileType;

    @Bind(R.id.vg_viwer)
    LinearLayout vgViewer;

    @Bind(R.id.vg_share_root)
    ScrollView vgRoot;

    @Inject
    ImageSharePresenter imageSharePresenter;

    MentionControlViewModel mentionControlViewModel;

    ScrollViewHelper scrollViewHelper;

    ProgressWheel progressWheel;

    public static FileShareFragment create(Context context, String uri) {
        Bundle bundle = new Bundle();
        bundle.putString("uri", uri);
        return (FileShareFragment) Fragment.instantiate(context, FileShareFragment.class.getName(), bundle);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_share_image, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Bundle arguments = getArguments();
        if (arguments != null) {
            Dart.inject(this, arguments);
        }
        DaggerFileShareComponent.builder()
                .fileShareModule(new FileShareModule(this))
                .build()
                .inject(this);
        initProgressWheel();
        initViews();
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        initProgressWheel();
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            MenuItem item = menu.findItem(R.id.action_share);
            if (item != null) {
                item.setEnabled(tvRoomName.length() > 0);
            }
        }
    }

    private void initProgressWheel() {
        progressWheel = new ProgressWheel(getActivity());
    }

    void initViews() {

        TextCutter.with(etComment)
                .listener((s) -> {
                    SuperToast.cancelAllSuperToasts();
                    ColoredToast.showError(R.string.jandi_exceeded_max_text_length);
                });

        setOnScrollMode();

        imageSharePresenter.initView(uri);
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.jandi_share_to_jandi) + " (1/1)");
        }
    }

    private void setOnScrollMode() {
        scrollViewHelper = new ScrollViewHelper(etComment, vgRoot);
        scrollViewHelper.initTouchMode();
    }

    private String getMentionType() {
//        return mode == MainShareActivity.MODE_SHARE_TEXT ? MentionControlViewModel.MENTION_TYPE_MESSAGE : MentionControlViewModel.MENTION_TYPE_FILE_COMMENT;
        return MentionControlViewModel.MENTION_TYPE_FILE_COMMENT;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Observable.just(1, 1)
                .delay(100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(integer -> {
                    if (mentionControlViewModel != null) {
                        mentionControlViewModel.onConfigurationChanged();
                    }
                });

    }

    @Override
    public void bindImage(File file) {
        final String fileName = file.getName();
        setFileName(file.getName());
        if (FileExtensionsUtil.getExtensions(fileName) == FileExtensionsUtil.Extensions.IMAGE) {
            vgFileIcon.setVisibility(View.GONE);
            ivSharePhoto.setVisibility(View.VISIBLE);
            vgShareContent.setBackgroundResource(R.drawable.upload_bg);

            ImageLoader.newInstance()
                    .fragment(this)
                    .actualImageScaleType(ImageView.ScaleType.FIT_CENTER)
                    .uri(Uri.fromFile(file))
                    .into(ivSharePhoto);
        } else {
            vgFileIcon.setVisibility(View.VISIBLE);
            tvShareFileType.setText(FileExtensionsUtil.getFileTypeText(fileName));
            ivSharePhoto.setVisibility(View.GONE);
            vgShareContent.setBackgroundColor(0xffffffff);
            vgViewer.setBackgroundColor(0xffffffff);

            int resId = FileExtensionsUtil.getFileTypeBigImageResource(fileName);
            ivShareFileIcon.setImageResource(resId);
            vgFileIcon.setBackgroundColor(FileExtensionsUtil.getFileDetailBackground(
                    FileExtensionsUtil.getExtensions(fileName)));
        }
    }

    @Override
    public void showSuccessToast(String message) {
        ColoredToast.show(message);
    }

    @Override
    public void showFailToast(String message) {
        ColoredToast.showError(message);
    }

    @Override
    public void dismissProgressBar() {
        if (progressWheel != null && !progressWheel.isShowing()) {
            progressWheel.show();
        }
    }

    @Override
    public void showProgressBar() {
        if (progressWheel != null && progressWheel.isShowing()) {
            progressWheel.dismiss();
        }
    }

    private ProgressDialog getUploadProgress(String absolutePath, String name) {
        final ProgressDialog progressDialog = new ProgressDialog(getActivity());
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMessage(getActivity().getApplicationContext()
                .getString(R.string.jandi_upload) + " " + absolutePath + "/" + name);
        progressDialog.setCancelable(false);
        progressDialog.show();
        return progressDialog;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mentionControlViewModel != null) {
            mentionControlViewModel.registClipboardListener();
        }
    }

    @Override
    public void onPause() {
        if (mentionControlViewModel != null) {
            mentionControlViewModel.removeClipboardListener();
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void finishOnUiThread() {
        FragmentActivity activity = getActivity();
        if (activity != null && !activity.isFinishing()) {
            activity.finish();
        }
    }

    public String getTitleText() {
        return tvTitle.getText().toString();
    }

    @Override
    public void moveEntity(long teamId, long roomId, long entityId, int entityType) {

        Completable.fromAction(() -> {
            if (getActivity() == null) {
                return;
            }
            UploadNotificationActivity.startActivity(getActivity(), teamId, entityId);

            getActivity().finish();
        }).subscribeOn(AndroidSchedulers.mainThread())
                .subscribe();
    }

    @Override
    public void setTeamName(String name) {
        tvTeamName.setText(name);
    }

    @Override
    public void setRoomName(String name) {
        tvRoomName.setText(name);
        getActivity().supportInvalidateOptionsMenu();
    }

    @Override
    public void startShare() {

        List<MentionObject> mentions;
        String messageText;
        if (mentionControlViewModel != null) {
            ResultMentionsVO mentionInfoObject = mentionControlViewModel.getMentionInfoObject();
            mentions = mentionInfoObject.getMentions();
            messageText = mentionInfoObject.getMessage();
        } else {
            mentions = new ArrayList<>();
            messageText = etComment.getText().toString();
        }


        File imageFile = imageSharePresenter.getImageFile();
        ProgressDialog uploadProgress = getUploadProgress(imageFile.getParentFile().getAbsolutePath(), imageFile.getName());
        imageSharePresenter.uploadFile(imageFile, getTitleText(), messageText, uploadProgress, mentions);
    }

    @OnClick(R.id.vg_team)
    void clickSelectTeam() {
        LogUtil.e("team");
        startActivity(new Intent(getActivity(), ShareSelectTeamActivity.class));

        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TeamSelect);
    }

    @OnClick(R.id.vg_room)
    void clickSelectRoom() {
        LogUtil.e("room");
        ShareSelectRoomActivity_
                .intent(this)
                .extra("teamId", imageSharePresenter.getTeamId())
                .start();
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TopicSelect);
    }

    @OnClick(R.id.et_share_comment)
    void clickComment() {
        AnalyticsUtil.sendEvent(AnalyticsValue.Screen.SharetoJandi, AnalyticsValue.Action.TapComment);
    }

    public void onEvent(ShareSelectTeamEvent event) {
        long teamId = event.getTeamId();
        String teamName = event.getTeamName();
        imageSharePresenter.initEntityData(teamId, teamName);
    }

    public void onEvent(ShareSelectRoomEvent event) {
        long roomId = event.getRoomId();
        String roomName = event.getRoomName();
        int roomType = event.getRoomType();
        imageSharePresenter.setEntityData(roomId, roomName, roomType);
    }

    @Override
    public String getComment() {
        return etComment.getText().toString();
    }

    @Override
    public void setComment(String comment) {
        etComment.setText(comment);
    }

    @Override
    public void setMentionInfo(long teamId, long roomId, int roomType) {

        if (mentionControlViewModel != null || getActivity() == null) {
            mentionControlViewModel.reset();
            mentionControlViewModel = null;
        }

        mentionControlViewModel = MentionControlViewModel.newInstance(getActivity(), etComment, teamId, Arrays.asList(roomId), getMentionType());
    }

    @Override
    public void dismissDialog(ProgressDialog uploadProgress) {
        if (getActivity() == null || getActivity().isFinishing()) {
            return;
        }
        if (uploadProgress != null && uploadProgress.isShowing()) {
            uploadProgress.dismiss();
        }
    }

    public void onEvent(SelectedMemberInfoForMentionEvent event) {
        if (mentionControlViewModel != null) {
            SearchedItemVO searchedItemVO = new SearchedItemVO();
            searchedItemVO.setId(event.getId());
            searchedItemVO.setName(event.getName());
            searchedItemVO.setType(event.getType());
            mentionControlViewModel.mentionedMemberHighlightInEditText(searchedItemVO);
        }
    }

    @OnClick(R.id.tv_file_rename_button)
    void onClickFileRename() {
        showRenameTitleDialog();
    }

    private void showRenameTitleDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        RelativeLayout vgInputEditText = (RelativeLayout) LayoutInflater
                .from(getActivity()).inflate(R.layout.dialog_fragment_input_text, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_dialog_input_text);
        input.setHint(getString(R.string.jandi_name));
        ((TextView) vgInputEditText.findViewById(R.id.tv_popup_title)).setText(R.string.common_fileupload_rename_description);
        String filename = tvTitle.getText().toString();
        String extension = getFileExtension(filename);
        String filenameWithoutExtension = filename.replaceAll(extension, "");
        input.setText(filenameWithoutExtension);
        input.setSelection(filenameWithoutExtension.length());

        builder.setView(vgInputEditText)
                .setPositiveButton(getString(R.string.jandi_confirm), (dialog, which) -> {
                    String renamedFileName = input.getText().toString() + extension;
                    setFileName(renamedFileName);
                    tvTitle.requestFocus();
                })
                .setNegativeButton(getString(R.string.jandi_cancel), null);

        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(100)});
        input.addTextChangedListener(new SimpleTextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                String name = s.toString();
                if (name.trim().length() <= 0
                        || TextUtils.equals(filenameWithoutExtension, s)
                        || name.contains("\\")
                        || name.contains("/")
                        || name.contains(":")
                        || name.contains("*")
                        || name.contains("?")
                        || name.contains("\"")
                        || name.contains("<")
                        || name.contains(">")
                        || name.contains("|")) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }

    public void setFileName(String fileName) {
        String extension = getFileExtension(fileName);
        int lastIndexOf = fileName.lastIndexOf(extension);
        final SpannableStringBuilder filenameSp = new SpannableStringBuilder(fileName);

        filenameSp.setSpan(new ForegroundColorSpan(0xff333333),
                lastIndexOf, lastIndexOf + extension.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvTitle.setText(filenameSp);
    }

    private String getFileExtension(String fileName) {
        String extension = "";
        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i);
        }
        return extension;
    }

}