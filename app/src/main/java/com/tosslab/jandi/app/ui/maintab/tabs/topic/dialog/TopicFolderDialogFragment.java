package com.tosslab.jandi.app.ui.maintab.tabs.topic.dialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.f2prateek.dart.Dart;
import com.f2prateek.dart.InjectExtra;
import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.events.entities.TopicFolderRefreshEvent;
import com.tosslab.jandi.app.local.orm.repositories.info.FolderRepository;
import com.tosslab.jandi.app.network.dagger.ApiClientModule;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.maintab.tabs.topic.views.folderlist.model.TopicFolderSettingModel;
import com.tosslab.jandi.app.utils.ColoredToast;
import com.tosslab.jandi.app.utils.analytics.AnalyticsUtil;
import com.tosslab.jandi.app.utils.analytics.AnalyticsValue;
import com.tosslab.jandi.app.views.listeners.SimpleTextWatcher;

import javax.inject.Inject;

import de.greenrobot.event.EventBus;
import rx.Completable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;


public class TopicFolderDialogFragment extends DialogFragment {

    @InjectExtra
    long folderId;
    @InjectExtra
    String folderName;
    @InjectExtra
    int seq;

    TextView tvFolderTitle;

    @Inject
    TopicFolderSettingModel topicFolderDialogModel;

    public static TopicFolderDialogFragment create(long folderId, String folderName, int seq) {
        Bundle args = new Bundle();
        args.putLong("folderId", folderId);
        args.putString("folderName", folderName);
        args.putInt("seq", seq);
        TopicFolderDialogFragment frag = new TopicFolderDialogFragment();
        frag.setArguments(args);
        return frag;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.fragment_folder_popup, null);
        tvFolderTitle = (TextView) view.findViewById(R.id.tv_popup_title);

        view.findViewById(R.id.tv_folder_rename).setOnClickListener(v -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolder_Rename);
            clickFolderRename();
            dismiss();
        });
        view.findViewById(R.id.tv_folder_delete).setOnClickListener(v -> {
            AnalyticsUtil.sendEvent(AnalyticsValue.Screen.TopicsTab, AnalyticsValue.Action.TopicFolder_Delete);
            clickFolderDelete();
            dismiss();
        });

        return new AlertDialog.Builder(getActivity(), R.style.JandiTheme_AlertDialog_FixWidth_280)
                .setView(view)
                .create();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Dart.inject(this, getArguments());
        DaggerTopicFolderDialogFragment_Component.builder()
                .build()
                .inject(this);
        initView();
    }

    void initView() {
        tvFolderTitle.setText(folderName);
    }

    void clickFolderRename() {
        showRenameFolderDialog(folderId, folderName, seq);
    }

    void clickFolderDelete() {
        showDeleteFolderDialog(folderId);
    }

    private void showRenameFolderDialog(long folderId, String name, int seq) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        RelativeLayout vgInputEditText = (RelativeLayout) LayoutInflater
                .from(getActivity()).inflate(R.layout.dialog_fragment_input_text, null);

        EditText input = (EditText) vgInputEditText.findViewById(R.id.et_dialog_input_text);
        ((TextView) vgInputEditText.findViewById(R.id.tv_popup_title)).setText(R.string.jandi_folder_rename);

        input.setText(name);
        input.setHint(R.string.jandi_title_name);
        input.setSelection(name.length());

        builder.setView(vgInputEditText)
                .setPositiveButton(getActivity().getString(R.string.jandi_confirm), (dialog, which) -> {
                    renameFolder(folderId, input.getText().toString().trim(), seq);
                })
                .setNegativeButton(getActivity().getString(R.string.jandi_cancel), null)
                .setOnCancelListener(dialog -> TopicFolderDialogFragment.this.dismiss());
        AlertDialog alertDialog = builder.create();
        alertDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);

        input.addTextChangedListener(new SimpleTextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() <= 0 || TextUtils.equals(folderName, s)) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
            }
        });
    }

    private void showDeleteFolderDialog(long folderId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(),
                R.style.JandiTheme_AlertDialog_FixWidth_300);

        builder.setMessage(R.string.jandi_folder_ask_delete)
                .setPositiveButton(getActivity().getString(R.string.jandi_confirm), (dialog, which) -> {
                    deleteTopicFolder(folderId);
                })
                .setNegativeButton(getActivity().getString(R.string.jandi_cancel), null)
                .setOnCancelListener(dialog -> TopicFolderDialogFragment.this.dismiss());
        builder.show();
    }

    public void deleteTopicFolder(long folderId) {
        Completable.fromCallable(() -> {
            topicFolderDialogModel.deleteTopicFolder(folderId);
            FolderRepository.getInstance().deleteFolder(folderId);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> {
                    showDeleteFolderToast();
                    dismiss();
                }, Throwable::printStackTrace);
    }

    public void renameFolder(long folderId, String name, int seq) {
        Completable.fromCallable(() -> {
            topicFolderDialogModel.renameFolder(folderId, name, seq);
            FolderRepository.getInstance().updateFolderName(folderId, name);
            TeamInfoLoader.getInstance().refresh();
            EventBus.getDefault().post(new TopicFolderRefreshEvent());
            return true;
        }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::showRenameFolderToast, t -> {
                    if (t instanceof RetrofitException) {
                        RetrofitException e = (RetrofitException) t;
                        e.printStackTrace();
                        if (e.getResponseCode() == 40008) {
                            showFailedRenameFolderToast();
                        }
                    }
                });
    }

    public void showRenameFolderToast() {
        ColoredToast.show(JandiApplication.getContext().getString(R.string.jandi_folder_renamed));
    }

    public void showFailedRenameFolderToast() {
        ColoredToast.showWarning(JandiApplication.getContext().getString(R.string.jandi_folder_alread_has_name));
    }

    public void showDeleteFolderToast() {
        ColoredToast.show(JandiApplication.getContext().getString(R.string.jandi_folder_removed));
    }

    @dagger.Component(modules = ApiClientModule.class)
    interface Component {
        void inject(TopicFolderDialogFragment fragment);
    }
}
