/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.p2p.handshake.v2;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.alienchain.util.Bytes;
import org.alienchain.util.TimeUtil;

public class InitMessageTest {

  @Test
    public void testCodec() {
        byte[] secret = Bytes.random(32);
        long timestamp = TimeUtil.currentTimeMillis();

        InitMessage msg = new InitMessage(secret, timestamp);
        assertArrayEquals(secret, msg.getSecret());
        assertEquals(timestamp, msg.getTimestamp());

        msg = new InitMessage(msg.getBody());
        assertArrayEquals(secret, msg.getSecret());
        assertEquals(timestamp, msg.getTimestamp());
    }
}
