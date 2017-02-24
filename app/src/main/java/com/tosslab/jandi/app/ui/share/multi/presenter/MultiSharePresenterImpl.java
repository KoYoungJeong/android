package com.tosslab.jandi.app.ui.share.multi.presenter;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Pair;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.manager.restapiclient.restadapterfactory.builder.RetrofitBuilder;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.services.upload.FileUploadManager;
import com.tosslab.jandi.app.services.upload.to.FileUploadDTO;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.MentionControlViewModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.model.SearchMemberModel;
import com.tosslab.jandi.app.ui.commonviewmodels.mention.vo.ResultMentionsVO;
import com.tosslab.jandi.app.ui.file.upload.preview.adapter.FileUploadThumbAdapter;
import com.tosslab.jandi.app.ui.share.model.ShareModel;
import com.tosslab.jandi.app.ui.share.multi.domain.FileShareData;
import com.tosslab.jandi.app.ui.share.multi.domain.ShareData;
import com.tosslab.jandi.app.ui.share.multi.domain.ShareTarget;
import com.tosslab.jandi.app.ui.share.multi.model.ShareAdapterDataModel;
import com.tosslab.jandi.app.utils.file.FileUtil;
import com.tosslab.jandi.app.utils.file.GoogleImagePickerUtil;
import com.tosslab.jandi.app.utils.file.ImageFilePath;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func0;
import rx.schedulers.Schedulers;

public class MultiSharePresenterImpl implements MultiSharePresenter {
    private final View view;
    ShareTarget shareTarget;
    TeamInfoLoader teamInfoLoader;
    List<String> comments;
    private ShareAdapterDataModel shareAdapterDataModel;
    private ShareModel shareModel;
    private int lastPageIndex = 0;

    @Inject
    public MultiSharePresenterImpl(View view,
                                   ShareModel shareModel,
                                   ShareAdapterDataModel shareAdapterDataModel) {
        this.view = view;
        this.shareModel = shareModel;
        this.shareAdapterDataModel = shareAdapterDataModel;
        shareTarget = new ShareTarget();
        comments = new ArrayList<>();

    }

    @Override
    public void onRoomChange() {
        view.callRoomSelector(shareTarget.getTeamId());
    }

    @Override
    public void initShareTarget() {
        long teamId = TeamInfoLoader.getInstance().getTeamId();
        if (teamId <= 0) {
            view.setTeamDefaultName();
            return;
        }
        onSelectTeam(teamId);
    }


