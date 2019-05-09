/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.consensus;

import org.alienchain.consensus.Vote;
import org.alienchain.net.msg.Message;
import org.alienchain.net.msg.MessageCode;

public class VoteMessage extends Message {

    private final Vote vote;

    public VoteMessage(Vote vote) {
        super(MessageCode.BFT_VOTE, null);

        this.vote = vote;

        // FIXME: consider wrapping by simple codec
        this.body = vote.toBytes();
    }

    public VoteMessage(byte[] body) {
        super(MessageCode.BFT_VOTE, null);

        this.vote = Vote.fromBytes(body);

        this.body = body;
    }

    public Vote getVote() {
        return vote;
    }

    @Override
    public String toString() {
        return "BFTVoteMessage: " + vote;
    }
}
