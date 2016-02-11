/*
 *    Copyright (C) 2015 Haruki Hasegawa
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.tosslab.jandi.app.lists.libs.advancerecyclerview.utils;

import android.support.v7.widget.RecyclerView;
import android.view.View;

public class CustomRecyclerViewUtils {

    public static RecyclerView.ViewHolder findChildViewHolderUnderWithTranslation(RecyclerView rv, float x, float y) {
        final View child = rv.findChildViewUnder(x, y);
        return (child != null) ? rv.getChildViewHolder(child) : null;
    }

    public static int getSynchronizedPosition(RecyclerView.ViewHolder holder) {
        int pos1 = holder.getLayoutPosition();
        int pos2 = holder.getAdapterPosition();
        if (pos1 == pos2) {
            return pos1;
        } else {
            return RecyclerView.NO_POSITION;
        }
    }

}
