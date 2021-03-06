/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.consensus;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.alienchain.TestUtils.createBlock;
import static org.alienchain.core.Amount.Unit.NANO_ALX;

import java.util.Collections;

import org.junit.Test;
import org.alienchain.Network;
import org.alienchain.core.Amount;
import org.alienchain.core.Block;
import org.alienchain.core.Transaction;
import org.alienchain.core.TransactionResult;
import org.alienchain.core.TransactionType;
import org.alienchain.crypto.Key;
import org.alienchain.util.Bytes;
import org.alienchain.util.TimeUtil;

public class ProposalTest {

  @Test
    public void testBasics() {
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
        TransactionResult res = new TransactionResult();

        long height = Long.MAX_VALUE;
        int view = Integer.MAX_VALUE;
        Block block = createBlock(height, Collections.singletonList(tx), Collections.singletonList(res));
        Vote vote = Vote.newReject(VoteType.VALIDATE, height, view - 1);
        vote.sign(new Key());

        Proof proof = new Proof(height, view, Collections.singletonList(vote));
        Proposal p = new Proposal(proof, block.getHeader(), block.getTransactions());
        Key key = new Key();
        p.sign(key);

        assertThat(p.getTransactions(), contains(tx));
        assertThat(p.getSignature().getAddress(), equalTo(key.toAddress()));
    }

  @Test
    public void testProposal() {
        long height = Long.MAX_VALUE;
        int view = Integer.MAX_VALUE;
        Block block = createBlock(height, Collections.emptyList(), Collections.emptyList());
        Vote vote = Vote.newReject(VoteType.VALIDATE, height, view - 1);
        vote.sign(new Key());

        Proof proof = new Proof(height, view, Collections.singletonList(vote));
        Proposal p = new Proposal(proof, block.getHeader(), block.getTransactions());
        assertFalse(p.validate());
        p.sign(new Key());
        assertTrue(p.validate());

        assertTrue(!p.toString().startsWith("java.lang.Object"));

        Proposal p2 = Proposal.fromBytes(p.toBytes());

        assertEquals(height, p2.getHeight());
        assertEquals(view, p2.getView());
        assertArrayEquals(block.getHash(), p2.getBlockHeader().getHash());
        assertEquals(1, p2.getProof().getVotes().size());
        assertArrayEquals(vote.getBlockHash(), p2.getProof().getVotes().get(0).getBlockHash());
    }
}
