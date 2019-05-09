/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.util;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.alienchain.core.Amount.Unit.ALX;
import static org.alienchain.core.Amount.ZERO;

import java.util.Arrays;

import org.junit.Test;
import org.alienchain.Network;
import org.alienchain.core.Amount;
import org.alienchain.core.Transaction;
import org.alienchain.core.TransactionResult;
import org.alienchain.core.TransactionType;
import org.alienchain.crypto.Hash;
import org.alienchain.crypto.Key;

public class MerkleUtilTest {
  @Test
    public void testComputeTransactionsRoot() {
        Network network = Network.DEVNET;
        TransactionType type = TransactionType.TRANSFER;
        byte[] to = Bytes.random(20);
        Amount value = ALX.of(1);
        Amount fee = ZERO;
        long nonce = 1;
        long timestamp = TimeUtil.currentTimeMillis();
        byte[] data = Bytes.random(128);
        Transaction tx1 = new Transaction(network, type, to, value, fee, nonce, timestamp, data).sign(new Key());
        Transaction tx2 = new Transaction(network, type, to, value, fee, nonce, timestamp, data).sign(new Key());
        byte[] b1 = tx1.getHash();
        byte[] b2 = tx2.getHash();
        byte[] root = new MerkleTree(Arrays.asList(b1, b2)).getRootHash();

        byte[] merkle = MerkleUtil.computeTransactionsRoot(Arrays.asList(tx1, tx2));

        assertThat(merkle, equalTo(root));
    }

  @Test
    public void testComputeResultsRoot() {
        TransactionResult res1 = new TransactionResult(TransactionResult.Code.SUCCESS);
        TransactionResult res2 = new TransactionResult(TransactionResult.Code.FAILURE);
        res1.setReturnData(Bytes.random(20));
        res2.setReturnData(Bytes.random(20));

        byte[] b1 = Hash.h256(res1.toBytesForMerkle());
        byte[] b2 = Hash.h256(res2.toBytesForMerkle());
        byte[] root = new MerkleTree(Arrays.asList(b1, b2)).getRootHash();

        byte[] merkle = MerkleUtil.computeResultsRoot(Arrays.asList(res1, res2));

        assertThat(merkle, equalTo(root));
    }
}
