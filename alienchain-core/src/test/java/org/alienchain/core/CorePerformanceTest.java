/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.core;

import static org.junit.Assert.assertTrue;
import static org.alienchain.core.Amount.Unit.NANO_ALX;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.alienchain.vm.client.AlienchainBlock;
import org.alienchain.vm.client.AlienchainBlockStore;
import org.junit.Rule;
import org.junit.Test;
import org.alienchain.Network;
import org.alienchain.config.Config;
import org.alienchain.config.Constants;
import org.alienchain.config.DevnetConfig;
import org.alienchain.core.state.Delegate;
import org.alienchain.crypto.Key;
import org.alienchain.rules.TemporaryDatabaseRule;
import org.alienchain.util.Bytes;
import org.alienchain.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CorePerformanceTest {

    @Rule
    public TemporaryDatabaseRule temporaryDBFactory = new TemporaryDatabaseRule();

    private static final Logger logger = LoggerFactory.getLogger(CorePerformanceTest.class);

    private Config config = new DevnetConfig(Constants.DEFAULT_DATA_DIR);

  @Test
    public void testSortDelegate() {
        List<Delegate> list = new ArrayList<>();
        int nDelegates = 100_000;

        Random r = new Random();
        for (int i = 0; i < nDelegates; i++) {
            Delegate d = new Delegate(Bytes.random(20), Bytes.random(16), r.nextLong(), NANO_ALX.of(r.nextLong()));
            list.add(d);
        }

        long t1 = System.nanoTime();
        list.sort((d1, d2) -> d2.getVotes().compareTo(d1.getVotes()));
        long t2 = System.nanoTime();
        logger.info("Perf_delegate_sort: {} μs", (t2 - t1) / 1_000);
    }

  @Test
    public void testTransactionProcessing() {
        List<Transaction> txs = new ArrayList<>();
        int repeat = 1000;

        for (int i = 0; i < repeat; i++) {
            Key key = new Key();

            TransactionType type = TransactionType.TRANSFER;
            byte[] to = Bytes.random(20);
            Amount value = NANO_ALX.of(5);
            Amount fee = config.minTransactionFee();
            long nonce = 1;
            long timestamp = TimeUtil.currentTimeMillis();
            byte[] data = Bytes.random(16);

            Transaction tx = new Transaction(Network.DEVNET, type, to, value, fee, nonce, timestamp, data);
            tx.sign(key);
            txs.add(tx);
        }

        long t1 = System.nanoTime();
        for (Transaction tx : txs) {
            assertTrue(tx.validate(Network.DEVNET));
        }
        long t2 = System.nanoTime();
        logger.info("Perf_transaction_1: {} μs/tx", (t2 - t1) / 1_000 / repeat);

        Blockchain chain = new BlockchainImpl(config, temporaryDBFactory);
        TransactionExecutor exec = new TransactionExecutor(config, new AlienchainBlockStore(chain));

        t1 = System.nanoTime();
        exec.execute(txs, chain.getAccountState().track(), chain.getDelegateState().track(),
                new AlienchainBlock(chain.getLatestBlock().getHeader(), config.vmMaxBlockGasLimit()), null);
        t2 = System.nanoTime();
        logger.info("Perf_transaction_2: {} μs/tx", (t2 - t1) / 1_000 / repeat);
    }
}
