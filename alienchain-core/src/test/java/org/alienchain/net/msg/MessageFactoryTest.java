/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg;

import static org.junit.Assert.assertNull;

import org.junit.Test;

public class MessageFactoryTest {

  @Test
    public void testNonExist() throws MessageException {
        MessageFactory factory = new MessageFactory();
        assertNull(factory.create((byte) 0xff, new byte[1]));
    }

  @Test(expected = MessageException.class)
    public void testWrongCodec() throws MessageException {
        MessageFactory factory = new MessageFactory();
        factory.create((byte) 0x01, new byte[1]);
    }
}
