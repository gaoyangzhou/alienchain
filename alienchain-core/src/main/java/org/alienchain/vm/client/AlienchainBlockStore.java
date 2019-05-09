/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.vm.client;

import org.alienchain.core.Blockchain;
import org.ethereum.vm.client.BlockStore;

/**
 * Facade class for Blockchain to Blockstore
 *
 * Eventually we'll want to make blockchain just implement blockstore
 */
public class AlienchainBlockStore implements BlockStore {
    private final Blockchain blockchain;

    public AlienchainBlockStore(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    @Override
    public byte[] getBlockHashByNumber(long index) {
        return blockchain.getBlockHeader(index).getHash();
    }
}
