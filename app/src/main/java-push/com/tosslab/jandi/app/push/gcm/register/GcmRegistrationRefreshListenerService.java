/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tosslab.jandi.app.push.gcm.register;

import android.util.Log;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;
import com.google.firebase.messaging.FirebaseMessaging;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.push.PushTokenRegister;
import com.tosslab.jandi.app.utils.JandiPreference;

import java.io.IOException;

public class GcmRegistrationRefreshListenerService extends FirebaseInstanceIdService {

    private static final String[] TOPICS = {"global"};
    private static final String TAG = "GcmRegistrationRefreshL";

    @Override
    public void onTokenRefresh() {
        // Fetch updated Instance ID token and notify our app's server of any changes (if applicable).
        try {
            String token = FirebaseInstanceId.getInstance().getToken();
            Log.d(TAG, "onTokenRefresh() called Token : " + token);
            subscribeTopics();
            PushTokenRepository.getInstance().upsertPushToken(new PushToken("gcm", token));
            JandiPreference.setLatestFcmTokenUpdate(System.currentTimeMillis());
            PushTokenRegister.getInstance().updateToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void subscribeTopics() throws IOException {
        for (String topic : TOPICS) {
            FirebaseMessaging.getInstance().subscribeToTopic(topic);
        }
    }
}