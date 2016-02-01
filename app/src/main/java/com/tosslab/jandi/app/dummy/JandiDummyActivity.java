package com.tosslab.jandi.app.dummy;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by tee on 16. 1. 18..
 */

// 프로세스가 Kill될 경우 푸시를 받을 더미 액티비티
public class JandiDummyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        finish();
    }
}
