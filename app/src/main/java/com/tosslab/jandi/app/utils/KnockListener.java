package com.tosslab.jandi.app.utils;

import com.tosslab.jandi.app.utils.logger.LogUtil;

/**
 * Created by tonyjs on 16. 4. 1..
 */
public class KnockListener {

    private int expectKnockCount;
    private long expectKnockedIn;
    private int knockedCount = 0;
    private long firstKnockedTime;
    private OnKnockedListener onKnockedListener;

    private KnockListener() {
    }

    public static KnockListener create() {
        return new KnockListener();
    }

    public KnockListener expectKnockCount(int expectKnockedCount) {
        this.expectKnockCount = expectKnockedCount;
        return this;
    }

    public KnockListener expectKnockedIn(long expectKnockedIn) {
        this.expectKnockedIn = expectKnockedIn;
        return this;
    }

    public KnockListener onKnocked(OnKnockedListener onKnockedListener) {
        this.onKnockedListener = onKnockedListener;
        return this;
    }

    public void knock() {
        if (knockedCount >= expectKnockCount
                && System.currentTimeMillis() - firstKnockedTime < expectKnockedIn) {
            firstKnockedTime = 0;
            knockedCount = 0;
            if (onKnockedListener != null) {
                onKnockedListener.onKnocked();
            }
            return;
        }

        if (firstKnockedTime == 0) {
            firstKnockedTime = System.currentTimeMillis();
        } else if (System.currentTimeMillis() - firstKnockedTime >= expectKnockedIn) {
            firstKnockedTime = 0;
            knockedCount = 0;
        } else {
            knockedCount++;
        }

    }

    public interface OnKnockedListener {
        void onKnocked();
    }
}