    @Override
    public void onSelectTeam(long teamId) {
        shareTarget.setTeamId(teamId);
        Observable
                .defer(() -> {
                    teamInfoLoader = shareModel.getTeamInfoLoader(teamId);
                    shareModel.refreshPollList(teamId);
                    return Observable.just(teamInfoLoader);
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(shareSelectModel -> {
                    shareTarget.setRoomId(-1);
                    shareTarget.setEntityId(-1);
                    String teamName = shareSelectModel.getTeamName();
                    view.setTeamId(teamId);
                    view.setTeamName(teamName);
                    view.setRoomName("");
                    view.setMentionInfo(shareTarget.getTeamId(), shareTarget.getRoomId());
                }, t -> {
                    t.printStackTrace();
                    view.moveIntro();
                });

    }

    @Override
    public void initShareData(List<String> uris) {
        view.showProgress();
        Observable<List<ShareData>> dataObservable = Observable
                .create((Observable.OnSubscribe<FileShareData>) subscriber -> {
                    for (String uri : uris) {
                        Uri paredUri = Uri.parse(uri);
                        String path = ImageFilePath.getPath(JandiApplication.getContext(), paredUri);
                        if (!TextUtils.isEmpty(path)) {
                            subscriber.onNext(new FileShareData(path));
                        }
                    }
                    subscriber.onCompleted();
                })
                .onBackpressureBuffer()
                .subscribeOn(Schedulers.io())
                .map(fileShareData -> {
                    String path = fileShareData.getData();
                    if (path.startsWith("http")) {
                        String downloadDir = FileUtil.getDownloadPath();
                        String downloadName = GoogleImagePickerUtil.getWebImageName();
                        try {
                            File file = GoogleImagePickerUtil
                                    .downloadFile(null, path, downloadDir, downloadName);
                            return new FileShareData(file.getAbsolutePath());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return fileShareData;
                })
                .observeOn(Schedulers.computation())
                .doOnNext(shareData -> comments.add(""))
                .collect((Func0<List<ShareData>>) ArrayList::new, List::add)
                .share();

        dataObservable.observeOn(AndroidSchedulers.mainThread())
                .subscribe(shareDatas -> {
                    shareAdapterDataModel.clear();
                    shareAdapterDataModel.addAll(shareDatas);
                    view.updateFiles(shareAdapterDataModel.size());

                }, t -> view.moveIntro(), () -> {
                    view.dismissProgress();
                    FileShareData item = (FileShareData) shareAdapterDataModel.getShareData(0);
                    if (item == null) {
                        return;
                    }
                    String fileName = item.getFileName();
                    view.setFileName(fileName);
                });

        dataObservable
                .observeOn(Schedulers.computation())
                .filter(its -> its.size() > 1)
                .concatMap(Observable::from)
                .map(ShareData::getData)
                .map(FileUploadThumbAdapter.FileThumbInfo::create)
                .collect((Func0<ArrayList<FileUploadThumbAdapter.FileThumbInfo>>) ArrayList::new, List::add)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(view::setFileThumbInfos);

    }

    @Override
    public void onSelectRoom(long roomId) {
        shareTarget.setRoomId(roomId);
        String entityName = teamInfoLoader.getName(roomId);
        view.setRoomName(entityName);
        view.setMentionInfo(shareTarget.getTeamId(), shareTarget.getRoomId());
    }

    @Override
    public void startShare() {
        if (shareTarget.getTeamId() <= 0 || shareTarget.getRoomId() <= 0) {
            view.showSelectRoomToast();
            return;
        }

        Observable.just(shareTarget.getRoomId())
                .map(roomId -> {
                    if (teamInfoLoader.isTopic(roomId)) {
                        return roomId;
                    } else {
                        long chatId = teamInfoLoader.getChatId(roomId);
                        if (chatId <= 0) {
                            try {
                                return new ChatApi(RetrofitBuilder.getInstance()).createChat(teamInfoLoader.getTeamId(), roomId).getId();
                            } catch (RetrofitException e) {
                                e.printStackTrace();
                            }
                        } else {
                            return chatId;
                        }
                    }
                    return -1L;
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(roomId -> {
                    SearchMemberModel model = new SearchMemberModel();
                    model.refreshSelectableMembers(shareTarget.getTeamId(),
                            Arrays.asList(shareTarget.getRoomId()),
                            MentionControlViewModel.MENTION_TYPE_FILE_COMMENT,
                            map -> {
                                Observable.range(0, shareAdapterDataModel.size())
                                        .subscribe(idx -> {
                                            FileShareData item = (FileShareData) shareAdapterDataModel.getShareData(idx);
                                            if (item == null) {
                                                return;
                                            }
                                            ResultMentionsVO mentionInfoObject = MentionControlViewModel.getMentionInfoObject(comments.get(idx), map);
                                            List<MentionObject> mentions = mentionInfoObject.getMentions();
                                            String message = mentionInfoObject.getMessage();
                                            Pair<String, List<MentionObject>> stringListPair = new Pair<>(message, mentions);
                                            FileUploadDTO object = new FileUploadDTO(item.getData(), item.getFileName(), roomId, stringListPair.first);
                                            object.setTeamId(shareTarget.getTeamId());
                                            object.setMentions(stringListPair.second);
                                            FileUploadManager.getInstance().add(object);
                                        }, t -> {
                                        });

                                view.moveRoom(shareTarget.getTeamId(), shareTarget.getRoomId());
                            });
                });


    }

    @Override
    public void onFilePageChanged(int position, String comment) {
        FileShareData item = (FileShareData) shareAdapterDataModel.getShareData(position);
        if (item == null) {
            return;
        }
        String fileName = item.getFileName();
        comments.set(lastPageIndex, comment);
        view.setCommentText(comments.get(position));
        view.setFileName(fileName);
        view.setUpScrollButton(position, shareAdapterDataModel.size());
        lastPageIndex = position;

    }

    @Override
    public void updateComment(int currentItem, String comment) {
        if (comments.size() <= lastPageIndex) {
            comments.add(comment);
        } else {
            comments.set(lastPageIndex, comment);
        }
    }

    @Override
    public void changeFileName(int position, String fileName) {
        FileShareData fileShareData = (FileShareData) shareAdapterDataModel.getShareData(position);
        fileShareData.setFileName(fileName);
    }

    @Override
    public String getFileName(int position) {
        FileShareData fileShareData = (FileShareData) shareAdapterDataModel.getShareData(position);
        return fileShareData.getFileName();
    }

}
