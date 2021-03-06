/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.core;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.alienchain.config.Constants;
import org.alienchain.util.Bytes;
import org.alienchain.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockHeaderTest {
    private static final Logger logger = LoggerFactory.getLogger(BlockHeaderTest.class);

    private long number = 1;
    private byte[] coinbase = Bytes.random(20);
    private byte[] prevHash = Bytes.random(32);
    private long timestamp = TimeUtil.currentTimeMillis();
    private byte[] transactionsRoot = Bytes.random(32);
    private byte[] resultsRoot = Bytes.random(32);
    private byte[] stateRoot = Bytes.random(32);
    private byte[] data = Bytes.of("data");

    private byte[] hash;

  @Test
    public void testNew() {
        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);
        hash = header.getHash();

        testFields(header);
    }

  @Test
    public void testSerialization() {
        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);
        hash = header.getHash();

        testFields(BlockHeader.fromBytes(header.toBytes()));
    }

  @Test
    public void testBlockHeaderSize() {
        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);
        byte[] bytes = header.toBytes();

        logger.info("block header size: {}", bytes.length);
        logger.info("block header size (1y): {} GB",
                1.0 * bytes.length * Constants.BLOCKS_PER_YEAR / 1024 / 1024 / 1024);
    }

    private void testFields(BlockHeader header) {
        assertArrayEquals(hash, header.getHash());
        assertEquals(number, header.getNumber());
        assertArrayEquals(coinbase, header.getCoinbase());
        assertArrayEquals(prevHash, header.getParentHash());
        assertEquals(timestamp, header.getTimestamp());
        assertArrayEquals(transactionsRoot, header.getTransactionsRoot());
        assertArrayEquals(resultsRoot, header.getResultsRoot());
        assertArrayEquals(stateRoot, header.getStateRoot());
        assertArrayEquals(data, header.getData());
    }
}
