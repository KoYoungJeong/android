package com.tosslab.jandi.app.ui.share.model;

import android.app.ProgressDialog;
import android.net.Uri;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.chat.ChatApi;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.file.FileUploadApi;
import com.tosslab.jandi.app.network.models.ResMessages;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.ResUploadedFile;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.messages.ReqTextMessage;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.file.ImageFilePath;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import retrofit2.Call;
import rx.Observable;

public class ShareModel {

    private Lazy<RoomsApi> roomsApi;
    private Lazy<TeamApi> teamApi;
    private Lazy<StartApi> startApi;
    private Lazy<PollApi> pollApi;
    private Lazy<ChatApi> chatApi;

    @Inject
    public ShareModel(Lazy<RoomsApi> roomsApi,
                      Lazy<TeamApi> teamApi,
                      Lazy<StartApi> startApi,
                      Lazy<PollApi> pollApi,
                      Lazy<ChatApi> chatApi) {
        this.roomsApi = roomsApi;
        this.teamApi = teamApi;
        this.startApi = startApi;
        this.pollApi = pollApi;
        this.chatApi = chatApi;
    }

    public ResTeamDetailInfo.InviteTeam getTeamInfoById(long teamId) throws RetrofitException {
        return teamApi.get().getTeamInfo(teamId);
    }

    public List<ResMessages.Link> sendMessage(long teamId, long entityId, String messageText, List<MentionObject> mention) throws RetrofitException {

        ReqTextMessage reqMessage = new ReqTextMessage(messageText, mention);

        return roomsApi.get().sendMessage(teamId, entityId, reqMessage);
    }

    public String getImagePath(String uriString) {
        Uri uri = Uri.parse(uriString);
        return ImageFilePath.getPath(JandiApplication.getContext(), uri);
    }

    public ResUploadedFile uploadFile(File imageFile, String titleText, String commentText,
                                      long teamId, long entityId, ProgressDialog progressDialog,
                                      boolean isPublicTopic, List<MentionObject> mentions) throws IOException {
        File uploadFile = new File(imageFile.getAbsolutePath());
        String permissionCode = (isPublicTopic) ? "744" : "740";

        Call<ResUploadedFile> uploadCall = new FileUploadApi().uploadFile(titleText, entityId, permissionCode, teamId, commentText, mentions, uploadFile, callback -> callback
                .distinctUntilChanged()
                .subscribe(it -> {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.setMax(100);
                        progressDialog.setProgress(it);
                    }
                }, t -> {
                }));

        progressDialog.setOnCancelListener(dialog -> uploadCall.cancel());
        return uploadCall.execute().body();

    }

    public String getFilePath(String uriString) {
        return Uri.parse(uriString).getPath();
    }

    public boolean hasLeftSideMenu(long teamId) {
        return InitialInfoRepository.getInstance().hasInitialInfo(teamId);
    }

    public InitialInfo getInitialInfo(long teamId) throws RetrofitException {
        return startApi.get().getInitializeInfo(teamId);
    }

    public boolean updateInitialInfo(InitialInfo initialInfo) {
        return InitialInfoRepository.getInstance().upsertInitialInfo(initialInfo);
    }

    public TeamInfoLoader getTeamInfoLoader(long teamId) {
        return TeamInfoLoader.getInstance(teamId);
    }

    public void refreshPollList(long teamId) {
        try {
            PollRepository.getInstance().clearAll();
            ResPollList resPollList = pollApi.get().getPollList(teamId, 50);
            List<Poll> onGoing = resPollList.getOnGoing();
            if (onGoing == null) {
                onGoing = new ArrayList<>();
            }
            List<Poll> finished = resPollList.getFinished();
            if (finished == null) {
                finished = new ArrayList<>();
            }
            Observable.merge(Observable.from(onGoing), Observable.from(finished))
                    .toList()
                    .subscribe(polls -> PollRepository.getInstance().upsertPollList(polls),
                            Throwable::printStackTrace);
        } catch (RetrofitException retrofitError) {
            retrofitError.printStackTrace();
        }
    }

    public long createChat(long teamId, long entityId) throws RetrofitException {
        return chatApi.get().createChat(teamId, entityId).id;
    }
}
