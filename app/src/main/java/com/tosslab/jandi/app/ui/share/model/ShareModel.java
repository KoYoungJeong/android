package com.tosslab.jandi.app.ui.share.model;

import android.app.ProgressDialog;
import android.net.Uri;

import com.tosslab.jandi.app.JandiApplication;
import com.tosslab.jandi.app.local.orm.repositories.PollRepository;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.network.client.MessageManipulator;
import com.tosslab.jandi.app.network.client.MessageManipulator_;
import com.tosslab.jandi.app.network.client.rooms.RoomsApi;
import com.tosslab.jandi.app.network.client.start.StartApi;
import com.tosslab.jandi.app.network.client.teams.TeamApi;
import com.tosslab.jandi.app.network.client.teams.poll.PollApi;
import com.tosslab.jandi.app.network.dagger.DaggerApiClientComponent;
import com.tosslab.jandi.app.network.exception.RetrofitException;
import com.tosslab.jandi.app.network.file.FileUploadApi;
import com.tosslab.jandi.app.network.models.ResCommon;
import com.tosslab.jandi.app.network.models.ResPollList;
import com.tosslab.jandi.app.network.models.ResTeamDetailInfo;
import com.tosslab.jandi.app.network.models.ResUploadedFile;
import com.tosslab.jandi.app.network.models.commonobject.MentionObject;
import com.tosslab.jandi.app.network.models.poll.Poll;
import com.tosslab.jandi.app.network.models.start.InitialInfo;
import com.tosslab.jandi.app.team.TeamInfoLoader;
import com.tosslab.jandi.app.utils.file.ImageFilePath;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.EBean;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.Lazy;
import retrofit2.Call;
import rx.Observable;

@EBean
public class ShareModel {

    @Inject
    Lazy<RoomsApi> roomsApi;
    @Inject
    Lazy<TeamApi> teamApi;
    @Inject
    Lazy<StartApi> startApi;
    @Inject
    Lazy<PollApi> pollApi;

    @AfterInject
    void initObject() {
        DaggerApiClientComponent.create().inject(this);
    }

    public ResTeamDetailInfo.InviteTeam getTeamInfoById(long teamId) throws RetrofitException {
        return teamApi.get().getTeamInfo(teamId);
    }

    public ResCommon sendMessage(long teamId, long entityId, int entityType, String messageText, List<MentionObject> mention) throws RetrofitException {

        MessageManipulator messageManipulator = MessageManipulator_.getInstance_(JandiApplication.getContext());

        messageManipulator.initEntity(entityType, entityId);

        messageManipulator.setTeamId(teamId);

        return messageManipulator.sendMessage(messageText, mention);

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
}
