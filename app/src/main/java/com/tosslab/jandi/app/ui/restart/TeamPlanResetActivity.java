package com.tosslab.jandi.app.ui.restart;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;

import com.tosslab.jandi.app.R;
import com.tosslab.jandi.app.local.orm.repositories.info.InitialInfoRepository;
import com.tosslab.jandi.app.services.socket.JandiSocketService;
import com.tosslab.jandi.app.ui.intro.IntroActivity;

/**
 * Created by tee on 2017. 3. 2..
 */

public class TeamPlanResetActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
        new AlertDialog.Builder(this)
                .setMessage(R.string.common_changedplan_restart_title)
                .setPositiveButton(R.string.common_changedplan_restart_desc, null)
                .setOnDismissListener(dialog -> {
                    // 기존 정보 삭제
                    InitialInfoRepository.getInstance().clear();
                    // 서비스 종료
                    JandiSocketService.stopService(this);
                    // 앱 재실행
                    Intent intent = new Intent(this, IntroActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                })
                .create()
                .show();
    }
}
