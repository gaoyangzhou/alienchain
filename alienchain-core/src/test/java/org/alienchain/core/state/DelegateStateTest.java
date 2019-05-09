/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.core.state;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.alienchain.core.Amount.Unit.NANO_ALX;
import static org.alienchain.core.Amount.Unit.ALX;
import static org.alienchain.core.Amount.ZERO;

import java.util.List;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.alienchain.config.Constants;
import org.alienchain.config.DevnetConfig;
import org.alienchain.core.Amount;
import org.alienchain.core.Blockchain;
import org.alienchain.core.BlockchainImpl;
import org.alienchain.crypto.Key;
import org.alienchain.rules.TemporaryDatabaseRule;
import org.alienchain.util.ByteArray;
import org.alienchain.util.Bytes;

public class DelegateStateTest {

    private Blockchain chain;
    private DelegateState ds;
    Map<String, byte[]> delegates;

    @Rule
    public TemporaryDatabaseRule temporaryDBFactory = new TemporaryDatabaseRule();

    @Before
    public void init() {
        chain = new BlockchainImpl(new DevnetConfig(Constants.DEFAULT_DATA_DIR), temporaryDBFactory);
        ds = chain.getDelegateState();
        delegates = chain.getGenesis().getDelegates();
    }

  @Test
    public void testAtGenesis() {
        for (String k : delegates.keySet()) {
            Delegate d = ds.getDelegateByAddress(delegates.get(k));
            assertNotNull(d);
            assertArrayEquals(delegates.get(k), d.getAddress());
            assertArrayEquals(Bytes.of(k), d.getName());
            assertEquals(ZERO, d.getVotes());
        }

        assertEquals(delegates.size(), ds.getDelegates().size());
        assertEquals(delegates.size(), chain.getValidators().size());
    }

  @Test
    public void testVoteWithoutRegistration() {
        Key delegate = new Key();
        Key voter = new Key();

        assertFalse(ds.vote(voter.toAddress(), delegate.toAddress(), NANO_ALX.of(1)));
    }

  @Test
    public void testVoteOneDelegate() {
        byte[] delegate = new Key().toAddress();
        byte[] delegateName = Bytes.of("delegate");

        byte[] voter = new Key().toAddress();

        assertTrue(ds.register(delegate, delegateName));
        for (int i = 0; i < 1000; i++) {
            assertTrue(ds.vote(voter, delegate, NANO_ALX.of(1000)));
        }

        List<Delegate> list = ds.getDelegates();
        assertEquals(delegates.size() + 1, list.size());

        assertArrayEquals(delegate, list.get(0).getAddress());
        assertArrayEquals(delegateName, list.get(0).getName());
        assertEquals(NANO_ALX.of(1000 * 1000), list.get(0).getVotes());
    }

  @Test
    public void testMultipleDelegates() {
        byte[] delegate = null;
        byte[] delegateName = null;

        byte[] voter = new Key().toAddress();

        for (int i = 0; i < 200; i++) {
            delegate = new Key().toAddress();
            delegateName = Bytes.of("delegate" + i);
            assertTrue(ds.register(delegate, delegateName));
            assertTrue(ds.vote(voter, delegate, NANO_ALX.of(i)));
        }

        List<Delegate> list = ds.getDelegates();
        assertEquals(delegates.size() + 200, list.size());

        assertArrayEquals(delegate, list.get(0).getAddress());
        assertArrayEquals(delegateName, list.get(0).getName());
        assertEquals(NANO_ALX.of(200 - 1), list.get(0).getVotes());
    }

  @Test
    public void testUnvote() {
        byte[] voter = new Key().toAddress();
        byte[] delegate = new Key().toAddress();
        Amount value = ALX.of(2);

        ds.register(delegate, Bytes.of("test"));
        assertFalse(ds.unvote(voter, delegate, value));
        ds.vote(voter, delegate, value);
        assertTrue(ds.vote(voter, delegate, value));
    }

  @Test
    public void testGetVote() {
        byte[] voter = new Key().toAddress();
        byte[] delegate = new Key().toAddress();
        Amount value = ALX.of(2);

        ds.register(delegate, Bytes.of("test"));
        assertTrue(ds.vote(voter, delegate, value));
        assertEquals(value, ds.getVote(voter, delegate));
        assertTrue(ds.vote(voter, delegate, value));
        assertEquals(ALX.of(4), ds.getVote(voter, delegate));
        ds.commit();
        assertEquals(ALX.of(4), ds.getVote(voter, delegate));
    }

  @Test
    public void testGetVotes() {
        Key delegateKey = new Key();
        Key voterKey1 = new Key();
        Amount value1 = ALX.of(1);
        Key voterKey2 = new Key();
        Amount value2 = ALX.of(2);

        ds.register(delegateKey.toAddress(), Bytes.of("test"));
        assertTrue(ds.vote(voterKey1.toAddress(), delegateKey.toAddress(), value1));
        assertTrue(ds.vote(voterKey2.toAddress(), delegateKey.toAddress(), value2));
        ds.commit();

        Map<ByteArray, Amount> votes = ds.getVotes(delegateKey.toAddress());
        assertEquals(value1, votes.get(new ByteArray(voterKey1.toAddress())));
        assertEquals(value2, votes.get(new ByteArray(voterKey2.toAddress())));
    }

    @After
    public void rollback() {
        ds.rollback();
    }
}
