/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.consensus;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.alienchain.consensus.Vote;
import org.alienchain.consensus.VoteType;
import org.alienchain.crypto.Key;

public class VoteMessageTest {
  @Test
    public void testSerialization() {
        Key key = new Key();
        VoteType type = VoteType.COMMIT;
        long height = 1;
        int view = 2;
        Vote vote = Vote.newReject(type, height, view);
        vote.sign(key);
        assertTrue(vote.validate());

        VoteMessage msg = new VoteMessage(vote);
        VoteMessage msg2 = new VoteMessage(msg.getBody());
        Vote vote2 = msg2.getVote();
        assertEquals(type, vote2.getType());
        assertEquals(height, vote2.getHeight());
        assertEquals(view, vote2.getView());
        assertTrue(vote2.validate());

        assertArrayEquals(key.toAddress(), vote2.getSignature().getAddress());
    }
}
