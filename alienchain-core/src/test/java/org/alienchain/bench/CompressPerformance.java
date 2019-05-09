/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.bench;

import java.io.IOException;

import org.alienchain.config.Config;
import org.alienchain.config.Constants;
import org.alienchain.config.MainnetConfig;
import org.alienchain.core.Block;
import org.alienchain.core.Blockchain;
import org.alienchain.core.BlockchainImpl;
import org.alienchain.db.LeveldbDatabase.LeveldbFactory;
import org.alienchain.net.msg.consensus.BlockMessage;
import org.xerial.snappy.Snappy;

public class CompressPerformance {

    enum Mode {
        ALL_BLOCKS, BLOCKS_WITH_TX
    }

    public static void main(String[] args) throws IOException {
        Config config = new MainnetConfig(Constants.DEFAULT_DATA_DIR);

        LeveldbFactory dbFactory = new LeveldbFactory(config.databaseDir());
        Blockchain chain = new BlockchainImpl(config, dbFactory);

        for (Mode mode : Mode.values()) {
            int blocks = 0;
            int transactions = 0;
            int size = 0;
            int sizeCompressed = 0;
            long time = 0;
            for (int i = 1; i <= chain.getLatestBlockNumber(); i++) {
                Block b = chain.getBlock(i);
                BlockMessage m = new BlockMessage(b);
                if (mode == Mode.BLOCKS_WITH_TX && b.getTransactions().isEmpty()) {
                    continue;
                }

                blocks++;
                transactions += b.getTransactions().size();
                size += m.getBody().length;
                long t1 = System.nanoTime();
                sizeCompressed += Snappy.compress(m.getBody()).length;
                long t2 = System.nanoTime();
                time += t2 - t1;
            }
            System.out.println("======================================");
            System.out.println(mode);
            System.out.println("======================================");
            System.out.println("# of blocks      : " + blocks);
            System.out.println("# of transactions: " + transactions);
            System.out.println("Raw size         : " + size + " bytes");
            System.out.println("Compressed size  : " + sizeCompressed + " bytes");
            System.out.println("Ratio            : " + (100.0 * sizeCompressed / size) + " %");
            System.out.println("Total time used  : " + time + " ns");
            System.out.println("Average time used: " + time / blocks + " ns");
        }
    }
}
