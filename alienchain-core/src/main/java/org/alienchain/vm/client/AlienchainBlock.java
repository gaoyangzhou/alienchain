/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.vm.client;

import org.alienchain.core.BlockHeader;
import org.ethereum.vm.client.Block;

/**
 * Facade for BlockHeader -> Block
 */
public class AlienchainBlock implements Block {

    private final long blockGasLimit;
    private final BlockHeader blockHeader;

    public AlienchainBlock(BlockHeader block, long blockGasLimit) {
        this.blockHeader = block;
        this.blockGasLimit = blockGasLimit;
    }

    @Override
    public long getGasLimit() {
        return blockGasLimit;
    }

    @Override
    public byte[] getParentHash() {
        return blockHeader.getParentHash();
    }

    @Override
    public byte[] getCoinbase() {
        return blockHeader.getCoinbase();
    }

    @Override
    public long getTimestamp() {
        return blockHeader.getTimestamp();
    }

    @Override
    public long getNumber() {
        return blockHeader.getNumber();
    }
}
