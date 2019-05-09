/**
 * Copyright (c) 2017-2018 The Alienchain Developers
 *
 * Distributed under the MIT software license, see the accompanying file
 * LICENSE or https://opensource.org/licenses/mit-license.php
 */
package org.alienchain.consensus;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.alienchain.config.Config;
import org.alienchain.core.Block;
import org.alienchain.core.BlockHeader;
import org.alienchain.core.Blockchain;
import org.alienchain.core.Transaction;
import org.alienchain.util.TimeUtil;

@RunWith(Parameterized.class)
public class AlienchainBftValidateBlockTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {
                        "block in the future",
                        (Callable<?>) () -> {
                            return null;
                        },
                        (Supplier<Blockchain>) () -> {
                            Blockchain blockchain = mock(Blockchain.class);
                            when(blockchain.getLatestBlock()).thenReturn(mock(Block.class));
                            return blockchain;
                        },
                        (Supplier<Config>) () -> {
                            Config config = mock(Config.class);
                            when(config.maxBlockTimeDrift()).thenReturn(TimeUnit.MINUTES.toMillis(15));
                            return config;
                        },
                        (Supplier<BlockHeader>) () -> {
                            BlockHeader blockHeader = mock(BlockHeader.class);
                            when(blockHeader.getTimestamp())
                                    .thenReturn(TimeUtil.currentTimeMillis() + TimeUnit.DAYS.toMillis(1));
                            return blockHeader;
                        },
                        (Supplier<List<Transaction>>) ArrayList::new,
                        false
                },
        });
    }

    AlienchainBft alienchainBFT;

    private BlockHeader blockHeader;

    private List<Transaction> transactions;

    private boolean result;

    public AlienchainBftValidateBlockTest(
            String name,
            Callable<Void> setUp,
            Supplier<Blockchain> chain,
            Supplier<Config> config,
            Supplier<BlockHeader> blockHeader,
            Supplier<List<Transaction>> transactions,
            boolean result) throws Exception {
        setUp.call();

        alienchainBFT = mock(AlienchainBft.class);
        alienchainBFT.chain = chain.get();
        alienchainBFT.config = config.get();
        doCallRealMethod().when(alienchainBFT).validateBlockProposal(any(), any());

        this.blockHeader = blockHeader.get();
        this.transactions = transactions.get();
        this.result = result;
    }

  @Test
    public void testValidateBlock() {
        Block block = spy(new Block(blockHeader, transactions));
        doReturn(true).when(block).validateHeader(any(), any());
        assertEquals(result, alienchainBFT.validateBlockProposal(blockHeader, transactions));
    }
}
