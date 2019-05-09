/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.consensus;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collections;

import org.junit.Test;
import org.alienchain.consensus.Proof;

public class NewViewMessageTest {
  @Test
    public void testSerialization() {
        int height = 1;
        int view = 1;
        Proof proof = new Proof(height, view, Collections.emptyList());

        NewViewMessage msg = new NewViewMessage(proof);
        NewViewMessage msg2 = new NewViewMessage(msg.getBody());
        assertEquals(height, msg2.getHeight());
        assertEquals(view, msg2.getView());
        assertTrue(msg2.getProof().getVotes().isEmpty());
    }
}
