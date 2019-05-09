/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.net.msg.consensus;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;

import org.junit.Test;
import org.alienchain.core.BlockHeader;
import org.alienchain.crypto.Key;
import org.alienchain.net.msg.MessageCode;
import org.alienchain.util.Bytes;
import org.alienchain.util.MerkleUtil;
import org.alienchain.util.TimeUtil;

public class BlockHeaderMessageTest {

  @Test
    public void testSerialization() {
        long number = 1;
        byte[] coinbase = Bytes.random(Key.ADDRESS_LEN);
        byte[] prevHash = Bytes.random(32);
        long timestamp = TimeUtil.currentTimeMillis();
        byte[] transactionsRoot = MerkleUtil.computeTransactionsRoot(Collections.emptyList());
        byte[] resultsRoot = MerkleUtil.computeResultsRoot(Collections.emptyList());
        byte[] stateRoot = Bytes.EMPTY_HASH;
        byte[] data = {};

        BlockHeader header = new BlockHeader(number, coinbase, prevHash, timestamp, transactionsRoot, resultsRoot,
                stateRoot, data);

        BlockHeaderMessage m = new BlockHeaderMessage(header);
        assertThat(m.getCode()).isEqualTo(MessageCode.BLOCK_HEADER);
        assertThat(m.getResponseMessageClass()).isNull();

        BlockHeaderMessage m2 = new BlockHeaderMessage(m.getBody());
        assertThat(m2.getCode()).isEqualTo(MessageCode.BLOCK_HEADER);
        assertThat(m2.getResponseMessageClass()).isNull();
        assertThat(m2.getHeader()).isEqualToComparingFieldByField(header);
    }
}
