package com.tosslab.jandi.app.ui.poll.util;

import com.tosslab.jandi.app.network.models.poll.Poll;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tonyjs on 16. 6. 22..
 */
public class VoteManager {

    private List<Poll.Item> selectedItems = new ArrayList<>();

    private Poll poll;

    private VoteManager(Poll poll) {
        this.poll = poll;
    }

    public static VoteManager create(Poll poll) {
        return new VoteManager(poll);
    }

    public void select(Poll.Item item) {
        if (!poll.isMultipleChoice() && selectedItems.size() > 0) {
            return;
        }

        selectedItems.add(item);
    }


}
