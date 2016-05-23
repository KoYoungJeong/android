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

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.gcm.GcmPubSub;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;
import com.tosslab.jandi.app.JandiConstantsForFlavors;
import com.tosslab.jandi.app.local.orm.repositories.PushTokenRepository;
import com.tosslab.jandi.app.network.models.PushToken;
import com.tosslab.jandi.app.push.PushTokenRegister;
import com.tosslab.jandi.app.utils.JandiPreference;
import com.tosslab.jandi.app.utils.logger.LogUtil;
import com.tosslab.jandi.app.utils.parse.PushUtil;

import java.io.IOException;

public class RegistrationIntentService extends IntentService {

    private static final String TAG = "RegIntentService";
    private static final String[] TOPICS = {"global"};

    public RegistrationIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        try {
            synchronized (TAG) {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token =
                        instanceID.getToken(JandiConstantsForFlavors.Push.GCM_SENDER_ID,
                                GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);
                LogUtil.e("tony", "token = " + token);
                subscribeTopics(token);
                // TODO: Implement this method to send any registration to your app's servers.
                PushTokenRepository.getInstance().upsertPushToken(new PushToken("gcm", token));
                PushTokenRegister.getInstance().updateToken();

                if (!JandiPreference.isParsePushRemoved()) {
                    PushUtil.unsubscribeParsePush();
                    JandiPreference.setParsePushRemoved(true);
                }
            }
        } catch (Exception e) {
            LogUtil.e(TAG, Log.getStackTraceString(e));
        }
    }

    private void subscribeTopics(String token) throws IOException {
        for (String topic : TOPICS) {
            GcmPubSub pubSub = GcmPubSub.getInstance(this);
            pubSub.subscribe(token, "/topics/" + topic, null);
        }
    }
}
