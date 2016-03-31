package com.tosslab.jandi.app.ui.share.multi.presenter;

import android.net.Uri;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.services.upload.FileUploadManager;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.ui.share.model.ShareModel_;
import com.tosslab.jandi.app.ui.share.multi.domain.FileShareData;
import com.tosslab.jandi.app.ui.share.multi.domain.ShareData;
import com.tosslab.jandi.app.ui.share.multi.domain.ShareTarget;
import com.tosslab.jandi.app.ui.share.multi.model.ShareAdapterDataModel;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.file.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.file.ImageFilePath;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MultiSharePresenterImpl implements MultiSharePresenter {
    private final View view;
    ShareTarget shareTarget;
    ShareSelectModel shareSelectModel;
    List<String> comments;
    private ShareAdapterDataModel shareAdapterDataModel;
    private ShareModel shareModel;
    private int lastPageIndex = 0;

    @Inject
    public MultiSharePresenterImpl(View view, ShareAdapterDataModel shareAdapterDataModel) {
        this.view = view;
        this.shareAdapterDataModel = shareAdapterDataModel;
        shareTarget = new ShareTarget();
        this.shareModel = ShareModel_.getInstance_(JandiApplication.getContext());
        comments = new ArrayList<>();

    }

    @Override
    public void onRoomChange() {
        view.callRoomSelector(shareTarget.getTeamId());

    }

    @Override
    public void initShareTarget() {
        long teamId = EntityManager.getInstance().getTeamId();
        onSelectTeam(teamId);
    }

    @Override
    public void onSelectTeam(long teamId) {
        shareTarget.setTeamId(teamId);
        Observable
                .create((Subscriber<? super ShareSelectModel> subscriber) -> {
                    if (!shareModel.hasLeftSideMenu(shareTarget.getTeamId())) {
                        try {
                            ResLeftSideMenu leftSideMenu = shareModel.getLeftSideMenu(teamId);
                            shareModel.updateLeftSideMenu(leftSideMenu);
                        } catch (RetrofitException e) {
                            subscriber.onError(e);
                        }
                    }

                    shareSelectModel = shareModel.getShareSelectModel(teamId);
                    subscriber.onNext(shareSelectModel);
                    subscriber.onCompleted();

                    subscriber.onCompleted();
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shareSelectModel -> {
                    shareTarget.setRoomId(shareSelectModel.getDefaultTopicId());
                    String roomName = shareSelectModel.getEntityById(shareTarget.getRoomId()).getName();
                    String teamName = shareSelectModel.getTeamName();
                    view.setTeamName(teamName);
                    view.setRoomName(roomName);
                    view.setMentionInfo(shareTarget.getTeamId(), shareTarget.getRoomId());
                }, t -> {
                    t.printStackTrace();
                    view.moveIntro();
                });

    }

    @Override
    public void initShareData(List<String> uris) {
        shareAdapterDataModel.clear();
        Observable.from(uris)
                .observeOn(Schedulers.io())
                .map(uri -> {
                    Uri paredUri = Uri.parse(uri);
                    String path = ImageFilePath.getPath(JandiApplication.getContext(), paredUri);
                    return new FileShareData(path);
                })
                .map(fileShareData -> {
                    String path = fileShareData.getData();
                    if (path.startsWith("http")) {
                        String downloadDir = FileUtil.getDownloadPath();
                        String downloadName = GoogleImagePickerUtil.getWebImageName();
                        try {
                            File file = GoogleImagePickerUtil
                                    .downloadFile(JandiApplication.getContext(), null, path, downloadDir, downloadName);
                            return new FileShareData(file.getAbsolutePath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return fileShareData;

                })
                .observeOn(Schedulers.computation())
                .doOnNext(shareData -> comments.add(""))
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shareAdapterDataModel::add, t -> view.moveIntro(), () -> {
                    ShareData item = shareAdapterDataModel.getShareData(0);
                    String fileName = getFileName(item.getData());
                    view.setFileTitle(fileName);
                    view.updateFiles(shareAdapterDataModel.size());
                });

    }

    private String getFileName(String data) {
        return new File(data).getName();
    }

    @Override
    public void onSelectRoom(long roomId) {
        shareTarget.setRoomId(roomId);
        String entityName = shareSelectModel.getEntityById(roomId).getName();
        view.setRoomName(entityName);
        view.setMentionInfo(shareTarget.getTeamId(), shareTarget.getRoomId());
    }

    @Override
    public void startShare() {

        Observable.range(0, shareAdapterDataModel.size())
                .subscribe(idx -> {
                    ShareData item = shareAdapterDataModel.getShareData(idx);
                    ResultMentionsVO mentionInfoObject = MentionControlViewModel.getMentionInfoObject(shareTarget.getTeamId(), shareTarget.getRoomId(), comments.get(idx), MentionControlViewModel.MENTION_TYPE_FILE_COMMENT);
                    List<MentionObject> mentions = mentionInfoObject.getMentions();
                    String message = mentionInfoObject.getMessage();
                    Pair<String, List<MentionObject>> stringListPair = new Pair<>(message, mentions);
                    FileUploadDTO object = new FileUploadDTO(item.getData(), getFileName(item.getData()), shareTarget.getRoomId(), stringListPair.first);
                    object.setTeamId(shareTarget.getTeamId());
                    object.setMentions(stringListPair.second);
                    FileUploadManager.getInstance().add(object);
                }, t -> {});

        view.moveRoom(shareTarget.getTeamId(), shareTarget.getRoomId());
    }

    @Override
    public void onFilePageChanged(int position, String comment) {
        ShareData item = shareAdapterDataModel.getShareData(position);
        String fileName = getFileName(item.getData());
        comments.set(lastPageIndex, comment);
        view.setCommentText(comments.get(position));
        view.setFileTitle(fileName);
        view.setUpScrollButton(position, shareAdapterDataModel.size());
        lastPageIndex = position;

    }

    @Override
    public void updateComment(int currentItem, String comment) {
        comments.set(lastPageIndex, comment);
    }
}
