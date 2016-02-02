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

package com.tosslab.jandi.app.libraries.advancerecyclerview.provider;

public abstract class AbstractExpandableDataProvider {

    public abstract int getGroupCount();

    public abstract int getChildCount(int groupPosition);

    public abstract GroupData getGroupItem(int groupPosition);

    public abstract ChildData getChildItem(int groupPosition, int childPosition);


    public static abstract class GroupData {

        public abstract boolean isPinnedToSwipeLeft();

        public abstract long getGroupId();

    }

    public static abstract class ChildData {

        public abstract long getChildId();

    }

}
