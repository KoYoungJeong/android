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

package com.tosslab.jandi.app.push.gcm.receiver;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.tosslab.jandi.app.push.receiver.JandiPushIntentService;

import java.util.Map;

public class GcmPushReceiver extends FirebaseMessagingService {

    public static final String KEY_CUSTOM_CONTENT = "custom_content";
    private static final String TAG = "GcmPushReceiver";

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();

        Log.d(TAG, "onMessageReceived() called with: remoteMessage = [" + data.toString() + "]");
        if (data.containsKey(KEY_CUSTOM_CONTENT)) {
            String customContent = data.get(KEY_CUSTOM_CONTENT);
            JandiPushIntentService.startService(GcmPushReceiver.this, customContent);
        }
    }
}
