package com.tosslab.jandi.app.ui.share.multi.presenter;

import android.net.Uri;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.lists.entities.entitymanager.EntityManager;
import com.tosslab.jandi.app.network.models.ResLeftSideMenu;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.services.upload.FileUploadManager;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.ui.share.model.ShareModel_;
import com.tosslab.jandi.app.ui.share.multi.domain.FileShareData;
import com.tosslab.jandi.app.ui.share.multi.domain.ShareData;
import com.tosslab.jandi.app.ui.share.multi.domain.ShareTarget;
import com.tosslab.jandi.app.ui.share.multi.model.SharesDataModel;
import com.tosslab.jandi.app.ui.share.views.model.ShareSelectModel;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.file.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.file.ImageFilePath;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MultiSharePresenterImpl implements MultiSharePresenter {
    private final View view;
    private SharesDataModel sharesDataModel;
    private ShareTarget shareTarget;
    private ShareSelectModel shareSelectModel;
    private ShareModel shareModel;


    @Inject
    public MultiSharePresenterImpl(View view, SharesDataModel sharesDataModel) {
        this.view = view;
        this.sharesDataModel = sharesDataModel;
        shareTarget = new ShareTarget();
        this.shareModel = ShareModel_.getInstance_(JandiApplication.getContext());

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
                        ResLeftSideMenu leftSideMenu = shareModel.getLeftSideMenu(teamId);
                        shareModel.updateLeftSideMenu(leftSideMenu);
                    }

                    shareSelectModel = shareModel.getShareSelectModel(teamId);
                    subscriber.onNext(shareSelectModel);
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
        sharesDataModel.clear();
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
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(sharesDataModel::add, t -> view.moveIntro(), () -> {
                    ShareData item = sharesDataModel.getItem(0);
                    String fileName = getFileName(item.getData());
                    view.setFileTitle(fileName);
                    view.updateFiles();
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
    public void startShare(List<Pair<String, List<MentionObject>>> mentionInfos) {
        int dataSize = sharesDataModel.size();

        for (int idx = 0; idx < dataSize; idx++) {
            ShareData item = sharesDataModel.getItem(idx);
            Pair<String, List<MentionObject>> stringListPair = mentionInfos.get(idx);
            FileUploadDTO object = new FileUploadDTO(item.getData(), getFileName(item.getData()), shareTarget.getRoomId(), stringListPair.first);
            object.setTeamId(shareTarget.getTeamId());
            object.setMentions(stringListPair.second);
            FileUploadManager.getInstance(JandiApplication.getContext()).add(object);
        }
    }
}
