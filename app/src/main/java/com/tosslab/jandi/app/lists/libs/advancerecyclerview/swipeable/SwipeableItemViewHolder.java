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

package com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable;

import android.view.View;

/**
 * Interface which provides required information for swiping item.
 * <p>
 * Implement this interface on your sub-class of the {@link android.support.v7.widget.RecyclerView.ViewHolder}.
 */
public interface SwipeableItemViewHolder {
    /**
     * Gets the state flags value for swiping item
     *
     * @return Bitwise OR of these flags;
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#STATE_FLAG_SWIPING}
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#STATE_FLAG_IS_ACTIVE}
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#STATE_FLAG_IS_UPDATED}
     */
    int getSwipeStateFlags();

    /**
     * Sets the state flags value for swiping item
     *
     * @param flags Bitwise OR of these flags;
     *              - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#STATE_FLAG_SWIPING}
     *              - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#STATE_FLAG_IS_ACTIVE}
     *              - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#STATE_FLAG_IS_UPDATED}
     */
    void setSwipeStateFlags(int flags);

    /**
     * Gets the result code of swiping item.
     *
     * @return Result code. One of these values;
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_LEFT}
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_RIGHT}
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_CANCELED}
     */
    int getSwipeResult();

    /**
     * Sets the result code of swiping item.
     *
     * @param result Result code. One of these values;
     *               - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_NONE}
     *               - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_LEFT}
     *               - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_SWIPED_RIGHT}
     *               - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#RESULT_CANCELED}
     */
    void setSwipeResult(int result);

    /**
     * Gets the reaction type of after swiping item.
     *
     * @return After-reaction type. One of these values;
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_DEFAULT}
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_REMOVE_ITEM}
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION}
     */
    int getAfterSwipeReaction();

    /**
     * Sets the reaction type of after swiping item.
     *
     * @param reaction After-reaction type. One of these values;
     *                 - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_DEFAULT}
     *                 - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_REMOVE_ITEM}
     *                 - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#AFTER_SWIPE_REACTION_MOVE_TO_SWIPED_DIRECTION}
     */
    void setAfterSwipeReaction(int reaction);

    /**
     * Gets the item swipe amount.
     *
     * @return Item swipe amount. Generally the range is [-1.0 .. 1.0], but these special values may be returned;
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_LEFT}
     * - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_RIGHT}
     */
    float getSwipeItemSlideAmount();

    /**
     * Sets the item swipe amount.
     *
     * @param amount Item swipe amount. Generally the range is [-1.0 .. 1.0], but these special values can be accepted;
     *               - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_LEFT}
     *               - {@link com.tosslab.jandi.app.lists.libs.advancerecyclerview.swipeable.RecyclerViewSwipeManager#OUTSIDE_OF_THE_WINDOW_RIGHT}
     */
    void setSwipeItemSlideAmount(float amount);

    /**
     * Gets the maximum item left swipe amount.
     *
     * @return Item swipe amount. Generally the range is [-1.0 .. 1.0]
     */
    float getMaxLeftSwipeAmount();

    /**
     * Sets the maximum item right swipe amount.
     *
     * @param amount Item swipe amount. Generally the range is [-1.0 .. 1.0]
     */
    void setMaxLeftSwipeAmount(float amount);

    /**
     * Gets the maximum item right swipe amount.
     *
     * @return Item swipe amount. Generally the range is [-1.0 .. 1.0]
     */
    float getMaxRightSwipeAmount();

    /**
     * Sets the maximum item right swipe amount.
     *
     * @param amount Item swipe amount. Generally the range is [-1.0 .. 1.0]
     */
    void setMaxRightSwipeAmount(float amount);

    /**
     * Gets the container view for the swipeable area.
     *
     * @return The container view instance.
     */
    View getSwipeableContainerView();
}
