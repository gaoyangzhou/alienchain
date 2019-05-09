/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.p2p;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.alienchain.core.Amount.Unit.NANO_ALX;

import org.junit.Test;
import org.alienchain.Network;
import org.alienchain.core.Amount;
import org.alienchain.core.Transaction;
import org.alienchain.core.TransactionType;
import org.alienchain.crypto.Key;
import org.alienchain.util.Bytes;
import org.alienchain.util.TimeUtil;

public class TransactionMessageTest {
  @Test
    public void testSerialization() {
        Network network = Network.DEVNET;
        TransactionType type = TransactionType.TRANSFER;
        byte[] to = Bytes.random(20);
        Amount value = NANO_ALX.of(2);
        Amount fee = NANO_ALX.of(50_000_000L);
        long nonce = 1;
        long timestamp = TimeUtil.currentTimeMillis();
        byte[] data = Bytes.of("data");

        Transaction tx = new Transaction(network, type, to, value, fee, nonce, timestamp, data);
        tx.sign(new Key());

        TransactionMessage msg = new TransactionMessage(tx);
        TransactionMessage msg2 = new TransactionMessage(msg.getBody());
        assertThat(msg2.getTransaction().getHash(), equalTo(tx.getHash()));
    }
}
