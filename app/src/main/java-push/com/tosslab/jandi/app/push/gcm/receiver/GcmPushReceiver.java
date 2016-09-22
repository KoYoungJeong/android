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

import com.google.android.gms.gcm.GcmListenerService;
import com.tosslab.jandi.app.push.receiver.JandiPushIntentService;
import com.tosslab.jandi.app.utils.logger.LogUtil;

public class GcmPushReceiver extends GcmListenerService {

    private static final String TAG = "GcmPushReceiver";

    @Override
    public void onMessageReceived(String from, Bundle data) {
        LogUtil.i(TAG, "onMessageReceived");
        String dataPayload = data.getString("data");
        if (!TextUtils.isEmpty(dataPayload)) {
            // Old type Push
            return;
        }

        LogUtil.d(TAG, "called with: " + "from = [" + from + "], data = [" + data + "]");

        String customContent = data.getString("custom_content");
        JandiPushIntentService.startService(GcmPushReceiver.this, customContent);

    }

}
